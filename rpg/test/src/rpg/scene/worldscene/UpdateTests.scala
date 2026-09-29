package rpg.scene.worldscene

import indigo.*
import rpg.common.Types.*
import rpg.common.util.LevelParser
import rpg.scene.worldscene.Type.*
import rpg.scene.worldscene.Update.*

class UpdateTests extends munit.FunSuite {

  //  012345
  // 0######
  // 1#@.E.#
  // 2#.sc.#
  // 3######
  val model: Model =
    init(
      Shared.atStartup(Dice.loaded(1)),
      LevelParser.parse(Batch("######", "#@.E.#", "#.sc.#", "######"))
    ).unsafeGet

  val now: Seconds = Seconds(10)

  val shared: Shared = Shared(now, Dice.fromSeed(1), Size(480, 320))

  def at(time: Seconds): Shared = shared.copy(now = time)

  test("the player steps onto walkable tiles") {
    val moved = update(shared, Msg.Walk(Direction.Right), model).unsafeGet.player

    assertEquals(moved.position, Point(2, 1))
    assertEquals(moved.facing, Direction.Right)
    assertEquals(moved.step, Some(Step(Point(1, 1), now)))
  }

  test("the player cannot walk into walls, but turns to face them") {
    val blocked = update(shared, Msg.Walk(Direction.Up), model).unsafeGet.player

    assertEquals(blocked.position, Point(1, 1))
    assertEquals(blocked.facing, Direction.Up)
  }

  test("the player waits for the current step to finish before moving again") {
    val once  = update(shared, Msg.Walk(Direction.Right), model).unsafeGet
    val twice = update(at(now + Seconds(0.05)), Msg.Walk(Direction.Down), once).unsafeGet

    assertEquals(twice.player.position, Point(2, 1))
  }

  test("bumping into an enemy records an encounter") {
    val atSide = model.copy(player = model.player.copy(position = Point(1, 2)))
    val result = update(shared, Msg.Walk(Direction.Right), atSide)

    assertEquals(result.unsafeGet.player.position, Point(1, 2))
    assertEquals(result.unsafeGet.encounter, Some(EnemyId(0)))
  }

  test("no battle starts while the player is safe after fleeing") {
    val safe = model.copy(player =
      model.player.copy(position = Point(1, 2), safeUntil = now + Seconds(1))
    )
    val result = update(shared, Msg.Walk(Direction.Right), safe)

    assertEquals(result.unsafeGet.encounter, None)
  }

  test("opening a chest grants a potion and gold, only once") {
    val facingChest =
      model.copy(player = model.player.copy(position = Point(4, 2), facing = Direction.Left))

    val opened = update(shared, Msg.Confirm, facingChest).unsafeGet
    assertEquals(opened.player.potions, model.player.potions + 1)
    assertEquals(opened.player.gold, 10)
    assert(opened.chests.forall(_.opened))

    val again = update(shared, Msg.Confirm, opened).unsafeGet
    assertEquals(again.player.potions, opened.player.potions)
    assertEquals(again.player.gold, opened.player.gold)
  }

  test("talking to the elder starts the quest once the dialogue is finished") {
    val facingElder =
      model.copy(player = model.player.copy(position = Point(2, 1), facing = Direction.Right))

    val talking = update(shared, Msg.Confirm, facingElder).unsafeGet
    assert(talking.dialogue.isDefined)

    val lines = talking.dialogue.map(_.dialogue.lines.length).getOrElse(0)
    val done =
      (1 to lines).foldLeft(talking)((m, _) => update(shared, Msg.Confirm, m).unsafeGet)

    assertEquals(done.dialogue, None)
    assertEquals(done.quest, Quest.SlayTheKing)
    assertEquals(done.player.potions, model.player.potions + 1)
  }

  test("the player cannot walk while a dialogue is open") {
    val talking = model.copy(dialogue = Some(OpenDialogue(Dialogue("ELDER", "HI"), DialogueEnd.Nothing)))
    val result  = update(shared, Msg.Walk(Direction.Right), talking).unsafeGet

    assertEquals(result.player.position, model.player.position)
  }

  test("finishing the elder's final dialogue completes the quest") {
    val won =
      update(
        shared,
        Msg.Confirm,
        model.copy(
          quest = Quest.KingDefeated,
          player = model.player.copy(position = Point(2, 1), facing = Direction.Right)
        )
      ).unsafeGet

    val lines = won.dialogue.map(_.dialogue.lines.length).getOrElse(0)
    val done =
      (1 to lines).foldLeft(won)((m, _) => update(shared, Msg.Confirm, m).unsafeGet)

    assertEquals(done.quest, Quest.Complete)
  }

  test("defeating the slime king completes the quest") {
    val king  = Enemy(EnemyId(9), EnemyKind.SlimeKing, Point(2, 1), None, Seconds.zero)
    val after =
      update(shared, Msg.BattleWon(model.player, king), model.copy(enemies = Batch(king))).unsafeGet

    assertEquals(after.quest, Quest.KingDefeated)
    assertEquals(after.enemies, Batch.empty)
    assertEquals(after.player.gold, EnemyKind.SlimeKing.goldReward)
  }
}
