package game.scene.battlescene.subui.actionmenu

import indigo.*
import game.theme.Palette
import game.ui.LabelUI.labelUI
import game.ui.PanelUI.panelUI
import game.scene.battlescene.subui.actionmenu.Type.*

object UI:

  /** @param active
    *   whether the menu takes input right now (the parent decides: only on the player's turn)
    * @param potions
    *   shown next to the POTION option
    */
  def ui(model: Model, active: Boolean, potions: Int): Batch[SceneNode] =
    panelUI(Rectangle(326, 232, 144, 78)) ++
      Action.menu.zipWithIndex.flatMap { case (action, i) =>
        val y        = 238 + i * 24
        val selected = active && i == model.selected
        val text =
          action match
            case Action.Potion => s"POTION $potions"
            case _             => action.label

        val color =
          if !active then Palette.muted else if selected then Palette.gold else Palette.text

        (if selected then
           Batch(Quad(Rectangle(332, y - 3, 132, 18), Fill.Color(Palette.highlight), Corners(3)))
         else Batch.empty) :+ labelUI(text, 340, y, color)
      }
