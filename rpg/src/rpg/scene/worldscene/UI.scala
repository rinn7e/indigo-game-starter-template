package rpg.scene.worldscene

import indigo.*
import rpg.common.constant.{Layers, Layout}
import rpg.common.Types.Shared
import rpg.scene.worldscene.common.Util.camera
import rpg.ui.FadeInUI.fadeInUI
import rpg.scene.worldscene.subui.DialogueBoxUI.dialogueBoxUI
import rpg.scene.worldscene.subui.EntitiesUI.entitiesUI
import rpg.scene.worldscene.subui.HudUI.hudUI
import rpg.scene.worldscene.subui.TerrainUI.{terrainCloneBlankUI, terrainUI}
import rpg.scene.worldscene.subui.ToastBannerUI.toastBannerUI
import rpg.scene.worldscene.Type.*

object UI:

  private val beyondTheMap: Fill = Fill.Color(RGBA.fromHexString("#1f4d38"))

  def ui(shared: Shared, model: Model): SceneUpdateFragment =
    val now      = shared.now
    val viewport = shared.viewport
    val cam      = camera(model, now, viewport)

    val worldLayerUI =
      Layer
        .Content(
          Batch(Quad(Rectangle(cam, viewport), beyondTheMap)) ++
            terrainUI(model.map, now) ++
            entitiesUI(model, now)
        )
        .withCamera(Camera.Fixed(cam))

    val overlayUI =
      model.dialogue match
        case Some(open) => dialogueBoxUI(open, now, viewport)
        case None       => model.toast.map(toastBannerUI(_, viewport)).getOrElse(Batch.empty)

    SceneUpdateFragment(
      Layers.world -> worldLayerUI,
      Layers.ui    -> Layer.Content(hudUI(model, viewport) ++ overlayUI)
    )
      .addCloneBlanks(terrainCloneBlankUI)
      .withMagnification(Magnification(Layout.magnification))
      |+| fadeInUI(model.enteredAt, now, viewport)
