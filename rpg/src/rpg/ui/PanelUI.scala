package rpg.ui

import indigo.*
import rpg.theme.Palette

object PanelUI:

  def panelUI(bounds: Rectangle): Batch[SceneNode] =
    Batch(
      Quad(bounds.expand(2), Fill.Color(Palette.border), Corners(4)),
      Quad(bounds, Fill.Color(Palette.panel), Corners(3))
    )
