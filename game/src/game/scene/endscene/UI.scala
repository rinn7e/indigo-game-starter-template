package game.scene.endscene

import indigo.*
import game.common.Types.*
import game.theme.Palette
import game.ui.FadeInUI.fadeInUI
import game.ui.CharacterSpriteUI.{playerSpriteUI, slimeSpriteUI}
import game.ui.LabelUI.{blinkingLabelUI, centeredLabelUI}
import game.ui.ScreenUI.screenUI
import game.scene.endscene.Type.*

object UI:

  def ui(shared: Shared, model: Model): SceneUpdateFragment =
    val now = shared.now

    val (title, subtitle, color) =
      model.ending match
        case Ending.Victory  => ("VICTORY!", "THE VILLAGE IS SAFE THANKS TO YOU.", Palette.gold)
        case Ending.GameOver => ("GAME OVER", "THE SLIMES HAVE WON... FOR NOW.", Palette.danger)

    val heroUI =
      model.ending match
        case Ending.Victory =>
          playerSpriteUI(Direction.Down, (Math.sin(now.toDouble * 8) * 2).toInt)
        case Ending.GameOver => slimeSpriteUI(EnemyKind.Slime, now, 0)

    screenUI(
      shared.viewport,
      Fill.Color(Palette.ink),
      Batch(
        heroUI.withScale(Vector2(4, 4)).moveTo(208, 70)
      ),
      Batch(
        centeredLabelUI(title, 36, color),
        centeredLabelUI(subtitle, 160, Palette.text),
        centeredLabelUI(s"LEVEL ${model.level}   GOLD ${model.gold}", 190, Palette.muted)
      ) ++ blinkingLabelUI("PRESS SPACE", 270, now)
    ) |+| fadeInUI(model.enteredAt, now, shared.viewport)
