package game.common.util

import indigo.*
import game.common.Types.*

/** Helpers for stats and the player. The subject comes last (`damage(amount)(stats)`), Elm /
  * Haskell style, so currying them later is mechanical.
  */
object Character:

  // --- Stats ----------------------------------------------------------

  def isDead(stats: Stats): Boolean =
    stats.hp <= 0

  def damage(amount: Int)(stats: Stats): Stats =
    stats.copy(hp = Math.max(0, stats.hp - amount))

  def heal(amount: Int)(stats: Stats): Stats =
    stats.copy(hp = Math.min(stats.maxHp, stats.hp + amount))

  // --- Player ---------------------------------------------------------

  def facingTile(player: Player): Point =
    player.position + player.facing.delta

  def xpToNextLevel(player: Player): Int =
    player.level * 10

  def isMoving(now: Seconds)(player: Player): Boolean =
    player.step.exists(s => now < s.startedAt + Step.duration)

  def isSafe(now: Seconds)(player: Player): Boolean =
    now < player.safeUntil

  /** Adds XP, levelling up as many times as the XP allows. Returns the new player and the number of
    * levels gained.
    */
  def gainXp(amount: Int)(player: Player): (Player, Int) =
    def loop(p: Player, gained: Int): (Player, Int) =
      if p.xp < xpToNextLevel(p) then (p, gained)
      else
        val maxHp = p.stats.maxHp + 6
        loop(
          p.copy(
            xp = p.xp - xpToNextLevel(p),
            level = p.level + 1,
            stats = Stats(maxHp, maxHp, p.stats.attack + 2, p.stats.defense + 1)
          ),
          gained + 1
        )

    loop(player.copy(xp = player.xp + amount), 0)
