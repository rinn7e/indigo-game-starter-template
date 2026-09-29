package rpg.common.util

import rpg.common.Types.*

object Dialogue:

  def currentLine(dialogue: Dialogue): String =
    dialogue.lines.lift(dialogue.index).getOrElse("")

  /** None when the conversation is over. */
  def advance(dialogue: Dialogue): Option[Dialogue] =
    if dialogue.index + 1 < dialogue.lines.length then
      Some(dialogue.copy(index = dialogue.index + 1))
    else None
