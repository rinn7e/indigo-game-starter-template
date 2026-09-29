package game

import indigo.*
import game.Subscription.subscriptions
import game.Type.*
import game.Update.*
import game.common.Types.*
import game.common.asset.Sfx
import game.scene.BattleScene.Phase
import game.scene.{BattleScene, EndScene, TitleScene, WorldScene}

class UpdateTests extends munit.FunSuite {

  val shared: Shared = Shared(Seconds(3), Dice.loaded(1), Size(480, 320))

  val start: Model = init(shared).unsafeGet

  val inWorld: Model = update(shared, Msg.TitleSceneMsg(TitleScene.Msg.Start), start).unsafeGet

  val world: WorldScene.Model = inWorld.world.get

  val slime: Enemy = world.enemies.find(_.kind == EnemyKind.Slime).get

  /** The world after the player has run into the slime. */
  val metSlime: Model =
    inWorld.copy(world = Some(world.copy(encounter = Some(slime.id))))

  val enteringBattle: Outcome[Model] =
    update(shared, Msg.WorldSceneMsg(WorldScene.Msg.Tick(None)), metSlime)

  val inBattle: Model = enteringBattle.unsafeGet

  def battleIn(phase: Phase): Model =
    inBattle.copy(battle = inBattle.battle.map(_.copy(phase = phase)))

  test("the game starts on the title, playing its entry sound") {
    assertEquals(start.route, SceneRoute.Title)
    assertEquals(start.world, None)
    assertEquals(init(shared).unsafeGlobalEvents, Batch(Sfx.title))
  }

  test("starting from the title creates a new world and enters it") {
    val entering = update(shared, Msg.TitleSceneMsg(TitleScene.Msg.Start), start)

    assertEquals(entering.unsafeGet.route, SceneRoute.World)
    assertEquals(world.enteredAt, shared.now)
    assertEquals(entering.unsafeGlobalEvents, Batch(Sfx.world))
  }

  test("an encounter in the world opens the battle scene, with its entry effects") {
    assertEquals(inBattle.route, SceneRoute.Battle)
    assertEquals(inBattle.battle.map(_.enemy.id), Some(slime.id))
    assertEquals(enteringBattle.unsafeGlobalEvents, Batch(Sfx.battle, BattleScene.BattleEvent.FightStarted))
  }

  test("other world messages don't start a battle") {
    val result = update(shared, Msg.WorldSceneMsg(WorldScene.Msg.Confirm), inWorld).unsafeGet
    assertEquals(result.route, SceneRoute.World)
  }

  test("continuing after a win hands the result to the world and re-enters it") {
    val returning =
      update(shared, Msg.BattleSceneMsg(BattleScene.Msg.Continue), battleIn(Phase.Won))
    val result = returning.unsafeGet

    assertEquals(result.route, SceneRoute.World)
    assertEquals(result.battle, None)
    assertEquals(result.world.flatMap(_.encounter), None)
    assert(!result.world.exists(_.enemies.exists(_.id == slime.id)))
    assertEquals(returning.unsafeGlobalEvents, Batch(Sfx.world))
  }

  test("continuing after a defeat ends the game") {
    val ending =
      update(shared, Msg.BattleSceneMsg(BattleScene.Msg.Continue), battleIn(Phase.Lost))

    assertEquals(ending.unsafeGet.route, SceneRoute.End)
    assertEquals(ending.unsafeGet.end.map(_.ending), Some(Ending.GameOver))
    assertEquals(ending.unsafeGlobalEvents, Batch(Sfx.defeat))
  }

  test("continuing mid-battle does nothing") {
    val result =
      update(shared, Msg.BattleSceneMsg(BattleScene.Msg.Continue), battleIn(Phase.PlayerTurn)).unsafeGet
    assertEquals(result, battleIn(Phase.PlayerTurn))
  }

  test("a completed quest ends the game in victory") {
    val done   = inWorld.copy(world = Some(world.copy(quest = Quest.Complete)))
    val ending = update(shared, Msg.WorldSceneMsg(WorldScene.Msg.Confirm), done)

    assertEquals(ending.unsafeGet.route, SceneRoute.End)
    assertEquals(ending.unsafeGet.end.map(_.ending), Some(Ending.Victory))
    assertEquals(ending.unsafeGlobalEvents, Batch(Sfx.victory))
  }

  test("continuing from the end goes back to a fresh title") {
    val ended =
      inWorld.copy(
        route = SceneRoute.End,
        end = Some(EndScene.init(shared, Ending.Victory, world.player).unsafeGet)
      )
    val result = update(shared, Msg.EndSceneMsg(EndScene.Msg.Continue), ended)

    assertEquals(result.unsafeGet.route, SceneRoute.Title)
    assertEquals(result.unsafeGet.world, None)
    assertEquals(result.unsafeGlobalEvents, Batch(Sfx.title))
  }

  test("only the showing scene hears input") {
    val confirm = KeyboardEvent.KeyUp(Key.SPACE)
    assertEquals(
      subscriptions(start, Keyboard.default)(confirm),
      Some(Msg.TitleSceneMsg(TitleScene.Msg.Start))
    )
    assertEquals(
      subscriptions(inWorld, Keyboard.default)(confirm),
      Some(Msg.WorldSceneMsg(WorldScene.Msg.Confirm))
    )
  }
}
