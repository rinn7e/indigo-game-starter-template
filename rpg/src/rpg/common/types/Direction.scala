package rpg.common.types

import indigo.*

enum Direction derives CanEqual:
  case Up, Down, Left, Right

  def delta: Point =
    this match
      case Up    => Point(0, -1)
      case Down  => Point(0, 1)
      case Left  => Point(-1, 0)
      case Right => Point(1, 0)

object Direction:

  val all: Batch[Direction] =
    Batch(Up, Down, Left, Right)
