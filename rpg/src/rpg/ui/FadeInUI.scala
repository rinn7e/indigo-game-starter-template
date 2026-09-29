package rpg.ui

import indigo.*
import rpg.common.constant.Layers

object FadeInUI:

  val duration: Seconds = Seconds(0.3)

  /** A black veil over the whole screen that fades out over `duration` after a scene is entered.
    * Drawn on the ui layer, so the FPS counter stays visible. Merge it into a scene's UI with
    * `|+|`.
    */
  def fadeInUI(enteredAt: Seconds, now: Seconds, viewport: Size): SceneUpdateFragment =
    val progress = (now - enteredAt).toDouble / duration.toDouble

    if progress >= 1 then SceneUpdateFragment.empty
    else
      SceneUpdateFragment(
        Layers.ui -> Layer.Content(
          Quad(Rectangle(viewport), Fill.Color(RGBA.Black.withAlpha(1 - progress.max(0))))
        )
      )
