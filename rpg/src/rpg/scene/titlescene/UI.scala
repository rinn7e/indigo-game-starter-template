package rpg.scene.titlescene

import indigo.*
import rpg.common.Types.*
import rpg.theme.Palette
import rpg.ui.FadeInUI.fadeInUI
import rpg.ui.CharacterSpriteUI.{playerSpriteUI, slimeSpriteUI}
import rpg.ui.LabelUI.{blinkingLabelUI, centeredLabelUI}
import rpg.ui.ScreenUI.screenUI
import rpg.scene.titlescene.Type.*

object UI:

  def ui(shared: Shared, model: Model): SceneUpdateFragment =
    val now = shared.now
    val bob = (Math.sin(now.toDouble * 3) * 3).toInt

    screenUI(
      shared.viewport,
      Fill.LinearGradient(
        Point(0, 0),
        Palette.ink,
        Point(0, shared.viewport.height),
        RGBA.fromHexString("#3d5a80")
      ),
      Batch(
        slimeSpriteUI(EnemyKind.SlimeKing, now, 0)
          .withScale(Vector2(4, 4))
          .moveTo(208, 72 + bob),
        slimeSpriteUI(EnemyKind.Slime, now, 2).withScale(Vector2(2, 2)).moveTo(140, 110),
        slimeSpriteUI(EnemyKind.Slime, now, 5).withScale(Vector2(2, 2)).moveTo(308, 110),
        playerSpriteUI(Direction.Up, 0).withScale(Vector2(3, 3)).moveTo(216, 150)
      ),
      Batch(
        centeredLabelUI("SLIME QUEST", 16, Palette.gold),
        centeredLabelUI("A TINY RPG MADE WITH INDIGO", 214, Palette.muted),
        centeredLabelUI("ARROWS OR WASD TO MOVE", 244, Palette.text),
        centeredLabelUI("SPACE TO TALK, OPEN AND CONFIRM", 262, Palette.text)
      ) ++ blinkingLabelUI("PRESS SPACE TO START", 292, now)
    ) |+| fadeInUI(model.enteredAt, now, shared.viewport)
