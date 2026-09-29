package game.common.types

import indigo.*
import game.common.constant.Layout

/** Per-frame values every scene's `update` and `ui` receive, like `shared` in a TEA app: the clock,
  * the frame's deterministic dice, and the visible area in game pixels.
  */
final case class Shared(now: Seconds, dice: Dice, viewport: Size)

object Shared:

  /** Before the first frame, when Indigo gives `setup` only the dice: the clock is at zero and the
    * viewport is the design size (the configured 960x640 window at 2x). The real viewport arrives
    * with the first frame.
    */
  def atStartup(dice: Dice): Shared =
    Shared(now = Seconds.zero, dice = dice, viewport = Layout.design)

  def fromContext(context: Context): Shared =
    Shared(
      now = context.frame.time.running,
      dice = context.frame.dice,
      viewport = context.frame.viewport.size / Layout.magnification
    )
