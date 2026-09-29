package game.ui

import indigo.*
import game.common.constant.{Layers, Layout}

object ScreenUI:

  private def designOffset(viewport: Size): Point =
    ((viewport - Layout.design) / 2).toPoint

  /** A fixed-layout screen (title, battle, end): the backdrop fills the whole viewport, and the
    * content, laid out in `Layout.design` space, is centred within it.
    */
  def screenUI(
      viewport: Size,
      backdrop: Fill,
      world: Batch[SceneNode],
      ui: Batch[SceneNode]
  ): SceneUpdateFragment =
    val offset = designOffset(viewport)
    SceneUpdateFragment(
      Layers.world -> Layer.Content(
        Batch(Quad(Rectangle(viewport), backdrop), Group(world).moveTo(offset))
      ),
      Layers.ui -> Layer.Content(Group(ui).moveTo(offset))
    ).withMagnification(Magnification(Layout.magnification))
