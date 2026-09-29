package rpg.scene.worldscene.subui

import indigo.*
import rpg.common.constant.Layout.tileSize
import rpg.common.util.Character.*
import rpg.scene.worldscene.Type.Model
import rpg.scene.worldscene.common.Util.renderPosition
import rpg.ui.CharacterSpriteUI.{chestSpriteUI, elderSpriteUI, playerSpriteUI, slimeSpriteUI}

object EntitiesUI:

  def entitiesUI(model: Model, now: Seconds): Batch[SceneNode] =
    val player = model.player

    val playerUI: Batch[(Int, SceneNode)] =
      val blinkingOff = isSafe(now)(player) && (now.toDouble * 10).toInt % 2 == 0
      if blinkingOff then Batch.empty
      else
        val bob = if isMoving(now)(player) && (now.toDouble * 12).toInt % 2 == 0 then -1 else 0
        val at  = renderPosition(player.position, player.step, now)
        Batch(at.y -> playerSpriteUI(player.facing, bob).moveTo(at))

    val enemiesUI =
      model.enemies.map { e =>
        val at = renderPosition(e.position, e.step, now)
        at.y -> slimeSpriteUI(e.kind, now, e.id.value).moveTo(at)
      }

    val chestsUI =
      model.chests.map(c =>
        (c.position.y * tileSize) -> chestSpriteUI(c.opened).moveTo(c.position * tileSize)
      )

    val elderUI =
      Batch(
        (model.elder.position.y * tileSize) -> elderSpriteUI(now)
          .moveTo(model.elder.position * tileSize)
      )

    // Draw back-to-front so sprites lower on screen overlap those above them.
    (chestsUI ++ elderUI ++ enemiesUI ++ playerUI).sortBy(_._1).map(_._2)
