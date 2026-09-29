package game.common.util

import indigo.*
import game.common.Types.*
import game.common.util.Character.*

import scala.util.chaining.*

class CharacterTests extends munit.FunSuite {

  test("gaining enough XP levels the player up, possibly more than once") {
    val (once, n1) = gainXp(10)(Player.initial(Point.zero))
    assertEquals((once.level, once.xp, n1), (2, 0, 1))
    assertEquals(once.stats.hp, once.stats.maxHp)

    val (twice, n2) = gainXp(35)(Player.initial(Point.zero))
    assertEquals((twice.level, twice.xp, n2), (3, 5, 2))
  }

  test("damage and healing stay within 0 and max HP") {
    val stats = Stats(hp = 10, maxHp = 20, attack = 1, defense = 1)
    assertEquals(damage(50)(stats).hp, 0)
    assert(stats.pipe(damage(50)).pipe(isDead))
    assertEquals(heal(50)(stats).hp, 20)
  }
}
