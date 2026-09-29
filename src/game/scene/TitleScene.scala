package game.scene

/** The title scene as one module. Re-exports `titlescene/`, so the parent writes
  * `import game.scene.TitleScene` and `TitleScene.update`, `TitleScene.Msg`, `TitleScene.ui` (~
  * Haskell's `import qualified Game.Scene.TitleScene as TitleScene`, or an index.ts barrel).
  */
object TitleScene:
  export titlescene.Type.*
  export titlescene.Update.*
  export titlescene.Subscription.*
  export titlescene.UI.*
