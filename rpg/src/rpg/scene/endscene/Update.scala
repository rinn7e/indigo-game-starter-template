package rpg.scene.endscene

import indigo.*
import rpg.common.Types.*
import rpg.common.asset.Sfx
import rpg.scene.endscene.Type.*

object Update:

  // -----------------------------------------------------------------
  // Init
  // -----------------------------------------------------------------

  /** Entering the end screen: fade in and play a fanfare or a lament (an effect, like a `Cmd`). */
  def init(shared: Shared, ending: Ending, player: Player): Outcome[Model] =
    val sound =
      ending match
        case Ending.Victory  => Sfx.victory
        case Ending.GameOver => Sfx.defeat

    Outcome(Model(ending, player.level, player.gold, enteredAt = shared.now))
      .addGlobalEvents(sound)

  // -----------------------------------------------------------------
  // Update
  // -----------------------------------------------------------------

  def update(shared: Shared, msg: Msg, model: Model): Outcome[Model] =
    msg match
      case Msg.Continue =>
        Outcome(model)
