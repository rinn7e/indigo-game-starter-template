package rpg.scene.endscene

import indigo.*
import rpg.common.Types.Ending

object Type:

  final case class Model(ending: Ending, level: Int, gold: Int, enteredAt: Seconds) derives CanEqual

  enum Msg derives CanEqual:
    /** Intercepted by the parent, which goes back to the title. */
    case Continue
