package rpg.scene.battlescene

import indigo.*
import rpg.common.Types.*
import rpg.common.util.Character.*
import rpg.scene.battlescene.subui.ActionMenu
import rpg.scene.battlescene.subui.ActionMenu.Action
import rpg.scene.battlescene.Type.*
import rpg.scene.battlescene.Subscription.*
import rpg.scene.battlescene.Update.*

class UpdateTests extends munit.FunSuite {

  val now: Seconds = Seconds(5)

  def shared(dice: Dice): Shared = Shared(now, dice, Size(480, 320))

  val slime: Enemy =
    Enemy(EnemyId(0), EnemyKind.Slime, Point.zero, None, Seconds.zero)

  val king: Enemy =
    Enemy(EnemyId(1), EnemyKind.SlimeKing, Point.zero, None, Seconds.zero)

  val battle: Model =
    init(Shared(Seconds.zero, Dice.loaded(1), Size(480, 320)), Player.initial(Point.zero), slime).unsafeGet

  test("damage is attack minus defence with a small spread, never below 1") {
    assertEquals(rollDamage(10, 3, Dice.loaded(2)), 7)
    assertEquals(rollDamage(10, 3, Dice.loaded(4)), 9)
    assertEquals(rollDamage(1, 50, Dice.loaded(4)), 1)
  }

  test("attacking hurts the enemy and hands the turn over") {
    val after = perform(Action.Attack, shared(Dice.loaded(2)), battle)

    assertEquals(after.enemyStats.hp, EnemyKind.Slime.stats.hp - 5)
    assertEquals(after.phase, Phase.EnemyTurn(now))
  }

  test("reducing the enemy to 0 HP wins the battle") {
    val weak  = battle.copy(enemyStats = battle.enemyStats.copy(hp = 1))
    val after = perform(Action.Attack, shared(Dice.loaded(2)), weak)

    assertEquals(after.enemyStats.hp, 0)
    assertEquals(after.phase, Phase.Won)
  }

  test("the enemy strikes back after a delay") {
    val waiting = battle.copy(phase = Phase.EnemyTurn(now))

    val tooSoon =
      update(shared(Dice.loaded(2)).copy(now = now + Seconds(0.1)), Msg.Tick, waiting).unsafeGet
    assertEquals(tooSoon, waiting)

    val struck =
      update(shared(Dice.loaded(2)).copy(now = now + Seconds(1)), Msg.Tick, waiting).unsafeGet
    assertEquals(struck.player.stats.hp, battle.player.stats.hp - 3)
    assertEquals(struck.phase, Phase.PlayerTurn)
  }

  test("a potion heals up to max HP and is consumed") {
    val hurt  = battle.copy(player = battle.player.copy(stats = damage(10)(battle.player.stats)))
    val after = perform(Action.Potion, shared(Dice.loaded(1)), hurt)

    assertEquals(after.player.stats.hp, battle.player.stats.maxHp)
    assertEquals(after.player.potions, battle.player.potions - 1)
  }

  test("you cannot drink a potion you do not have") {
    val empty = battle.copy(player = battle.player.copy(potions = 0))
    val after = perform(Action.Potion, shared(Dice.loaded(1)), empty)

    assertEquals(after.phase, Phase.PlayerTurn)
  }

  test("you can escape from slimes but never from the slime king") {
    assertEquals(perform(Action.Run, shared(Dice.loaded(4)), battle).phase, Phase.Escaped)

    val boss = battle.copy(enemy = king, enemyStats = king.kind.stats)
    assertEquals(perform(Action.Run, shared(Dice.loaded(4)), boss).phase, Phase.EnemyTurn(now))
  }

  test("input is ignored just after the battle starts") {
    val fresh = init(shared(Dice.loaded(1)), Player.initial(Point.zero), slime).unsafeGet
    val moved =
      update(
        shared(Dice.loaded(1)).copy(now = now + Seconds(0.1)),
        Msg.ActionMenuMsg(ActionMenu.Msg.CursorDown),
        fresh
      ).unsafeGet

    assertEquals(ActionMenu.selectedAction(moved.menu), Action.Attack)
  }

  test("menu messages move the menu's own cursor") {
    val up =
      update(shared(Dice.loaded(1)), Msg.ActionMenuMsg(ActionMenu.Msg.CursorUp), battle).unsafeGet
    assertEquals(ActionMenu.selectedAction(up.menu), Action.Run)
  }

  test("choosing from the menu performs that action") {
    val attacked =
      update(shared(Dice.loaded(2)), Msg.ActionMenuMsg(ActionMenu.Msg.Choose), battle).unsafeGet

    assertEquals(attacked.enemyStats.hp, EnemyKind.Slime.stats.hp - 5)
    assertEquals(attacked.phase, Phase.EnemyTurn(now))
  }

  test("the menu ignores input when it isn't the player's turn") {
    val waiting = battle.copy(phase = Phase.EnemyTurn(now))
    val result =
      update(shared(Dice.loaded(1)), Msg.ActionMenuMsg(ActionMenu.Msg.CursorDown), waiting).unsafeGet

    assertEquals(result, waiting)
  }

  test("continuing is left to the parent: the battle itself doesn't change") {
    val won = battle.copy(phase = Phase.Won)
    assertEquals(update(shared(Dice.loaded(1)), Msg.Continue, won).unsafeGet, won)
  }

  test("the fight-started event emitted on entry comes back as a message") {
    val sub = subscriptions(battle, Keyboard.default)
    assertEquals(sub(BattleEvent.FightStarted), Some(Msg.FightStarted))
  }

  test("the fight start is handled even during the input grace period") {
    val fresh     = init(shared(Dice.loaded(1)), Player.initial(Point.zero), slime).unsafeGet
    val started = update(shared(Dice.loaded(1)), Msg.FightStarted, fresh).unsafeGet

    assertEquals(started.fightStartedAt, Some(now))
  }
}
