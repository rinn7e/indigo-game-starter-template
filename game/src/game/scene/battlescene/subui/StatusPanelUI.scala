package game.scene.battlescene.subui

import indigo.*
import game.scene.battlescene.Type.Model
import game.theme.Palette
import game.ui.BarUI.barUI
import game.ui.LabelUI.labelUI
import game.ui.PanelUI.panelUI

object StatusPanelUI:

  /** The hero's and the enemy's name, HP and HP bar. */
  def statusPanelUI(model: Model): Batch[SceneNode] =
    heroStatusPanelUI(model) ++ enemyStatusPanelUI(model)

  private def heroStatusPanelUI(model: Model): Batch[SceneNode] =
    val p = model.player
    panelUI(Rectangle(10, 20, 206, 36)) ++
      Batch(
        labelUI(s"HERO  LV ${p.level}", 16, 23, Palette.gold),
        labelUI(s"${p.stats.hp}/${p.stats.maxHp}", 150, 23, Palette.muted)
      ) ++
      barUI(Point(16, 42), 194, p.stats.hp, p.stats.maxHp, Palette.good)

  private def enemyStatusPanelUI(model: Model): Batch[SceneNode] =
    val e = model.enemyStats
    panelUI(Rectangle(264, 20, 206, 36)) ++
      Batch(
        labelUI(model.enemy.kind.name, 270, 23, Palette.text),
        labelUI(s"${e.hp}/${e.maxHp}", 404, 23, Palette.muted)
      ) ++
      barUI(Point(270, 42), 194, e.hp, e.maxHp, Palette.danger)
