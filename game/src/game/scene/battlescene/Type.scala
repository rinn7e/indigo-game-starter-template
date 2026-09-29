package game.scene.battlescene

import indigo.*
import game.common.Types.*
import game.scene.battlescene.subui.ActionMenu

object Type:

  enum Phase derives CanEqual:
    case PlayerTurn
    case EnemyTurn(startedAt: Seconds)
    case Won
    case Lost
    case Escaped

  final case class Model(
      player: Player,
      enemy: Enemy,
      enemyStats: Stats,
      menu: ActionMenu.Model, // a stateful sub-UI: its own Model / Msg / update / ui
      phase: Phase,
      log: Batch[String],
      lastHit: Option[(Boolean, Seconds)], // (enemy was hit?, when) - used for hit flashes
      enteredAt: Seconds,
      fightStartedAt: Option[Seconds] // when the fight started (shows the "FIGHT!" banner)
  ) derives CanEqual

  enum Msg derives CanEqual:
    case Tick

    /** Confirm once the battle is over. Intercepted by the parent, which reads `phase`. */
    case Continue
    case ActionMenuMsg(subMsg: ActionMenu.Msg)

    /** From `BattleEvent.FightStarted`: shows the "FIGHT!" banner. */
    case FightStarted

  /** This scene's own engine events, the Indigo way (like `KeyboardEvent` or `SceneEvent`). `init`
    * emits them as effects, the engine delivers them on the next frame, and `subscriptions` turns
    * them into `Msg`s, like any other engine event.
    */
  enum BattleEvent extends GlobalEvent derives CanEqual:
    case FightStarted
