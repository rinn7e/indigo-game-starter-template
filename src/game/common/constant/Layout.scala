package game.common.constant

import indigo.*

object Layout:

  val magnification: Int = 2

  val tileSize: Int = 16

  /** The size fixed-layout screens (title, battle, end) are designed at, in game pixels. */
  val design: Size        = Size(480, 320)
  val designCenter: Point = design.toPoint / 2
