package rpg.scene.worldscene.subui

import indigo.*
import rpg.common.util.Dialogue.*
import rpg.scene.worldscene.Type.OpenDialogue
import rpg.theme.Palette
import rpg.ui.LabelUI.labelUI
import rpg.ui.PanelUI.panelUI

object DialogueBoxUI:

  def dialogueBoxUI(open: OpenDialogue, now: Seconds, viewport: Size): Batch[SceneNode] =
    val d     = open.dialogue
    val box   = Rectangle(viewport.width / 2 - 228, viewport.height - 76, 456, 64)
    val textX = box.x + 8

    panelUI(box) ++
      Batch(
        labelUI(d.speaker, textX, box.y + 6, Palette.gold),
        labelUI(currentLine(d), textX, box.y + 26, Palette.text),
        labelUI(s"${d.index + 1}/${d.lines.length}", textX, box.y + 46, Palette.muted)
      ) ++
      (if (now.toDouble * 2).toInt % 2 == 0 then
         Batch(labelUI("SPACE", box.right - 64, box.y + 46, Palette.muted))
       else Batch.empty)
