package rpg.scene.battlescene

import indigo.*
import rpg.common.util.Input
import rpg.scene.battlescene.subui.ActionMenu
import rpg.scene.battlescene.Type.*

object Subscription:

  /** Turns engine input (frame ticks, key presses) into this scene's messages. Menu input is handed
    * to the action menu's own subscriptions, and its messages wrapped, as with any TEA child.
    */
  def subscriptions(model: Model, keyboard: Keyboard): GlobalEvent => Option[Msg] =
    case FrameTick =>
      Some(Msg.Tick)

    case BattleEvent.FightStarted =>
      Some(Msg.FightStarted)

    case e if isOver(model.phase) && Input.isConfirm(e) =>
      Some(Msg.Continue)

    case e =>
      ActionMenu.subscriptions(model.menu, keyboard)(e).map(Msg.ActionMenuMsg(_))

  private def isOver(phase: Phase): Boolean =
    phase match
      case Phase.Won | Phase.Lost | Phase.Escaped => true
      case Phase.PlayerTurn | Phase.EnemyTurn(_)  => false
