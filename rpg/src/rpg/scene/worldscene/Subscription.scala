package rpg.scene.worldscene

import indigo.*
import rpg.common.util.Input
import rpg.scene.worldscene.Type.*

object Subscription:

  /** Turns engine input (frame ticks, key presses) into this scene's messages. */
  def subscriptions(model: Model, keyboard: Keyboard): GlobalEvent => Option[Msg] =
    case FrameTick => Some(Msg.Tick(Input.heldDirection(keyboard)))
    case KeyboardEvent.KeyDown(key) if Input.direction(key).isDefined =>
      Input.direction(key).map(Msg.Walk(_))
    case e if Input.isConfirm(e) => Some(Msg.Confirm)
    case _                       => None
