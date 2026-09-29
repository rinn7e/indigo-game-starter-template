package rpg.common.types

enum Tile derives CanEqual:
  case Grass, Flowers, Path, Floor, Water, Tree, Rock, Wall, Roof

  def walkable: Boolean =
    this match
      case Grass | Flowers | Path | Floor    => true
      case Water | Tree | Rock | Wall | Roof => false
