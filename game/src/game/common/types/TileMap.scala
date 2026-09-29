package game.common.types

import indigo.*

final case class TileMap(size: Size, tiles: Batch[Tile]) derives CanEqual
