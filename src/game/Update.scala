package game

import indigo.*
import game.Type.*
import game.common.Types.*
import game.common.constant.Maps
import game.scene.BattleScene.Phase
import game.scene.{BattleScene, EndScene, TitleScene, WorldScene}

object Update:

  // -----------------------------------------------------------------
  // Init
  // -----------------------------------------------------------------

  /** Like a TEA root `init`: starts on the title, with the title's entry effects. Other scenes are
    * created (with their own entry effects) when they are entered.
    */
  def init(shared: Shared): Outcome[Model] =
    TitleScene.init(shared).map(title => Model(SceneRoute.Title, title, None, None, None))

  // -----------------------------------------------------------------
  // Update
  // -----------------------------------------------------------------

  /** Delegates each message to its scene, then intercepts the child messages the root cares about
    * (TEA child msg interception; `flatMap` is `updateAndCmd`).
    */
  def update(shared: Shared, msg: Msg, model: Model): Outcome[Model] =
    msg match
      case Msg.TitleSceneMsg(subMsg) =>
        TitleScene
          .update(shared, subMsg, model.title)
          .map(title => model.copy(title = title))
          .flatMap { m =>
            subMsg match
              case TitleScene.Msg.Start => startGame(shared, m)
          }

      case Msg.WorldSceneMsg(subMsg) =>
        model.world match
          case None =>
            Outcome(model)

          case Some(world) =>
            WorldScene
              .update(shared, subMsg, world)
              .map(world => model.copy(world = Some(world)))
              .flatMap { m =>
                (subMsg, m.world) match
                  case (WorldScene.Msg.Tick(_) | WorldScene.Msg.Walk(_), Some(world)) =>
                    world.encounter match
                      case Some(enemyId) => startBattle(shared, enemyId, world, m)
                      case None          => Outcome(m)

                  case (WorldScene.Msg.Confirm, Some(world)) if world.quest == Quest.Complete =>
                    endGame(shared, Ending.Victory, world.player, m)

                  case _ =>
                    Outcome(m)
              }

      case Msg.BattleSceneMsg(subMsg) =>
        model.battle match
          case None =>
            Outcome(model)

          case Some(battle) =>
            BattleScene
              .update(shared, subMsg, battle)
              .map(battle => model.copy(battle = Some(battle)))
              .flatMap { m =>
                (subMsg, m.battle) match
                  case (BattleScene.Msg.Continue, Some(battle)) => finishBattle(shared, battle, m)
                  case _                                        => Outcome(m)
              }

      case Msg.EndSceneMsg(subMsg) =>
        model.end match
          case None =>
            Outcome(model)

          case Some(end) =>
            EndScene
              .update(shared, subMsg, end)
              .map(end => model.copy(end = Some(end)))
              .flatMap { m =>
                subMsg match
                  case EndScene.Msg.Continue => backToTitle(shared, m)
              }

  // --- Navigation: each one enters a scene through its `init` ---------

  /** A new game: a fresh world, and nothing left over from the last one. */
  private def startGame(shared: Shared, model: Model): Outcome[Model] =
    WorldScene
      .init(shared, Maps.overworld)
      .map(world => Model(SceneRoute.World, model.title, Some(world), None, None))

  private def startBattle(
      shared: Shared,
      enemyId: EnemyId,
      world: WorldScene.Model,
      model: Model
  ): Outcome[Model] =
    world.enemies.find(_.id == enemyId) match
      case Some(enemy) =>
        BattleScene
          .init(shared, world.player, enemy)
          .map(battle => model.copy(route = SceneRoute.Battle, battle = Some(battle)))

      case None =>
        Outcome(model)

  private def finishBattle(
      shared: Shared,
      battle: BattleScene.Model,
      model: Model
  ): Outcome[Model] =
    battle.phase match
      case Phase.Won =>
        backToWorld(shared, WorldScene.Msg.BattleWon(battle.player, battle.enemy), model)

      case Phase.Escaped =>
        backToWorld(shared, WorldScene.Msg.BattleEscaped(battle.player, battle.enemy), model)

      case Phase.Lost =>
        endGame(shared, Ending.GameOver, battle.player, model.copy(battle = None))

      case Phase.PlayerTurn | Phase.EnemyTurn(_) =>
        Outcome(model)

  /** Hands the battle result to the world scene, the way a TEA parent calls a child's `update`. The
    * world's handling of it re-enters the scene (fade-in, jingle).
    */
  private def backToWorld(shared: Shared, worldMsg: WorldScene.Msg, model: Model): Outcome[Model] =
    model.world match
      case Some(world) =>
        WorldScene
          .update(shared, worldMsg, world)
          .map(world => model.copy(route = SceneRoute.World, world = Some(world), battle = None))

      case None =>
        Outcome(model)

  private def endGame(
      shared: Shared,
      ending: Ending,
      player: Player,
      model: Model
  ): Outcome[Model] =
    EndScene
      .init(shared, ending, player)
      .map(end => model.copy(route = SceneRoute.End, end = Some(end)))

  private def backToTitle(shared: Shared, model: Model): Outcome[Model] =
    TitleScene.init(shared).map(title => Model(SceneRoute.Title, title, None, None, None))
