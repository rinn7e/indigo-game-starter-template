package rpg.scene.worldscene

import indigo.*
import rpg.common.Types.*
import rpg.common.asset.Sfx
import rpg.common.util.Character.*
import rpg.common.util.Dialogue.*
import rpg.scene.worldscene.common.Util.*

import scala.util.chaining.*
import rpg.scene.worldscene.Type.*

object Update:

  // -----------------------------------------------------------------
  // Init
  // -----------------------------------------------------------------

  /** Entering the world for a new game: fade in and play its jingle (an effect, like a `Cmd`). */
  def init(shared: Shared, level: Level): Outcome[Model] =
    val placements = level.placements

    val playerStart =
      placements.collectFirst { case Placement.PlayerStart(at) => at }.getOrElse(Point(1, 1))

    val elderAt =
      placements.collectFirst { case Placement.Elder(at) => at }.getOrElse(Point.zero)

    val enemies =
      placements
        .collect {
          case Placement.Slime(at)     => EnemyKind.Slime     -> at
          case Placement.SlimeKing(at) => EnemyKind.SlimeKing -> at
        }
        .zipWithIndex
        .map { case ((kind, at), i) =>
          Enemy(EnemyId(i), kind, at, None, Seconds(1.0 + i * 0.25))
        }

    val chests =
      placements.collect { case Placement.Chest(at) => Chest(at, opened = false) }

    Model(
      map = level.map,
      player = Player.initial(playerStart),
      enemies = enemies,
      chests = chests,
      elder = Npc("ELDER", elderAt),
      quest = Quest.NotStarted,
      dialogue = None,
      toast = None,
      encounter = None,
      enteredAt = shared.now
    ).pipe(enter(shared.now))

  /** Every entry into the world (a new game, or back from a battle) fades in and plays the jingle.
    */
  private def enter(now: Seconds)(model: Model): Outcome[Model] =
    Outcome(model.copy(enteredAt = now)).addGlobalEvents(Sfx.world)

  // -----------------------------------------------------------------
  // Update
  // -----------------------------------------------------------------

  def update(shared: Shared, msg: Msg, model: Model): Outcome[Model] =
    msg match
      case Msg.Tick(held) =>
        val expired = model.copy(toast = model.toast.filter(_.until > shared.now))

        if model.dialogue.isDefined then Outcome(expired)
        else
          movePlayer(shared.now, held, expired)
            .flatMap(moveEnemies(shared, _))

      case Msg.Walk(direction) =>
        if model.dialogue.isDefined then Outcome(model)
        else movePlayer(shared.now, Some(direction), model)

      case Msg.Confirm =>
        model.dialogue match
          case Some(open) => advanceDialogue(open, model)
          case None       => Outcome(interact(shared.now, model))

      case Msg.BattleWon(winner, defeated) =>
        afterVictory(winner, defeated, shared.now, model).pipe(enter(shared.now))

      case Msg.BattleEscaped(escaped, enemy) =>
        afterEscape(escaped, enemy, shared.now, model).pipe(enter(shared.now))

  // --- Movement -------------------------------------------------------

  def movePlayer(now: Seconds, input: Option[Direction], model: Model): Outcome[Model] =
    val player = model.player

    input match
      case None =>
        Outcome(model)

      case Some(_) if isMoving(now)(player) =>
        Outcome(model)

      case Some(dir) =>
        val turned = player.copy(facing = dir)
        val target = turned.position + dir.delta

        enemyAt(target)(model) match
          case Some(enemy) if !isSafe(now)(player) =>
            Outcome(meet(enemy.id, model.copy(player = turned)))

          case _ if canEnter(target)(model) =>
            Outcome(
              model.copy(player =
                turned.copy(position = target, step = Some(Step(turned.position, now)))
              )
            )

          case _ =>
            Outcome(model.copy(player = turned))

  def moveEnemies(shared: Shared, model: Model): Outcome[Model] =
    model.enemies.foldLeft(Outcome(model)) { (acc, enemy) =>
      acc.flatMap(moveEnemy(enemy.id, shared, _))
    }

  private def moveEnemy(id: EnemyId, shared: Shared, model: Model): Outcome[Model] =
    val now = shared.now

    model.enemies.find(_.id == id) match
      case Some(enemy) if enemy.kind.wanders && now >= enemy.nextMoveAt =>
        val rescheduled = enemy.copy(nextMoveAt = now + Seconds(0.6 + shared.dice.roll(10) * 0.1))
        val roll        = shared.dice.roll(6)

        Direction.all.lift(roll - 1) match
          case None =>
            Outcome(replaceEnemy(rescheduled, model))

          case Some(dir) =>
            val target = enemy.position + dir.delta

            if target == model.player.position then
              if isSafe(now)(model.player) then Outcome(replaceEnemy(rescheduled, model))
              else Outcome(meet(id, replaceEnemy(rescheduled, model)))
            else if canEnter(target)(model) then
              Outcome(
                replaceEnemy(
                  rescheduled.copy(position = target, step = Some(Step(enemy.position, now))),
                  model
                )
              )
            else Outcome(replaceEnemy(rescheduled, model))

      case _ =>
        Outcome(model)

  /** Records an encounter; if two happen in the same frame, the first one wins. */
  private def meet(id: EnemyId, model: Model): Model =
    model.copy(encounter = model.encounter.orElse(Some(id)))

  private def replaceEnemy(enemy: Enemy, model: Model): Model =
    model.copy(enemies = model.enemies.map(e => if e.id == enemy.id then enemy else e))

  // --- Interaction ----------------------------------------------------

  def interact(now: Seconds, model: Model): Model =
    val target = facingTile(model.player)

    if model.elder.position == target then talkToElder(model)
    else
      chestAt(target)(model) match
        case Some(chest) if !chest.opened =>
          model
            .copy(
              chests =
                model.chests.map(c => if c.position == target then c.copy(opened = true) else c),
              player = model.player.copy(
                potions = model.player.potions + 1,
                gold = model.player.gold + 10
              )
            )
            .pipe(withToast("FOUND A POTION AND 10 GOLD!", now))

        case Some(_) =>
          withToast("THE CHEST IS EMPTY.", now)(model)

        case None =>
          model

  def talkToElder(model: Model): Model =
    val open =
      model.quest match
        case Quest.NotStarted =>
          OpenDialogue(
            Dialogue(
              model.elder.name,
              "AH, A TRAVELLER! THANK GOODNESS.",
              "SLIMES HAVE OVERRUN OUR MEADOW.",
              "THEIR MASTER, THE SLIME KING, HIDES",
              "IN THE STONE RUINS TO THE EAST.",
              "DEFEAT HIM AND SAVE OUR VILLAGE!",
              "TAKE THIS POTION. GOOD LUCK!"
            ),
            DialogueEnd.StartQuest
          )

        case Quest.SlayTheKing =>
          OpenDialogue(
            Dialogue(
              model.elder.name,
              "THE SLIME KING WAITS IN THE RUINS",
              "EAST OF THE RIVER. CROSS THE BRIDGE.",
              "OPEN CHESTS TO FIND POTIONS."
            ),
            DialogueEnd.Nothing
          )

        case Quest.KingDefeated | Quest.Complete =>
          OpenDialogue(
            Dialogue(
              model.elder.name,
              "YOU DID IT! THE SLIME KING IS GONE!",
              "THE VILLAGE IS SAFE ONCE MORE.",
              "YOU ARE A TRUE HERO!"
            ),
            DialogueEnd.Win
          )

    model.copy(dialogue = Some(open))

  def advanceDialogue(open: OpenDialogue, model: Model): Outcome[Model] =
    advance(open.dialogue) match
      case Some(next) =>
        Outcome(model.copy(dialogue = Some(open.copy(dialogue = next))))

      case None =>
        val closed = model.copy(dialogue = None)

        open.onEnd match
          case DialogueEnd.Nothing =>
            Outcome(closed)

          case DialogueEnd.StartQuest =>
            Outcome(
              closed.copy(
                quest = Quest.SlayTheKing,
                player = model.player.copy(potions = model.player.potions + 1)
              )
            )

          case DialogueEnd.Win =>
            Outcome(closed.copy(quest = Quest.Complete))

  // --- Battle results -------------------------------------------------

  def afterVictory(winner: Player, defeated: Enemy, now: Seconds, model: Model): Model =
    val kind = defeated.kind
    val (levelled, n) =
      winner
        .copy(gold = winner.gold + kind.goldReward)
        .pipe(gainXp(kind.xpReward))

    val updated =
      model.copy(
        player = levelled.copy(step = None, safeUntil = now + Seconds(1)),
        encounter = None,
        enemies = model.enemies.filterNot(_.id == defeated.id),
        quest = if kind == EnemyKind.SlimeKing then Quest.KingDefeated else model.quest
      )

    if kind == EnemyKind.SlimeKing then
      withToast("THE SLIME KING FALLS! RETURN TO THE ELDER.", now)(updated)
    else if n > 0 then withToast(s"LEVEL UP! YOU ARE NOW LEVEL ${levelled.level}!", now)(updated)
    else withToast(s"GAINED ${kind.xpReward} XP AND ${kind.goldReward} GOLD.", now)(updated)

  def afterEscape(escaped: Player, enemy: Enemy, now: Seconds, model: Model): Model =
    model.copy(
      player = escaped.copy(step = None, safeUntil = now + Seconds(2)),
      encounter = None,
      enemies = model.enemies.map(e =>
        if e.id == enemy.id then e.copy(nextMoveAt = now + Seconds(2)) else e
      )
    )
