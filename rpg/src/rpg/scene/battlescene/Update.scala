package rpg.scene.battlescene

import indigo.*
import rpg.common.Types.*
import rpg.common.asset.Sfx
import rpg.common.util.Character.*
import rpg.scene.battlescene.subui.ActionMenu
import rpg.scene.battlescene.subui.ActionMenu.Action
import rpg.scene.battlescene.Type.*

object Update:

  val logSize: Int        = 4
  val enemyDelay: Seconds = Seconds(0.7)
  val inputGrace: Seconds = Seconds(0.35)

  // -----------------------------------------------------------------
  // Init
  // -----------------------------------------------------------------

  /** Entering a battle: fade in, play the sting, and emit `BattleEvent.FightStarted`, which comes
    * back through `subscriptions` as `Msg.FightStarted` (an effect whose result is a `Msg`).
    */
  def init(shared: Shared, player: Player, enemy: Enemy): Outcome[Model] =
    Outcome(
      Model(
        player = player.copy(step = None),
        enemy = enemy,
        enemyStats = enemy.kind.stats,
        menu = ActionMenu.init,
        phase = Phase.PlayerTurn,
        log = Batch(s"A ${enemy.kind.name} APPEARS!"),
        lastHit = None,
        enteredAt = shared.now,
        fightStartedAt = None
      )
    ).addGlobalEvents(Sfx.battle, BattleEvent.FightStarted)

  // -----------------------------------------------------------------
  // Update
  // -----------------------------------------------------------------

  def update(shared: Shared, msg: Msg, model: Model): Outcome[Model] =
    val now = shared.now

    msg match
      case Msg.Tick =>
        model.phase match
          case Phase.EnemyTurn(startedAt) if now >= startedAt + enemyDelay =>
            Outcome(enemyAttacks(shared, model))

          case _ =>
            Outcome(model)

      case Msg.FightStarted =>
        Outcome(model.copy(fightStartedAt = Some(now)))

      // The key press that started the battle must not also drive the menu.
      case _ if now < model.enteredAt + inputGrace =>
        Outcome(model)

      // TEA child msg interception: delegate to the menu's update, then intercept `Choose`. The
      // menu only takes input on the player's turn.
      case Msg.ActionMenuMsg(subMsg) =>
        if model.phase == Phase.PlayerTurn then
          Outcome(model.copy(menu = ActionMenu.update(subMsg, model.menu)))
            .flatMap { m =>
              subMsg match
                case ActionMenu.Msg.Choose =>
                  Outcome(perform(ActionMenu.selectedAction(m.menu), shared, m))

                case ActionMenu.Msg.CursorUp | ActionMenu.Msg.CursorDown =>
                  Outcome(m)
            }
        else Outcome(model)

      case Msg.Continue =>
        Outcome(model)

  /** Attack minus defence, with a -1..+2 random spread, never less than 1. */
  def rollDamage(attack: Int, defense: Int, dice: Dice): Int =
    Math.max(1, attack - defense + dice.roll(4) - 2)

  private def say(model: Model, lines: String*): Model =
    model.copy(log = (model.log ++ Batch.fromSeq(lines)).takeRight(logSize))

  def perform(action: Action, shared: Shared, model: Model): Model =
    val now    = shared.now
    val dice   = shared.dice
    val player = model.player
    val enemy  = model.enemy

    action match
      case Action.Attack =>
        val critical = dice.roll(10) == 10
        val base     = rollDamage(player.stats.attack, model.enemyStats.defense, dice)
        val dmg      = if critical then base * 2 else base
        val hurt     = damage(dmg)(model.enemyStats)
        val hit =
          say(
            model.copy(enemyStats = hurt, lastHit = Some((true, now))),
            if critical then s"CRITICAL! YOU DEAL $dmg DAMAGE!"
            else s"YOU HIT ${enemy.kind.name} FOR $dmg."
          )

        if isDead(hurt) then
          say(
            hit.copy(phase = Phase.Won),
            s"${enemy.kind.name} IS DEFEATED!",
            s"GAINED ${enemy.kind.xpReward} XP."
          )
        else hit.copy(phase = Phase.EnemyTurn(now))

      case Action.Potion if player.potions <= 0 =>
        say(model, "YOU HAVE NO POTIONS LEFT!")

      case Action.Potion =>
        val healed = player.copy(
          potions = player.potions - 1,
          stats = heal(Player.potionHeal)(player.stats)
        )
        say(
          model.copy(player = healed, phase = Phase.EnemyTurn(now)),
          s"YOU DRINK A POTION. HP ${healed.stats.hp}/${healed.stats.maxHp}."
        )

      case Action.Run if !enemy.kind.wanders =>
        say(model.copy(phase = Phase.EnemyTurn(now)), "THERE IS NO ESCAPE!")

      case Action.Run =>
        if dice.roll(4) > 1 then say(model.copy(phase = Phase.Escaped), "YOU GOT AWAY SAFELY.")
        else say(model.copy(phase = Phase.EnemyTurn(now)), "YOU COULD NOT ESCAPE!")

  def enemyAttacks(shared: Shared, model: Model): Model =
    val dmg  = rollDamage(model.enemyStats.attack, model.player.stats.defense, shared.dice)
    val hurt = model.player.copy(stats = damage(dmg)(model.player.stats))
    val hit =
      say(
        model.copy(player = hurt, lastHit = Some((false, shared.now))),
        s"${model.enemy.kind.name} HITS YOU FOR $dmg."
      )

    if isDead(hurt.stats) then say(hit.copy(phase = Phase.Lost), "YOU HAVE FALLEN...")
    else hit.copy(phase = Phase.PlayerTurn)
