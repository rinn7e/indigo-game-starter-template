package rpg.ui

import indigo.*
import rpg.theme.Palette

object BarUI:

  def barUI(at: Point, width: Int, current: Int, max: Int, color: RGBA): Batch[SceneNode] =
    val filled = if max <= 0 then 0 else Math.max(0, (width - 2) * current / max)
    Batch(
      Quad(Rectangle(at, Size(width, 6)), Fill.Color(Palette.ink)),
      Quad(Rectangle(at + Point(1, 1), Size(filled, 4)), Fill.Color(color))
    )
