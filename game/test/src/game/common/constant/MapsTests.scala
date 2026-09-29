package game.common.constant

import indigo.*
import game.common.Types.*
import game.common.util.TileMap.*
import game.scene.WorldScene

import scala.annotation.tailrec

class MapsTests extends munit.FunSuite {

  test("every overworld row has the same width") {
    val map = Maps.overworld.map
    assertEquals(map.tiles.length / map.size.height, map.size.width)
  }

  test("the overworld has exactly one player, elder and slime king") {
    val ps = Maps.overworld.placements
    assertEquals(ps.collect { case p: Placement.PlayerStart => p }.length, 1)
    assertEquals(ps.collect { case p: Placement.Elder => p }.length, 1)
    assertEquals(ps.collect { case p: Placement.SlimeKing => p }.length, 1)
  }

  test("the elder, the slime king and every chest can be reached from the start") {
    val world = WorldScene.init(Shared.atStartup(Dice.loaded(1)), Maps.overworld).unsafeGet
    val map   = world.map

    @tailrec
    def flood(frontier: List[Point], seen: Set[Point]): Set[Point] =
      frontier match
        case Nil => seen
        case p :: rest =>
          val next =
            Direction.all.toList
              .map(d => p + d.delta)
              .filter(n => isWalkable(n)(map) && !seen.contains(n))
          flood(next ++ rest, seen ++ next)

    val reachable = flood(List(world.player.position), Set(world.player.position))

    def adjacentReachable(target: Point): Boolean =
      Direction.all.exists(d => reachable.contains(target + d.delta))

    assert(adjacentReachable(world.elder.position), "elder is unreachable")

    val king = world.enemies.find(_.kind == EnemyKind.SlimeKing).map(_.position)
    assert(king.exists(adjacentReachable), "slime king is unreachable")
    assert(world.chests.forall(c => adjacentReachable(c.position)), "a chest is unreachable")
  }
}
