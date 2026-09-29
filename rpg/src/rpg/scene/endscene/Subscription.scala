package rpg.scene.endscene

import indigo.*
import rpg.common.util.Input
import rpg.scene.endscene.Type.*

object Subscription:

  def subscriptions(model: Model, keyboard: Keyboard): GlobalEvent => Option[Msg] =
    case e if Input.isConfirm(e) => Some(Msg.Continue)
    case _                       => None
