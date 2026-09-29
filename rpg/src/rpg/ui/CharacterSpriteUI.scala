package rpg.ui

import indigo.*
import rpg.common.Types.*
import rpg.theme.Palette

/** Character art is procedural: each sprite is a pure function from state to a small scene graph
  * drawn in a 16x16 cell with its top-left corner at the origin.
  */
object CharacterSpriteUI:

  private def rgb(hex: String): RGBA = RGBA.fromHexString(hex)

  private def boxUI(x: Int, y: Int, w: Int, h: Int, color: RGBA): Quad =
    Quad(Rectangle(x, y, w, h), Fill.Color(color))

  private def dotUI(x: Int, y: Int, r: Int, color: RGBA): Shape.Circle =
    Shape.Circle(Point(x, y), r, Fill.Color(color))

  private val shadowUI: SceneNode =
    Quad(Rectangle(3, 13, 10, 3), Fill.Color(RGBA.Black.withAlpha(0.25)), Corners(2))

  // --- Characters -----------------------------------------------------------

  def playerSpriteUI(facing: Direction, bob: Int): Group =
    val eyesUI =
      facing match
        case Direction.Down =>
          Batch(boxUI(6, 6 + bob, 1, 2, Palette.ink), boxUI(9, 6 + bob, 1, 2, Palette.ink))
        case Direction.Left  => Batch(boxUI(5, 6 + bob, 1, 2, Palette.ink))
        case Direction.Right => Batch(boxUI(10, 6 + bob, 1, 2, Palette.ink))
        case Direction.Up    => Batch.empty

    Group(
      Batch(
        shadowUI,
        Quad(Rectangle(4, 8 + bob, 8, 7), Fill.Color(rgb("#3a86ff")), Corners(2)),
        boxUI(4, 12 + bob, 8, 1, rgb("#6b4226")),
        dotUI(8, 6 + bob, 4, rgb("#f1c27d")),
        boxUI(4, 1 + bob, 8, 3, rgb("#5c3a21"))
      ) ++ eyesUI
    )

  def elderSpriteUI(time: Seconds): Group =
    val sway = if (time.toDouble * 1.5).toInt % 2 == 0 then 0 else 1
    Group(
      shadowUI,
      Quad(Rectangle(3, 7, 10, 8), Fill.Color(rgb("#7b2cbf")), Corners(2)),
      dotUI(8, 5, 4, rgb("#f1c27d")),
      boxUI(5, 7, 6, 4, RGBA.White),
      boxUI(6, 4, 1, 2, Palette.ink),
      boxUI(9, 4, 1, 2, Palette.ink),
      boxUI(13 + sway, 2, 1, 13, rgb("#8b5a2b")),
      dotUI(13 + sway, 2, 2, rgb("#48cae4"))
    )

  def slimeSpriteUI(kind: EnemyKind, time: Seconds, seed: Int): Group =
    val phase  = time.toDouble * 5 + seed
    val squash = (Math.sin(phase) * 1.5).toInt

    kind match
      case EnemyKind.Slime =>
        Group(
          shadowUI,
          Shape.Circle(Point(8, 10 - squash), 6 + squash / 2, Fill.Color(rgb("#7ae582"))),
          boxUI(2, 10, 12, 5, rgb("#7ae582")),
          dotUI(6, 8 - squash, 2, RGBA.White.withAlpha(0.6)),
          boxUI(6, 10, 1, 2, Palette.ink),
          boxUI(10, 10, 1, 2, Palette.ink)
        )

      case EnemyKind.SlimeKing =>
        Group(
          Quad(Rectangle(0, 13, 16, 3), Fill.Color(RGBA.Black.withAlpha(0.3)), Corners(2)),
          Shape.Circle(Point(8, 8 - squash), 8 + squash / 2, Fill.Color(rgb("#9d4edd"))),
          boxUI(0, 9, 16, 6, rgb("#9d4edd")),
          dotUI(5, 5 - squash, 2, RGBA.White.withAlpha(0.5)),
          boxUI(5, 8, 2, 2, Palette.ink),
          boxUI(10, 8, 2, 2, Palette.ink),
          boxUI(4, -3 - squash, 8, 3, Palette.gold),
          boxUI(4, -5 - squash, 2, 2, Palette.gold),
          boxUI(7, -6 - squash, 2, 3, Palette.gold),
          boxUI(10, -5 - squash, 2, 2, Palette.gold)
        )

  def chestSpriteUI(opened: Boolean): Group =
    if opened then
      Group(
        shadowUI,
        boxUI(2, 3, 12, 3, rgb("#5e3c1c")),
        boxUI(2, 7, 12, 8, rgb("#6b4423")),
        boxUI(3, 8, 10, 3, Palette.ink)
      )
    else
      Group(
        shadowUI,
        boxUI(2, 5, 12, 10, rgb("#8b5a2b")),
        boxUI(2, 8, 12, 2, Palette.gold),
        boxUI(7, 8, 2, 4, rgb("#fff3b0"))
      )
