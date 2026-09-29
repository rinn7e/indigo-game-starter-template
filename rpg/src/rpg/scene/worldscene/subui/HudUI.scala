package rpg.scene.worldscene.subui

import indigo.*
import rpg.common.Types.Quest
import rpg.common.util.Character.*
import rpg.scene.worldscene.Type.Model
import rpg.theme.Palette
import rpg.ui.BarUI.barUI
import rpg.ui.LabelUI.labelUI
import rpg.ui.PanelUI.panelUI

object HudUI:

  def hudUI(model: Model, viewport: Size): Batch[SceneNode] =
    val p = model.player

    val statsUI =
      panelUI(Rectangle(8, 20, 216, 36)) ++
        Batch(
          labelUI(s"LV ${p.level}", 14, 23, Palette.gold),
          labelUI(s"HP ${p.stats.hp}/${p.stats.maxHp}", 64, 23, Palette.text),
          labelUI(s"G ${p.gold}", 164, 23, Palette.gold)
        ) ++
        barUI(Point(14, 41), 96, p.stats.hp, p.stats.maxHp, Palette.good) ++
        barUI(Point(116, 41), 44, p.xp, xpToNextLevel(p), Palette.highlight) ++
        Batch(labelUI(s"P ${p.potions}", 170, 38, Palette.danger))

    val objective =
      model.quest match
        case Quest.NotStarted   => "TALK TO THE ELDER"
        case Quest.SlayTheKing  => "DEFEAT THE SLIME KING"
        case Quest.KingDefeated => "RETURN TO THE ELDER"
        case Quest.Complete     => "QUEST COMPLETE"

    val questX = viewport.width - 236

    val questUI =
      panelUI(Rectangle(questX, 20, 228, 36)) ++
        Batch(
          labelUI("QUEST", questX + 6, 23, Palette.muted),
          labelUI(objective, questX + 6, 38, Palette.text)
        )

    statsUI ++ questUI
