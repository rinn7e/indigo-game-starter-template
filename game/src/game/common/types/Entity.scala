package game.common.types

import indigo.*

object Entity:

  final case class Chest(position: Point, opened: Boolean) derives CanEqual

  final case class Npc(name: String, position: Point) derives CanEqual

  enum Quest derives CanEqual:
    case NotStarted, SlayTheKing, KingDefeated

    /** The elder has thanked the hero: the game is won. */
    case Complete

  final case class Dialogue(speaker: String, lines: Batch[String], index: Int) derives CanEqual

  object Dialogue:
    def apply(speaker: String, lines: String*): Dialogue =
      Dialogue(speaker, Batch.fromSeq(lines), 0)

  final case class Toast(message: String, until: Seconds) derives CanEqual
