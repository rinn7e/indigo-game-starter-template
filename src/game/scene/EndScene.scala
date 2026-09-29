package game.scene

/** The end scene as one module. Re-exports `endscene/`, so the parent writes
  * `import game.scene.EndScene` and `EndScene.update`, `EndScene.Msg`, `EndScene.ui` (~ Haskell's
  * `import qualified Game.Scene.EndScene as EndScene`, or an index.ts barrel).
  */
object EndScene:
  export endscene.Type.*
  export endscene.Update.*
  export endscene.Subscription.*
  export endscene.UI.*
