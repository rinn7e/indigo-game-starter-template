package rpg.common.util

import indigo.*
import rpg.common.Types.*
import rpg.common.util.TileMap.*

class LevelParserTests extends munit.FunSuite {

  test("parses tiles and placements from ASCII") {
    val level = LevelParser.parse(Batch("#@.", "~Ts"))

    assertEquals(level.map.size, Size(3, 2))
    assertEquals(tileAt(Point(0, 0))(level.map), Tile.Rock)
    assertEquals(tileAt(Point(1, 0))(level.map), Tile.Grass)
    assertEquals(tileAt(Point(0, 1))(level.map), Tile.Water)
    assertEquals(tileAt(Point(1, 1))(level.map), Tile.Tree)
    assertEquals(
      level.placements,
      Batch(Placement.PlayerStart(Point(1, 0)), Placement.Slime(Point(2, 1)))
    )
  }

  test("out of bounds is solid rock") {
    val map = LevelParser.parse(Batch("..")).map
    assertEquals(tileAt(Point(-1, 0))(map), Tile.Rock)
    assert(!isWalkable(Point(5, 5))(map))
  }
}
