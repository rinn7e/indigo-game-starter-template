package rpg.scene.battlescene.subui.actionmenu

import rpg.scene.battlescene.subui.actionmenu.Type.*

object Update:

  // -----------------------------------------------------------------
  // Init
  // -----------------------------------------------------------------

  def init: Model =
    Model(selected = 0)

  // -----------------------------------------------------------------
  // Update
  // -----------------------------------------------------------------

  def update(msg: Msg, model: Model): Model =
    msg match
      case Msg.CursorUp =>
        model.copy(selected = (model.selected + Action.menu.length - 1) % Action.menu.length)

      case Msg.CursorDown =>
        model.copy(selected = (model.selected + 1) % Action.menu.length)

      case Msg.Choose =>
        model

  def selectedAction(model: Model): Action =
    Action.menu(model.selected)
