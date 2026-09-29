package rpg.scene.worldscene.subui

import indigo.*
import rpg.common.Types.Toast
import rpg.theme.Palette
import rpg.ui.LabelUI.centeredLabelAtUI
import rpg.ui.PanelUI.panelUI

object ToastBannerUI:

  def toastBannerUI(toast: Toast, viewport: Size): Batch[SceneNode] =
    val centreX = viewport.width / 2
    val bottom  = viewport.height
    val width   = toast.message.length * 10 + 20

    panelUI(Rectangle(centreX - width / 2, bottom - 38, width, 24)) ++
      Batch(centeredLabelAtUI(toast.message, centreX, bottom - 32, Palette.gold))
