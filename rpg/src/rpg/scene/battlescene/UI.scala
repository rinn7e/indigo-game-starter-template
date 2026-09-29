package rpg.scene.battlescene

import indigo.*
import rpg.common.Types.*
import rpg.scene.battlescene.subui.ActionMenu
import rpg.scene.battlescene.subui.BattleLogUI.battleLogUI
import rpg.scene.battlescene.subui.StatusPanelUI.statusPanelUI
import rpg.theme.Palette
import rpg.ui.CharacterSpriteUI.{playerSpriteUI, slimeSpriteUI}
import rpg.ui.FadeInUI.fadeInUI
import rpg.ui.LabelUI.{centeredLabelUI, labelUI}
import rpg.ui.PanelUI.panelUI
import rpg.ui.ScreenUI.screenUI
import rpg.scene.battlescene.Type.*

object UI:

  private def shake(hit: Option[(Boolean, Seconds)], enemyWasHit: Boolean, now: Seconds): Point =
    hit match
      case Some((onEnemy, at)) if onEnemy == enemyWasHit && now < at + Seconds(0.3) =>
        Point((Math.sin(now.toDouble * 80) * 4).toInt, 0)

      case _ =>
        Point.zero

  def ui(shared: Shared, model: Model): SceneUpdateFragment =
    val now        = shared.now
    val enemyAlive = model.phase != Phase.Won
    val playerUp   = model.phase != Phase.Lost

    val sky =
      Fill.LinearGradient(
        Point(0, 0),
        RGBA.fromHexString("#355c7d"),
        Point(0, shared.viewport.height * 2 / 3),
        RGBA.fromHexString("#6c9a5b")
      )

    val shadowsUI =
      Batch(
        Quad(Rectangle(280, 160, 160, 18), Fill.Color(RGBA.Black.withAlpha(0.2)), Corners(9)),
        Quad(Rectangle(40, 200, 140, 16), Fill.Color(RGBA.Black.withAlpha(0.2)), Corners(8))
      )

    val enemyUI =
      if enemyAlive then
        Batch(
          slimeSpriteUI(model.enemy.kind, now, 0)
            .withScale(Vector2(6, 6))
            .moveTo(Point(312, 76) + shake(model.lastHit, true, now))
        )
      else Batch.empty

    val heroUI =
      if playerUp then
        Batch(
          playerSpriteUI(Direction.Up, 0)
            .withScale(Vector2(5, 5))
            .moveTo(Point(70, 138) + shake(model.lastHit, false, now))
        )
      else Batch.empty

    screenUI(
      shared.viewport,
      sky,
      shadowsUI ++ enemyUI ++ heroUI,
      statusPanelUI(model) ++ battleLogUI(model) ++ controlsUI(model, now) ++
        fightBannerUI(model, now)
    ) |+| fadeInUI(model.enteredAt, now, shared.viewport)

  /** Shown briefly once `Msg.FightStarted` arrives (see `BattleEvent.FightStarted`). */
  private def fightBannerUI(model: Model, now: Seconds): Batch[SceneNode] =
    model.fightStartedAt match
      case Some(at) if now < at + Seconds(0.8) =>
        panelUI(Rectangle(170, 104, 140, 32)) ++
          Batch(centeredLabelUI("FIGHT!", 114, Palette.gold))

      case _ =>
        Batch.empty

  /** The action menu during the fight; a "continue" prompt once it's over. */
  private def controlsUI(model: Model, now: Seconds): Batch[SceneNode] =
    model.phase match
      case Phase.PlayerTurn | Phase.EnemyTurn(_) =>
        ActionMenu.ui(model.menu, active = model.phase == Phase.PlayerTurn, model.player.potions)

      case Phase.Won | Phase.Escaped | Phase.Lost =>
        panelUI(Rectangle(326, 232, 144, 78)) ++
          Batch(labelUI("CONTINUE", 340, 250, Palette.text)) ++
          (if (now.toDouble * 2).toInt % 2 == 0 then Batch(labelUI("SPACE", 340, 274, Palette.gold))
           else Batch.empty)
