package rpg.scene

/** The title scene as one module. Re-exports `titlescene/`, so the parent writes
  * `import rpg.scene.TitleScene` and `TitleScene.update`, `TitleScene.Msg`, `TitleScene.ui` (~
  * Haskell's `import qualified Rpg.Scene.TitleScene as TitleScene`, or an index.ts barrel).
  */
object TitleScene:
  export titlescene.Type.*
  export titlescene.Update.*
  export titlescene.Subscription.*
  export titlescene.UI.*
