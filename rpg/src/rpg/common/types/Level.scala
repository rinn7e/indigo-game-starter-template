package rpg.common.types

import indigo.*

final case class Level(map: TileMap, placements: Batch[Placement])
