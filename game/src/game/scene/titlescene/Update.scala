package game.scene.titlescene

import indigo.*
import game.common.Types.Shared
import game.common.asset.Sfx
import game.scene.titlescene.Type.*

object Update:

  // -----------------------------------------------------------------
  // Init
  // -----------------------------------------------------------------

  /** Entering the title: fade in (`enteredAt`) and play its chime (an effect, like a `Cmd`). */
  def init(shared: Shared): Outcome[Model] =
    Outcome(Model(enteredAt = shared.now)).addGlobalEvents(Sfx.title)

  // -----------------------------------------------------------------
  // Update
  // -----------------------------------------------------------------

  def update(shared: Shared, msg: Msg, model: Model): Outcome[Model] =
    msg match
      case Msg.Start =>
        Outcome(model)
