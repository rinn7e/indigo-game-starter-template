package rpg.scene

/** The world scene as one module. Re-exports `worldscene/`, so the parent writes
  * `import rpg.scene.WorldScene` and `WorldScene.update`, `WorldScene.Msg`, `WorldScene.ui` (~
  * Haskell's `import qualified Rpg.Scene.WorldScene as WorldScene`, or an index.ts barrel).
  */
object WorldScene:
  export worldscene.Type.*
  export worldscene.Update.*
  export worldscene.Subscription.*
  export worldscene.UI.*
