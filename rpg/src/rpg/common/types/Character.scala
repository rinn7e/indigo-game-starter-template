package rpg.common.types

import indigo.*

object Character:

  final case class Stats(hp: Int, maxHp: Int, attack: Int, defense: Int) derives CanEqual

  /** A one-tile move in progress, used to tween sprites between grid positions. */
  final case class Step(from: Point, startedAt: Seconds) derives CanEqual

  object Step:
    val duration: Seconds = Seconds(0.16)

  final case class Player(
      position: Point,
      facing: Direction,
      step: Option[Step],
      stats: Stats,
      level: Int,
      xp: Int,
      gold: Int,
      potions: Int,
      safeUntil: Seconds
  ) derives CanEqual

  object Player:

    def initial(at: Point): Player =
      Player(
        position = at,
        facing = Direction.Down,
        step = None,
        stats = Stats(hp = 24, maxHp = 24, attack = 6, defense = 2),
        level = 1,
        xp = 0,
        gold = 0,
        potions = 1,
        safeUntil = Seconds.zero
      )

    val potionHeal: Int = 15

  enum EnemyKind derives CanEqual:
    case Slime, SlimeKing

    def name: String =
      this match
        case Slime     => "SLIME"
        case SlimeKing => "SLIME KING"

    def stats: Stats =
      this match
        case Slime     => Stats(hp = 12, maxHp = 12, attack = 5, defense = 1)
        case SlimeKing => Stats(hp = 60, maxHp = 60, attack = 10, defense = 4)

    def xpReward: Int =
      this match
        case Slime     => 6
        case SlimeKing => 50

    def goldReward: Int =
      this match
        case Slime     => 5
        case SlimeKing => 100

    def wanders: Boolean =
      this match
        case Slime     => true
        case SlimeKing => false

  final case class EnemyId(value: Int) derives CanEqual

  final case class Enemy(
      id: EnemyId,
      kind: EnemyKind,
      position: Point,
      step: Option[Step],
      nextMoveAt: Seconds
  ) derives CanEqual
