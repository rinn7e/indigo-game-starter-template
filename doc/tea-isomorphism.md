# TEA web app ↔ Indigo game: the isomorphism

This game is structured like a typical TEA (The Elm Architecture) web app, as written in Elm or
in TypeScript with [react-tea-cup](https://github.com/vankeisb/react-tea-cup). If you can find your
way around such an app, you can find your way around this one. This document maps one onto the
other; "the web app" below means that kind of app, with pages such as `page/settings/` and
components such as `component/dropdown/`.

The short version: **a TEA page is a game scene**. Every scene has a `Model`, a `Msg`, an
`update`, a `ui` (TEA's `view`) and `subscriptions`, and lives in `scene/<name>/`. The root program
routes between scenes exactly like the web app's root routes between pages: it wraps each scene's
`Msg`, delegates to the showing scene, and intercepts the child messages it cares about.

**Vocabulary** (game side uses game terms, the concepts are the same):

| TEA web app                   | Game                                   |
| ----------------------------- | -------------------------------------- |
| page                          | scene                                  |
| `view` / `component.tsx`      | `ui` / `UI.scala`                      |
| `sub-component/`              | `subui/`                               |
| `component/` (reusable)       | `ui/` (reusable)                       |
| `AppRoute` (which page shows) | `SceneRoute` (which scene shows)       |
| `SettingsPageMsg { subMsg }`  | `WorldSceneMsg(subMsg)`                |

### The same loop, two runtimes

```text
   TEA web app (tea-cup + React)          Game (Indigo)
   -----------------------------          -------------

   DOM / window / HTTP events             FrameTick (~60/s) + keyboard events
            |                                      |
            v                                      v
   subscriptions  ->  Msg                 subscriptions  ->  Msg
            |                                      |
            v                                      v
   update(shared, msg, model)             update(shared, msg, model)
            |                                      |
            v                                      v
   [Model, Cmd]                           Outcome[Model]
     |       \                              |       \
     |      Cmd runs; its result            |      events arrive next frame
     |      comes back as a Msg             |      and come back as a Msg
     v                                      v
   view(model)  ->  JSX                   ui(shared, model)
            |                               ->  SceneUpdateFragment
            v                                      |
   React renders the DOM                           v
                                          Indigo draws the frame (WebGL)
```

The shapes are identical. The two differences: the game has a clock that fires every frame
(`FrameTick`), and it redraws everything every frame instead of diffing.

---

## 1. Folder map

| TEA frontend (`src/`)                          | Game (`src/game/`)                                  | Notes |
| ---------------------------------------------- | --------------------------------------------------- | ----- |
| `root.tsx` — mounts the program                | `Main.scala` — `object Main`, `@JSExportTopLevel("IndigoGame")` | Entry point the HTML page calls. |
| `program.tsx` — `ProgramWithNav(init, update, view, subscriptions)` | `Main.scala` — `class Program`, the Indigo `Game`: `initialModel`, `updateModel`, `present` | Same four things, Indigo names. |
| `app.tsx` — root view / layout                 | `Main.scala` — `mainUI` (layer order + the showing scene's `ui`) | These three live in one file; they're all root wiring. |
| `type.ts` — root `Model`, `Msg`                | `Type.scala` — root `Model`, `Msg`                  | |
| `update.ts` — root `init`, `update`            | `Update.scala` — root `init`, `update`              | Delegates, intercepts, navigates (§5). |
| `subscription.ts`                              | `Subscription.scala`                                | The showing scene's subscriptions, wrapped. |
| `common/type/route`                            | `common/types/SceneRoute.scala`                     | Navigating = setting `route` in the root model. |
| `common/type/shared.ts` (`Shared`)             | `common/types/Shared.scala`                         | Passed to every scene's `update`/`ui`. |
| `common/type/`                                 | `common/types/` + barrel `common/Types.scala`       | `type` is a Scala keyword, hence `types`. Consumers `import game.common.Types.*`. |
| `common/util/`                                 | `common/util/`                                      | Pure helpers: curried functions, data last (`tileAt(pt)(map)`). |
| `common/constant/`                             | `common/constant/`                                  | Layout sizes, layer keys, the map. |
| `component/` — reusable views                  | `ui/` — `PanelUI`, `BarUI`, `LabelUI`, `ScreenUI`, `CharacterSpriteUI` | Same `XxxUI` module shape as stateless sub-UIs (§7). |
| `theme/`                                       | `theme/Palette.scala`                               | |
| `page/<name>/type.ts`                          | `scene/<name>/Type.scala`                           | `Model`, `Msg`. |
| `page/<name>/update.ts`                        | `scene/<name>/Update.scala`                         | `init(shared, ...): Outcome[Model]` (~ `[Model, Cmd]`), `update`. |
| `page/<name>/subscription.ts`                  | `scene/<name>/Subscription.scala`                   | |
| `page/<name>/component.tsx`                    | `scene/<name>/UI.scala`                             | `ui` (TEA's `view`). |
| `page/<name>/sub-component/`                   | `scene/<name>/subui/`                               | |
| `page/<name>/common/util.ts`                   | `scene/<name>/common/Util.scala`                    | |
| `page/<name>/index.ts` (barrel)                | `scene/WorldScene.scala` — `object WorldScene` re-exporting `worldscene/` | Haskell style: `Game/Scene/WorldScene.hs` next to `Game/Scene/WorldScene/`. The parent does `import game.scene.WorldScene`. |

Scenes: `title`, `world`, `battle`, `end`.

### Who imports whom

Arrows point from a module to what it depends on. As in the web app, dependencies only flow
downwards: scenes never import the root or each other.

```text
                     Main.scala    (root.tsx + program.tsx + app.tsx)
                          |
                 +--------+--------+
                 |                 |
                 v                 v
            Update.scala      Subscription.scala
            (update.ts)       (subscription.ts)
                 |                 |
                 +--------+--------+
                          v
                     Type.scala    (type.ts)
                          |
                          v
   +-------------------------------------------------------------+
   |  scene/  titlescene/  worldscene/  battlescene/  endscene/  |
   |    each:     Type  Update  Subscription  UI                 |
   |    barrels:  TitleScene  WorldScene  BattleScene  EndScene  |
   +-------------------------------------------------------------+
          |                    |                   |
          v                    v                   v
     ui/                   common/              theme/
     PanelUI  BarUI        types  util          Palette
     LabelUI  ScreenUI     constant  asset
     CharacterSpriteUI

   ui/ also uses common/ and theme/.
   The root files import each scene through its barrel module: game.scene.{WorldScene, ...}.
```

---

## 2. Model and Msg

**TEA (TypeScript)**

```ts
// page/settings/type.ts
export type Model = {
  readonly profile: RemoteData<HttpError, Profile>
  readonly theme: Theme
}

export type Msg =
  | { readonly _tag: 'GotProfile'; readonly data: ... }
  | { readonly _tag: 'SetTheme'; readonly theme: Theme }
```

**Game (Scala)**

```scala
// scene/worldscene/Type.scala
final case class Model(
    map: TileMap,
    player: Player,
    enemies: Batch[Enemy],
    // ...
) derives CanEqual

enum Msg derives CanEqual:
  case Tick(held: Option[Direction])
  case Walk(direction: Direction)
  case Confirm
  case BattleWon(player: Player, enemy: Enemy)
  case BattleEscaped(player: Player, enemy: Enemy)
```

`enum` ↔ tagged union, `case class` ↔ readonly record, `.copy(...)` ↔ `{ ...model, x }`.
Every scene has a `Model`, even a simple one: the title's only holds `enteredAt` (for its
fade-in), so every scene has the same shape.

---

## 3. update: `[Model, Cmd<Msg>]` ↔ `Outcome[Model]`

Every scene is the same set of files. Here is one frame's journey through the world scene:

```text
   engine event  (FrameTick, KeyDown, KeyUp)
         |
         v
   Subscription.scala     subscriptions(model, keyboard)
         |
         |  Some(Msg)            (None: the event is ignored)
         v
   Update.scala           update(shared, msg, model)  <-----+
         |                                                  |
         v                                                  +----  Shared
   Outcome[Model]  --- root intercepts (section 5)          |      (now, dice, viewport)
         |                                                  |
         |  new Model                                       |
         v                                                  |
   UI.scala               ui(shared, model)  <--------------+
         |
         v
   SceneUpdateFragment    (layers of nodes)

   Type.scala defines the Model and Msg that all of the above use.
```

```ts
// TEA
export const update = (shared: Shared, msg: Msg, model: Model): [Model, Cmd<Msg>] =>
```

```scala
// Game
def update(shared: Shared, msg: Msg, model: Model): Outcome[Model]
```

| TEA                              | Indigo                                              |
| -------------------------------- | --------------------------------------------------- |
| `[model, Cmd.none()]`            | `Outcome(model)`                                    |
| `[model, someCmd]`               | `Outcome(model).addGlobalEvents(event)`             |
| `Cmd.batch([...])`               | `.addGlobalEvents(a, b, ...)`                       |
| `pipe(x, updateAndCmd(f))`       | `x.flatMap(f)`                                      |
| `updateAndCmdExtra`              | `Outcome[(Model, A)]`                               |

`Outcome` is the "model + effects" pair; the effects are `GlobalEvent`s the engine delivers on
the next frame, which is exactly how a `Cmd` resolves into a later `Msg`. A fire-and-forget effect
is an engine event (`Sfx.battle`, a `PlaySound`); an effect whose result comes back is the scene's
own event, which its `subscriptions` turn into a `Msg` like any engine event
(`BattleScene.BattleEvent.FightStarted` → `Msg.FightStarted`; rule 6 of
[code-convention.md](code-convention.md)). `flatMap` runs the next
step on the model and keeps the events of both steps, exactly what `updateAndCmd` does with
`Cmd.batch`.

**Randomness and time** are not effects here. They arrive in `Shared` (`shared.now`,
`shared.dice`), and the dice is deterministic per frame, so `update` stays a pure function you can
unit test with `Dice.loaded(n)`. That's the equivalent of passing a seed instead of calling
`Math.random()`.

---

## 4. subscriptions

In the web app, subscriptions turn outside events (window resize, OS dark mode) into `Msg`s.
In the game, *all* input is outside events: keyboard, mouse, and the frame clock. Each scene's
`subscriptions` translates engine events into that scene's `Msg`:

```ts
// TEA: subscription.ts
windowEvents.on('resize', (): Msg => ({ _tag: 'WindowResized', isMobile: getIsMobile() }))
```

```scala
// Game: scene/worldscene/Subscription.scala
def subscriptions(model: Model, keyboard: Keyboard): GlobalEvent => Option[Msg] =
  case FrameTick                    => Some(Msg.Tick(Input.heldDirection(keyboard)))
  case KeyboardEvent.KeyDown(key)   => Input.direction(key).map(Msg.Walk(_))
  case e if Input.isConfirm(e)      => Some(Msg.Confirm)
  case _                            => None
```

`FrameTick` (every frame, ~60/s) is the game's `Sub.every(...)`: animation, enemy AI and
held-key movement all run off `Msg.Tick`. Because of this split, `update` never sees raw engine
events; it only sees the scene's own `Msg`, just like a page in the web app.

---

## 5. Parent ↔ child: routing and child msg interception

The root is the web app's root, one to one. Its `Msg` wraps each scene's **whole** `Msg`:

```ts
// TEA: type.ts
| { readonly _tag: 'SettingsPageMsg'; readonly subMsg: Settings.Msg }
```

```scala
// Game: Type.scala
enum Msg derives CanEqual:
  case TitleSceneMsg(subMsg: TitleScene.Msg)
  case WorldSceneMsg(subMsg: WorldScene.Msg)
  case BattleSceneMsg(subMsg: BattleScene.Msg)
  case EndSceneMsg(subMsg: EndScene.Msg)
```

`Subscription.scala` asks only the showing scene for its subscriptions and wraps the result, like
`Settings.subscriptions(...).map((subMsg) => ({ _tag: 'SettingsPageMsg', subMsg }))`.
`Main.scala`'s `mainUI` draws only the showing scene's `ui`.

`Update.scala` uses the web app's **TEA child msg interception** pattern (rule 4 of
[code-convention.md](code-convention.md)): delegate to the child's `update`, put its model back,
then intercept the child messages the parent cares about. `flatMap` is the `updateAndCmd` helper
many tea-cup codebases define (run the next step, batch both steps' commands):

```ts
// TEA
case 'ChildMsg': {
  const [newChildModel, childCmd] = Child.update(msg.subMsg, model.childModel)
  return pipe(
    [{ ...model, child: newChildModel }, childCmd.map(...)],
    updateAndCmd((m) => msg.subMsg._tag === 'MsgToIntercept' ? [...] : [m, Cmd.none()]),
  )
}
```

```scala
// Game: Update.scala
case Msg.BattleSceneMsg(subMsg) =>
  BattleScene
    .update(shared, subMsg, battle)
    .map(battle => model.copy(battle = Some(battle)))
    .flatMap { m =>
      (subMsg, m.battle) match
        case (BattleScene.Msg.Continue, Some(battle)) => finishBattle(shared, battle, m)
        case _                                        => Outcome(m)
    }
```

**No `OutMsg`.** A child never addresses its parent. When the parent needs to know *what
happened* inside the child, it reads the child's updated model after intercepting, as a web app
page intercepting a dropdown's `ChooseHighlighted` reads the dropdown's highlighted index. So the
world scene
records a bump into an enemy as `encounter: Option[EnemyId]`, and the end of the elder's last
dialogue as `Quest.Complete`; the root intercepts `Tick` / `Walk` / `Confirm` and reads them.

When the parent needs to tell a child something (a battle ended), it **calls the child's `update`
directly** with a child `Msg`, as the web app does (`Dropdown.update({ _tag: 'Close' }, ...)`):

```scala
WorldScene.update(shared, WorldScene.Msg.BattleWon(player, enemy), model.world)
```

**Navigation** is setting `route` in the root model (`route = SceneRoute.Battle`) and creating
the scene's model on the way in with its `init`, like the web app's `initPageModel` on a route
change. `init` returns `Outcome[Model]` (~ `[Model, Cmd]`), so each entry brings its effects:
every scene fades in and plays a sound, and the battle also announces "FIGHT!". Only the root
navigates, so scenes never know about each other.

### A battle, start to finish

Every arrow is a plain function call within the same frame.

```text
   Player         root program                                          world          battle
      |                 |                                                 |               |
  1   |----- Right ---->|                                                 |               |
      |                 | WorldSceneMsg(Walk(Right))                      |               |
  2   |                 |------------ WorldScene.update(Walk) ----------->|               |
      |                 |<----------- encounter = Some(slime) ------------|               |
  3   |                 |-+ intercept Walk: encounter?                    |               |
      |                 |<+ BattleScene.init, route = SceneRoute.Battle   |               |
  4   |----- Space ---->|                                                 |               |
      |                 | BattleSceneMsg(ActionMenuMsg(Choose))           |               |
  5   |                 |---------------------- BattleScene.update ---------------------->|
      |                 |                                                 |               |-+ ActionMenu.update, then
      |                 |                                                 |               |<+ intercept Choose: perform
      |                 |      ... turn after turn ...                    |               |
  6   |----- Space ---->|                                                 |               |
      |                 | BattleSceneMsg(Continue)                        |               |
  7   |                 |---------------------- BattleScene.update ---------------------->|
  8   |                 |-+ intercept Continue: phase == Won              |               |
      |                 |<+ route = SceneRoute.World, battle = None       |               |
  9   |                 |--------- WorldScene.update(BattleWon) --------->|               |
      |                 |                                                 |               |
```

### Navigation between scenes

```text
                  TitleScene.Msg.Start
        +-------+  (root resets the game)  +-------+  Tick / Walk with an   +--------+
   ---->| Title |------------------------->| World |----------------------->| Battle |
        +-------+                          +-------+  encounter             +--------+
            ^                                |   ^                              |  |
            |                                |   |   Continue: Won or Escaped   |  |
            |                                |   +------------------------------+  |
            |                  Confirm, with |                                     |
            |               quest Complete   v                                     |
            |     EndScene.Msg.Continue   +-----+     Continue: Lost               |
            +-----------------------------| End |<---------------------------------+
                                          +-----+
```

Every transition is decided in the root `Update.scala`, exactly where the web app's root
`update.ts` handles route changes.

---

## 6. The root model and scene models

```ts
// TEA: the route, plus page models in Option (only the active one is Some)
readonly settingsModel: Option<Settings.Model>
```

```scala
// Game: Type.scala
final case class Model(
    route: SceneRoute,                 // which scene shows
    title: TitleScene.Model,
    world: Option[WorldScene.Model],   // Some from a new game on; kept during battles
    battle: Option[BattleScene.Model], // Some while a battle is in progress
    end: Option[EndScene.Model]        // Some once the game has ended
)
```

```text
   root Model
  +-------------------------------------+
  | route  : SceneRoute                 |---> picks the scene: subscriptions, update, ui
  | title  : TitleScene.Model           |---> SceneRoute.Title
  | world  : Option[WorldScene.Model]   |---> SceneRoute.World   (WorldScene.init on a new game)
  | battle : Option[BattleScene.Model]  |---> SceneRoute.Battle  (BattleScene.init on an encounter)
  | end    : Option[EndScene.Model]     |---> SceneRoute.End     (EndScene.init when the game ends)
  +-------------------------------------+
```

The root unwraps a scene's model, calls the scene, and puts the result back, the same as the web
app's root does with `pageModel.settingsModel`.

---

## 7. ui (TEA's view)

```ts
// TEA: component.tsx returns JSX
const SettingsView = (props: Props) => <div className="...">...</div>
```

```scala
// Game: UI.scala returns a scene description
def ui(shared: Shared, model: Model): SceneUpdateFragment
```

Both are pure functions from model to a description of what to show, which the runtime renders
(React diffs it; Indigo simply redraws it every frame). A `SceneUpdateFragment` is a set of
**layers** (`world`, `ui`, `fps`, declared in order by `mainUI` in `Main.scala`) each holding
nodes: `Quad`, `Shape`, `Text`, `Group`, `Graphic`, `CloneTiles`. Think of layers as z-index
stacking contexts and `Group` as a positioned `<div>`.

UI that belongs to one scene lives in its `subui/`, in one of two shapes, the same two shapes
as components in the web app:

**Stateless ("dumb") sub-UI:** one file, one module, one function named after it. The `object`
makes the file its own module, and the consumer imports just the function:

```scala
// scene/battlescene/subui/BattleLogUI.scala
package game.scene.battlescene.subui
object BattleLogUI:                               // ~ module Game.Scene.BattleScene.Subui.BattleLogUI
  def battleLogUI(model: Model): Batch[SceneNode] = ...

// scene/battlescene/UI.scala
import game.scene.battlescene.subui.BattleLogUI.battleLogUI   // ~ import ...BattleLogUI (battleLogUI)
battleLogUI(model)
```

Others: `StatusPanelUI`, `HudUI`, `DialogueBoxUI`, `ToastBannerUI`, `EntitiesUI`, `TerrainUI`.

**Stateful sub-UI:** a TEA module folder of its own, wired into the scene the usual TEA way, like
`component/dropdown/` inside a page. Example: `scene/battlescene/subui/actionmenu/` (`Type`,
`Update`, `Subscription`, `UI`) owns the menu cursor. The battle keeps `menu: ActionMenu.Model`
in its model, wraps its messages as `ActionMenuMsg(subMsg)`, forwards input in `subscriptions`,
delegates in `update` and intercepts `Choose` (reading `selectedAction(m.menu)`), and draws it
with `ActionMenu.ui(...)`: the same pattern as root → scene, one level down.

Naming rules for all of this (e.g. every function returning a drawing type ends in `UI`) are in
[code-convention.md](code-convention.md).

Reusable UI used by several scenes lives in `ui/`, in the same `XxxUI` module shape: e.g.
`import game.ui.PanelUI.panelUI`, `import game.ui.LabelUI.{labelUI, centeredLabelUI}`. A module
can hold a few related functions (`LabelUI`, `CharacterSpriteUI`) when they share helpers.

How the world scene's UI is assembled, bottom layer first:

```text
   drawn last (on top)
   +--------------+------------------------------------------+
   | fps layer    | FPSCounter                               |
   +--------------+------------------------------------------+
   | ui layer     | hudUI                                    |   fixed to the screen
   |              | dialogueBoxUI or toastBannerUI           |
   +--------------+------------------------------------------+
   | world layer  | backdrop Quad                            |   moves with the camera
   |              | terrainUI   (CloneTiles: static, cached) |
   |              |             (CloneTiles, animated water) |
   |              | entitiesUI  (sprites, sorted by y)       |
   +--------------+------------------------------------------+
   drawn first (at the back)
```

---

## 8. Where the analogy stops

- **Time is a first-class input.** Web UIs are mostly idle between events; a game updates every
  frame. Anything animated is a pure function of `shared.now` (see `blinkingLabelUI`, sprite
  bobbing, tile tweening via `Step`), not stored animation state.
- **No HTTP, no `RemoteData`.** Nothing is loaded asynchronously after boot; assets are loaded by
  Indigo before the first frame (`Program.boot`).
- **Rendering cost matters.** React re-renders are cheap to reason about; here every node is
  drawn every frame. Indigo handles a few hundred ordinary nodes per frame, so large repeated
  things use instancing (the map is `CloneTiles`, see `scene/worldscene/subui/TerrainUI.scala`).
- **Layout is in pixels.** There's no CSS; fixed screens are designed at 480×320 game pixels and
  centred (`ui/ScreenUI.scala`), and the world scene reads the real viewport from `Shared`.

---

## 9. Recipe: adding a scene

1. `scene/<name>/Type.scala` — `Model`, `Msg`.
2. `scene/<name>/Update.scala` — `init(shared, ...): Outcome[Model]` (with its entry effects, e.g.
   a sound and `enteredAt` for `fadeInUI`), `update(shared, msg, model): Outcome[Model]`.
3. `scene/<name>/Subscription.scala` — engine events → `Option[Msg]`.
4. `scene/<name>/UI.scala` — `ui(shared, model): SceneUpdateFragment`.
5. Root: add a `SceneRoute` case, a model field and an `XSceneMsg(subMsg)` case in `Type.scala`, and a
   branch in `Subscription.scala`, `Update.scala` (delegate + intercept) and `mainUI`'s `sceneUI`.
   The compiler points at every `match` you missed.
6. Tests mirror the source tree: `test/src/game/scene/<name>/UpdateTests.scala`.

---

## 10. What's official Indigo, and what's our convention

Indigo ships a scene system: `Scene` objects run by a `SceneManager`, each with a `modelLens` into
the game model, navigating with `SceneEvent.JumpTo`, and talking to each other through broadcast
events. We don't use it. In it, the game-level `updateModel` never calls a scene's update (the
`SceneManager` runs the active scene after the game-level update, on its own), so a parent can't
delegate to or intercept its children, and scenes can only talk upwards through events that arrive
a frame later.

Instead the root routes scenes itself, like the web app's root. Indigo allows this: its scene guide
notes you could build your own scene system on top of the engine, and its own perf sandbox runs
everything from the game-level `updateModel` with `NonEmptyBatch(Scene.empty)` and
`initialScene = None`, which is what `Program` does. It is our convention, not Indigo's.

| Official Indigo                                                | What we do                                              |
| -------------------------------------------------------------- | ------------------------------------------------------- |
| `Game`: `boot`, `setup`, `initialModel`, `updateModel`, `present` | Used as is; root pieces named `Program` / `Type` / `Update` / `Subscription` / `mainUI` |
| `Scene` objects, `modelLens`, `SceneEvent.JumpTo`              | Not used: `SceneRoute` in the root model, set by the root `update` |
| Scenes talk through broadcast `GlobalEvent`s                   | Child msg interception; events are only effects (engine ones, or a scene's own events) |
| Scenes pattern-match raw `GlobalEvent`s in `updateModel`       | `subscriptions` first turns them into a scene `Msg`     |
| `context.frame.time`, `context.frame.dice`                     | Bundled as `Shared` and passed to `update` / `ui`       |

What we give up: per-scene `subSystems` and `eventFilters`, and the `SceneEvent` lifecycle events
(e.g. `SceneChange`). None of them were used. Performance is unchanged: the same scene code runs
each frame, reached through one `match` on `route` instead of the `SceneManager`.
