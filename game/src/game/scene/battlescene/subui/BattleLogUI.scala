package game.scene.battlescene.subui

import indigo.*
import game.scene.battlescene.Type.Model
import game.theme.Palette
import game.ui.LabelUI.labelUI
import game.ui.PanelUI.panelUI

object BattleLogUI:

  def battleLogUI(model: Model): Batch[SceneNode] =
    panelUI(Rectangle(10, 232, 304, 78)) ++
      model.log.zipWithIndex.map { case (line, i) =>
        labelUI(
          line,
          16,
          237 + i * 18,
          if i == model.log.length - 1 then Palette.text else Palette.muted
        )
      }
