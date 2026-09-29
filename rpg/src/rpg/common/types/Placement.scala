package rpg.common.types

import indigo.*

/** Everything placed on a map by the level DSL, other than the terrain itself. */
enum Placement derives CanEqual:
  case PlayerStart(at: Point)
  case Elder(at: Point)
  case Slime(at: Point)
  case SlimeKing(at: Point)
  case Chest(at: Point)
