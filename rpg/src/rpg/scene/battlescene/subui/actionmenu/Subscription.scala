package rpg.scene.battlescene.subui.actionmenu

import indigo.*
import rpg.common.Types.Direction
import rpg.common.util.Input
import rpg.scene.battlescene.subui.actionmenu.Type.*

object Subscription:

  /** Turns key presses into the menu's messages. */
  def subscriptions(model: Model, keyboard: Keyboard): GlobalEvent => Option[Msg] =
    case KeyboardEvent.KeyDown(key) =>
      Input.direction(key) match
        case Some(Direction.Up)   => Some(Msg.CursorUp)
        case Some(Direction.Down) => Some(Msg.CursorDown)
        case _                    => None

    case e if Input.isConfirm(e) =>
      Some(Msg.Choose)

    case _ =>
      None
