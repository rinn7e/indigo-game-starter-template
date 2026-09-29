# Code Conventions for `indigo-game-starter-template`

How this codebase names and organises its code. For how the overall structure maps onto a TEA
(The Elm Architecture) web app, see [tea-isomorphism.md](tea-isomorphism.md).

## Drawing types

These are Indigo's types for "what to draw". Every one of them is a pure description, built fresh
each frame; none of them emits messages (input arrives through `subscriptions`).

| Type | Elm-ish analogy | In our code |
| --- | --- | --- |
| `Outcome[SceneUpdateFragment]` | the view result paired with effects (Indigo lets `present` emit events too) | `Program.present` |
| `SceneUpdateFragment` | the whole `Html msg` a page's `view` returns | each scene's `ui(shared, model)`, `mainUI`, `ScreenUI.screenUI` |
| `LayerKey` | the name of a stacking layer | `Layers.world`, `Layers.ui`, `Layers.fps` |
| `Layer.Content` | a positioned full-screen container with its own z-order | the world layer (with `Camera`), the ui layer |
| `Camera` | scroll position of a container | `Camera.Fixed(cam)` on the world layer |
| `Magnification` | CSS `zoom` for a layer | `Magnification(2)`, everything drawn at 2× |
| `Batch[SceneNode]` | `List (Html msg)` | pieces of a screen: `battleLogUI`, `hudUI`, `panelUI`, ... |
| `SceneNode` | `Html msg` (but emits no messages) | any single node below |
| `Group` | `div` (children moved / scaled / rotated as one) | sprites (`playerSpriteUI`), centring in `screenUI` |
| `Quad` | a `div` with a background colour | panels, bars, highlights, backdrops |
| `Shape.Circle` | an SVG circle | heads, slime bodies, tree tops |
| `Text[Material.ImageEffects]` | a `span` of text | `labelUI`, via `GameAssets.textUI` |
| `CloneTiles` | (no DOM equivalent) thousands of identical tiles in one draw | `terrainUI` |
| `CloneBlank` | (no DOM equivalent) the tile template | `terrainCloneBlankUI` |

Which to return:

- a whole screen → `SceneUpdateFragment`
- a piece of a screen → `Batch[SceneNode]`
- one thing → `SceneNode` (or its specific type, e.g. `Quad`)
- one thing made of parts that moves together → `Group`

## Rules

### 0. One file, one module

Every file `X.scala` defines `object X` (or a type `X` with its companion `object X`), and nothing
else at the top level. The object is the module, like Haskell's
`module Rpg.Scene.EndScene.Update` for `Rpg/Scene/EndScene/Update.hs`; packages stay lowercase
(Scala convention), and only serve as the namespace.

```scala
// scene/endscene/Update.scala              ~ module Rpg.Scene.EndScene.Update
package rpg.scene.endscene

import rpg.scene.endscene.Type.*            // ~ import Rpg.Scene.EndScene.Type

object Update:
  def update(shared: Shared, msg: Msg, model: Model): Outcome[Model] = ...
```

- **Import modules explicitly**, siblings included (`import rpg.scene.endscene.Type.*`,
  `import rpg.common.util.Dialogue.*`). There are no wildcard imports of whole packages.
- **Barrels are Haskell style**: a folder `foo/` gets a sibling `Foo.scala` whose `object Foo`
  re-exports it (~ `Rpg/Scene/WorldScene.hs` re-exporting `Rpg/Scene/WorldScene/*`, or an
  `index.ts`): `scene/WorldScene.scala` (and `TitleScene`, `BattleScene`, `EndScene`),
  `scene/battlescene/subui/ActionMenu.scala`, `common/Types.scala`. Parents use them qualified:
  `import rpg.scene.WorldScene`, then `WorldScene.update`, `WorldScene.Msg`, `WorldScene.ui`.
- `common/util/` has no barrel on purpose: its modules are named after the types they extend
  (`Dialogue`, `TileMap`), so importing them all at once would clash with the types.

### 1. Anything that is or returns a drawing type ends in `UI`

Any function, **public or private**, whose return type is a drawing type (`SceneUpdateFragment`,
`Layer`, `Batch[SceneNode]`, `SceneNode`, or a node type such as `Group`, `Quad`, `Shape`, `Text`,
`Graphic`, `CloneTiles`, `CloneBlank`) is named with a `UI` suffix, and so is any **value**,
including local `val`s, holding one. The name then tells you it draws something.

```scala
// Good
def battleLogUI(model: Model): Batch[SceneNode]
private def heroStatusPanelUI(model: Model): Batch[SceneNode]
private def boxUI(x: Int, y: Int, w: Int, h: Int, color: RGBA): Quad
private val shadowUI: SceneNode = ...
val statsUI = panelUI(...) ++ Batch(...)    // a local val inside hudUI

// Bad
def battleLog(model: Model): Batch[SceneNode]
private def heroStatusPanel(model: Model): Batch[SceneNode]
private def box(x: Int, y: Int, w: Int, h: Int, color: RGBA): Quad
private val shadow: SceneNode = ...
val stats = panelUI(...) ++ Batch(...)
```

Values that only *feed* drawing keep plain names: colours and gradients (`Fill`, `RGBA`),
positions and sizes (`Point`, `Rectangle`), numbers and strings (`bob`, `objective`).

Exceptions:

- a function already named `ui` (each scene's `UI.scala`, a stateful sub-UI's `UI.scala`);
- methods whose name Indigo requires, such as `present`.

### 2. Stateless UI: one `XxxUI` module, its function imported directly

A stateless ("dumb") piece of UI lives in `XxxUI.scala` as `object XxxUI`, whose main function is
`xxxUI`. The `object` makes the file its own module (like a Haskell module), and consumers import
just the function:

```scala
// scene/battlescene/subui/BattleLogUI.scala  ~ module Rpg.Scene.BattleScene.Subui.BattleLogUI
package rpg.scene.battlescene.subui
object BattleLogUI:
  def battleLogUI(model: Model): Batch[SceneNode] = ...

// scene/battlescene/UI.scala                 ~ import ...BattleLogUI (battleLogUI)
import rpg.scene.battlescene.subui.BattleLogUI.battleLogUI
battleLogUI(model)
```

A module may hold a few closely related functions when they share private helpers
(`LabelUI.{labelUI, centeredLabelUI, ...}`, `CharacterSpriteUI.{playerSpriteUI, ...}`).

Where it lives:

- used by one scene → `scene/<name>/subui/`;
- used by several scenes → `ui/`.

### 3. Stateful UI: a TEA module folder

A piece of UI with its own state is a TEA module in its own folder (`Type` / `Update` /
`Subscription` / `UI`), wired into its parent scene the usual TEA way. Example:
`scene/battlescene/subui/actionmenu/`. See §7 of [tea-isomorphism.md](tea-isomorphism.md).

### 4. TEA child msg interception

A common convention in TEA web apps, ported here. When a parent needs to respond to a
child's messages, it delegates to the child's `update`, puts the child's model back, then
intercepts the messages it cares about in a `flatMap`. `Outcome.flatMap` is `updateAndCmd`: it runs
the next step and keeps the events of both.

| TEA (react-tea-cup) | Indigo |
| --- | --- |
| `[model, cmd]` | `Outcome[Model]` |
| `pipe(x, updateAndCmd(f), updateAndCmd(g))` | `x.flatMap(f).flatMap(g)` |
| `updateAndCmdExtra` → `[model, cmd, A]` | `Outcome[(Model, A)]` |

```scala
// scene/battlescene/Update.scala
case Msg.ActionMenuMsg(subMsg) =>
  Outcome(model.copy(menu = ActionMenu.update(subMsg, model.menu)))
    .flatMap { m =>
      subMsg match
        case ActionMenu.Msg.Choose =>
          Outcome(perform(ActionMenu.selectedAction(m.menu), shared, m))

        case ActionMenu.Msg.CursorUp | ActionMenu.Msg.CursorDown =>
          Outcome(m)
    }
```

- **No `OutMsg`.** A child never sends messages to its parent. If the parent needs to know what
  happened inside the child, it reads the child's *updated* model (`m`) in the interception: e.g.
  the root intercepts the world's `Tick` / `Walk` and reads `m.world.encounter`.
- **Parent → child** is a direct call to the child's `update` with a child `Msg`
  (`WorldScene.update(shared, WorldScene.Msg.BattleWon(...), model.world)`), not an event.
- **`GlobalEvent`s are effects, never messages between our own modules**: engine effects
  (`PlaySound`), or a scene's own events (rule 6).
- The same rule applies at every level: root → scene (`Update.scala`) and scene → stateful sub-UI
  (`scene/battlescene/Update.scala`).

### 5. Plain curried functions, data last, chained with `pipe`

Our own helpers are plain functions, never extension methods. They are **curried with the thing
they work on last**, like Elm / Haskell:

```scala
// Good                                     ~ enemyAt :: Point -> Model -> Maybe Enemy
def enemyAt(pt: Point)(model: Model): Option[Enemy]
def damage(amount: Int)(stats: Stats): Stats
def withToast(message: String, now: Seconds)(model: Model): Model

enemyAt(target)(model)                      // ~ enemyAt target model

// Bad
extension (model: Model) def enemyAt(pt: Point): Option[Enemy]
model.enemyAt(target)
def enemyAt(model: Model, pt: Point): Option[Enemy]   // data first: can't be piped
```

When a value flows through several steps, chain them with `pipe` from the standard library
(`import scala.util.chaining.*`), the equivalent of Elm's `|>` or fp-ts's `pipe`:

```scala
model                                       // ~ model
  .copy(chests = opened)                    //     |> (\m -> { m | chests = opened })
  .pipe(withToast("FOUND A POTION!", now))  //     |> withToast "FOUND A POTION!" now
```

- A single application stays a plain call: `isSafe(now)(player)`, not `player.pipe(isSafe(now))`.
- Parameters that aren't the subject are grouped in the first list (`withToast(message, now)`),
  so partial application gives the `A => A` step that `pipe` needs.
- Methods of library and built-in types stay as they are (`model.copy(...)`, `batch.map(...)`),
  and `Outcome` steps chain with `flatMap` (rule 4), not `pipe`.

### 6. Entering a scene: `init` returns `Outcome[Model]`

Every scene's `init` is `init(shared, ...): Outcome[Model]`, like a TEA page's
`init(shared, route): [Model, Cmd]`. Its parameters are what the scene needs on entry (~ route
params: `BattleScene.init(shared, player, enemy)`), and the root calls it whenever it navigates to
the scene, so a scene's model exists only once it's been entered. A stateful sub-UI's `init` returns
plain `Model`, like a web app component's `Dropdown.init()`.

Entry effects go in the `Outcome`, even when a scene has none yet, so adding one later changes no
signature. There are two kinds:

```scala
// Fire and forget (~ a Cmd that never replies, e.g. a port)
Outcome(model).addGlobalEvents(Sfx.battle)

// A result that comes back as a Msg (~ Task.perform): the scene's own engine event, named the
// Indigo way (like KeyboardEvent, SceneEvent), as a past-tense fact
enum BattleEvent extends GlobalEvent derives CanEqual:   // scene/battlescene/Type.scala
  case FightStarted

Outcome(model).addGlobalEvents(BattleEvent.FightStarted) // BattleScene.init
case BattleEvent.FightStarted => Some(Msg.FightStarted)  // BattleScene.subscriptions (next frame)
case Msg.FightStarted => ...                             // BattleScene.update
```

`subscriptions` treats a scene's own events exactly like engine events (`KeyboardEvent.KeyDown` →
`Msg.Walk`), so `update` still only sees `Msg`. Don't call the enum `Cmd`: in TEA, `Cmd` is the
type of *all* effects, and `tyrian.Cmd` is a real type in this codebase.

Every scene records `enteredAt` in `init` and fades in with `fadeInUI(model.enteredAt, now,
viewport)`. The world is also re-entered after a battle, and does the same.

