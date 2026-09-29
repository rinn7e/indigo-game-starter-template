package rpg.common.util

import indigo.*
import rpg.common.Types.*

object LevelParser:

  /** Parses an ASCII level. Each character is one tile:
    *
    * `.` grass, `,` flowers, `=` path, `_` floor, `~` water, `T` tree, `#` rock, `H` wall, `R`
    * roof, `@` player start, `E` elder, `s` slime, `K` slime king, `c` chest (entities stand on
    * grass, except the elder who stands on floor).
    */
  def parse(rows: Batch[String]): Level =
    val width  = rows.map(_.length).foldLeft(0)(Math.max)
    val height = rows.length

    val cells: Batch[(Point, Char)] =
      Batch.fromIndexedSeq(
        for {
          y <- 0 until height
          x <- 0 until width
        } yield Point(x, y) -> rows(y).lift(x).getOrElse('#')
      )

    val tiles = cells.map { case (_, c) => charToTile(c) }

    val placements = cells.flatMap { case (pt, c) =>
      c match
        case '@' => Batch(Placement.PlayerStart(pt))
        case 'E' => Batch(Placement.Elder(pt))
        case 's' => Batch(Placement.Slime(pt))
        case 'K' => Batch(Placement.SlimeKing(pt))
        case 'c' => Batch(Placement.Chest(pt))
        case _   => Batch.empty
    }

    Level(TileMap(Size(width, height), tiles), placements)

  private def charToTile(c: Char): Tile =
    c match
      case ','       => Tile.Flowers
      case '='       => Tile.Path
      case '_' | 'E' => Tile.Floor
      case '~'       => Tile.Water
      case 'T'       => Tile.Tree
      case '#'       => Tile.Rock
      case 'H'       => Tile.Wall
      case 'R'       => Tile.Roof
      case _         => Tile.Grass
