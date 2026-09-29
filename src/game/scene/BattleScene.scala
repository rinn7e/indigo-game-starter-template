package game.scene

/** The battle scene as one module. Re-exports `battlescene/`, so the parent writes
  * `import game.scene.BattleScene` and `BattleScene.update`, `BattleScene.Msg`, `BattleScene.ui` (~
  * Haskell's `import qualified Game.Scene.BattleScene as BattleScene`, or an index.ts barrel).
  */
object BattleScene:
  export battlescene.Type.*
  export battlescene.Update.*
  export battlescene.Subscription.*
  export battlescene.UI.*
