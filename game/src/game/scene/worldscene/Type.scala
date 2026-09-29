package game.scene.worldscene

import indigo.*
import game.common.Types.*

object Type:

  enum DialogueEnd derives CanEqual:
    case Nothing, StartQuest, Win

  final case class OpenDialogue(dialogue: Dialogue, onEnd: DialogueEnd) derives CanEqual

  final case class Model(
      map: TileMap,
      player: Player,
      enemies: Batch[Enemy],
      chests: Batch[Chest],
      elder: Npc,
      quest: Quest,
      dialogue: Option[OpenDialogue],
      toast: Option[Toast],
      // The enemy the player has run into. The parent starts the battle when it sees this, and the
      // battle result (`BattleWon` / `BattleEscaped`) clears it.
      encounter: Option[EnemyId],
      // When the player last entered this scene (a new game, or back from a battle): drives the
      // fade-in.
      enteredAt: Seconds
  ) derives CanEqual

  enum Msg derives CanEqual:
    /** Every frame, with the direction key being held down (if any). */
    case Tick(held: Option[Direction])

    /** A direction key was pressed. Taps can begin and end within a single frame, so key presses
      * move the player too, not only held keys.
      */
    case Walk(direction: Direction)

    case Confirm

    // Sent by the parent when a battle started from this scene (see `encounter`) ends.
    case BattleWon(player: Player, enemy: Enemy)
    case BattleEscaped(player: Player, enemy: Enemy)
