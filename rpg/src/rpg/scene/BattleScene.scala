package rpg.scene

/** The battle scene as one module. Re-exports `battlescene/`, so the parent writes
  * `import rpg.scene.BattleScene` and `BattleScene.update`, `BattleScene.Msg`, `BattleScene.ui` (~
  * Haskell's `import qualified Rpg.Scene.BattleScene as BattleScene`, or an index.ts barrel).
  */
object BattleScene:
  export battlescene.Type.*
  export battlescene.Update.*
  export battlescene.Subscription.*
  export battlescene.UI.*
