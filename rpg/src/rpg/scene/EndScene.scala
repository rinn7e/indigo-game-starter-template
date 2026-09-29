package rpg.scene

/** The end scene as one module. Re-exports `endscene/`, so the parent writes
  * `import rpg.scene.EndScene` and `EndScene.update`, `EndScene.Msg`, `EndScene.ui` (~ Haskell's
  * `import qualified Rpg.Scene.EndScene as EndScene`, or an index.ts barrel).
  */
object EndScene:
  export endscene.Type.*
  export endscene.Update.*
  export endscene.Subscription.*
  export endscene.UI.*
