package rpg.scene.titlescene

import indigo.*
import rpg.common.Types.Shared
import rpg.common.asset.Sfx
import rpg.scene.titlescene.Type.*

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
