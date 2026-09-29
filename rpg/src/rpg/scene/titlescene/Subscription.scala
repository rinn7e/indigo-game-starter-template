package rpg.scene.titlescene

import indigo.*
import rpg.common.util.Input
import rpg.scene.titlescene.Type.*

object Subscription:

  def subscriptions(model: Model, keyboard: Keyboard): GlobalEvent => Option[Msg] =
    case e if Input.isConfirm(e) => Some(Msg.Start)
    case _                       => None
