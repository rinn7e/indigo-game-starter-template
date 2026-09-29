package game.scene.endscene

import indigo.*
import game.common.util.Input
import game.scene.endscene.Type.*

object Subscription:

  def subscriptions(model: Model, keyboard: Keyboard): GlobalEvent => Option[Msg] =
    case e if Input.isConfirm(e) => Some(Msg.Continue)
    case _                       => None
