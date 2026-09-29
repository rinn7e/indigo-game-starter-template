package game.scene.worldscene.common

import indigo.*
import game.common.constant.Layout
import game.common.Types.*
import game.common.util.TileMap.*
import game.scene.worldscene.Type.Model

object Util:

  // Queries on the world model. The model comes last (`enemyAt(pt)(model)`), Elm / Haskell style.

  def enemyAt(pt: Point)(model: Model): Option[Enemy] =
    model.enemies.find(_.position == pt)

  def chestAt(pt: Point)(model: Model): Option[Chest] =
    model.chests.find(_.position == pt)

  def isOccupied(pt: Point)(model: Model): Boolean =
    model.elder.position == pt || chestAt(pt)(model).isDefined || enemyAt(pt)(model).isDefined

  def canEnter(pt: Point)(model: Model): Boolean =
    isWalkable(pt)(model.map) && !isOccupied(pt)(model)

  def withToast(message: String, now: Seconds)(model: Model): Model =
    model.copy(toast = Some(Toast(message, now + Seconds(2.5))))

  /** Pixel position of something on the grid, tweened while a step is in progress. */
  def renderPosition(position: Point, step: Option[Step], now: Seconds): Point =
    step match
      case Some(s) if now < s.startedAt + Step.duration =>
        val t    = ((now - s.startedAt).toDouble / Step.duration.toDouble).min(1.0)
        val from = s.from * Layout.tileSize
        val to   = position * Layout.tileSize
        Point(
          (from.x + (to.x - from.x) * t).toInt,
          (from.y + (to.y - from.y) * t).toInt
        )

      case _ =>
        position * Layout.tileSize

  /** Top-left of the visible area, in world pixels. Follows the player, stops at the map edges, and
    * centres the map when the window is bigger than the map.
    */
  def camera(model: Model, now: Seconds, viewport: Size): Point =
    val focus =
      renderPosition(model.player.position, model.player.step, now) + Point(Layout.tileSize / 2)
    val mapPx = model.map.size.toPoint * Layout.tileSize

    def axis(focus: Int, map: Int, visible: Int): Int =
      if map <= visible then (map - visible) / 2
      else (focus - visible / 2).max(0).min(map - visible)

    Point(axis(focus.x, mapPx.x, viewport.width), axis(focus.y, mapPx.y, viewport.height))
