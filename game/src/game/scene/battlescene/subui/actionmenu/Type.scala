package game.scene.battlescene.subui.actionmenu

import indigo.*

object Type:

  enum Action derives CanEqual:
    case Attack, Potion, Run

    def label: String =
      this match
        case Attack => "ATTACK"
        case Potion => "POTION"
        case Run    => "RUN"

  object Action:
    val menu: Batch[Action] = Batch(Attack, Potion, Run)

  /** The menu's own state: which option the cursor is on. */
  final case class Model(selected: Int) derives CanEqual

  enum Msg derives CanEqual:
    case CursorUp
    case CursorDown

    /** Intercepted by the parent, which performs the selected action. */
    case Choose
