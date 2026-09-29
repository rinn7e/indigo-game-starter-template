package rpg

import indigo.*
import rpg.Type.*
import rpg.common.Types.SceneRoute
import rpg.scene.{BattleScene, EndScene, TitleScene, WorldScene}

object Subscription:

  /** Only the showing scene listens to input; its messages are wrapped as root messages. */
  def subscriptions(model: Model, keyboard: Keyboard): GlobalEvent => Option[Msg] =
    e =>
      model.route match
        case SceneRoute.Title =>
          TitleScene.subscriptions(model.title, keyboard)(e).map(Msg.TitleSceneMsg(_))

        case SceneRoute.World =>
          model.world.flatMap(WorldScene.subscriptions(_, keyboard)(e)).map(Msg.WorldSceneMsg(_))

        case SceneRoute.Battle =>
          model.battle.flatMap(BattleScene.subscriptions(_, keyboard)(e)).map(Msg.BattleSceneMsg(_))

        case SceneRoute.End =>
          model.end.flatMap(EndScene.subscriptions(_, keyboard)(e)).map(Msg.EndSceneMsg(_))
