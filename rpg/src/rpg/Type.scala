package rpg

import rpg.common.Types.SceneRoute
import rpg.scene.{BattleScene, EndScene, TitleScene, WorldScene}

object Type:

  final case class Model(
      route: SceneRoute,
      title: TitleScene.Model,
      // Created when a new game starts, and kept during battles (you return to it).
      world: Option[WorldScene.Model],
      // Only present while a battle is in progress.
      battle: Option[BattleScene.Model],
      // Only present once the game has ended.
      end: Option[EndScene.Model]
  ) derives CanEqual

  /** Root messages: each wraps a scene's own `Msg`, like the page messages of a TEA root. */
  enum Msg derives CanEqual:
    case TitleSceneMsg(subMsg: TitleScene.Msg)
    case WorldSceneMsg(subMsg: WorldScene.Msg)
    case BattleSceneMsg(subMsg: BattleScene.Msg)
    case EndSceneMsg(subMsg: EndScene.Msg)
