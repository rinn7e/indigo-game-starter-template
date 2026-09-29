package rpg.common.util

import indigo.*
import rpg.common.Types.*

/** Queries on a tile map. The map comes last (`tileAt(pt)(map)`), Elm / Haskell style. */
object TileMap:

  def inBounds(pt: Point)(map: TileMap): Boolean =
    pt.x >= 0 && pt.y >= 0 && pt.x < map.size.width && pt.y < map.size.height

  def tileAt(pt: Point)(map: TileMap): Tile =
    if inBounds(pt)(map) then map.tiles(pt.y * map.size.width + pt.x) else Tile.Rock

  def isWalkable(pt: Point)(map: TileMap): Boolean =
    tileAt(pt)(map).walkable
