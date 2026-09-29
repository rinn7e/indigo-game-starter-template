package rpg.ui

import indigo.*
import rpg.common.asset.GameAssets
import rpg.common.constant.Layout
import rpg.theme.Palette

object LabelUI:

  def labelUI(message: String, x: Int, y: Int, color: RGBA): SceneNode =
    GameAssets.textUI(message, x, y, color)

  def centeredLabelAtUI(message: String, x: Int, y: Int, color: RGBA): SceneNode =
    GameAssets.textUI(message, x, y, color).alignCenter

  /** Centred horizontally on a fixed-layout screen. */
  def centeredLabelUI(message: String, y: Int, color: RGBA): SceneNode =
    centeredLabelAtUI(message, Layout.designCenter.x, y, color)

  /** A prompt that blinks, driven purely by the running time. */
  def blinkingLabelUI(message: String, y: Int, time: Seconds): Batch[SceneNode] =
    if (time.toDouble * 2).toInt % 2 == 0 then Batch(centeredLabelUI(message, y, Palette.gold))
    else Batch.empty
