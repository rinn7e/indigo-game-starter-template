package game.scene.battlescene.subui.actionmenu

import indigo.*
import game.scene.battlescene.subui.actionmenu.Type.*
import game.scene.battlescene.subui.actionmenu.Update.*
import game.scene.battlescene.subui.actionmenu.Subscription.*

class UpdateTests extends munit.FunSuite {

  test("the cursor moves and wraps around") {
    assertEquals(selectedAction(update(Msg.CursorDown, init)), Action.Potion)
    assertEquals(selectedAction(update(Msg.CursorUp, init)), Action.Run)
  }

  test("choosing keeps the selection, for the parent to read") {
    val onRun = update(Msg.CursorUp, init)
    assertEquals(update(Msg.Choose, onRun), onRun)
  }

  test("arrow keys, WASD and confirm keys become menu messages") {
    val subs = subscriptions(init, Keyboard.default)
    assertEquals(subs(KeyboardEvent.KeyDown(Key.ARROW_UP)), Some(Msg.CursorUp))
    assertEquals(subs(KeyboardEvent.KeyDown(Key.KEY_S)), Some(Msg.CursorDown))
    assertEquals(subs(KeyboardEvent.KeyUp(Key.SPACE)), Some(Msg.Choose))
    assertEquals(subs(KeyboardEvent.KeyDown(Key.ARROW_LEFT)), None)
  }
}
