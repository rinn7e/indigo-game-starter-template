package rpg.scene.battlescene.subui

/** The battle action menu (a stateful sub-UI) as one module. Re-exports `actionmenu/`, so the
  * parent writes `import rpg.scene.battlescene.subui.ActionMenu` and `ActionMenu.update`,
  * `ActionMenu.Msg`, `ActionMenu.ui` (~ Haskell's
  * `import qualified Rpg.Scene.Battle.Subui.Actionmenu as ActionMenu`, or an index.ts barrel).
  */
object ActionMenu:
  export actionmenu.Type.*
  export actionmenu.Update.*
  export actionmenu.Subscription.*
  export actionmenu.UI.*
