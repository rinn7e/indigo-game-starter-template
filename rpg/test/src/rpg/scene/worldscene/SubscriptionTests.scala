package rpg.scene.worldscene

import indigo.*
import rpg.common.Types.{Direction, Shared}
import rpg.common.constant.Maps
import rpg.scene.worldscene.Type.*
import rpg.scene.worldscene.Update.*
import rpg.scene.worldscene.Subscription.*

class SubscriptionTests extends munit.FunSuite {

  val subs: GlobalEvent => Option[Msg] =
    subscriptions(
      init(Shared.atStartup(Dice.loaded(1)), Maps.overworld).unsafeGet,
      Keyboard.default
    )

  test("a quick key tap becomes a walk, even if no key is held on the next frame") {
    assertEquals(subs(KeyboardEvent.KeyDown(Key.ARROW_DOWN)), Some(Msg.Walk(Direction.Down)))
    assertEquals(subs(KeyboardEvent.KeyDown(Key.KEY_A)), Some(Msg.Walk(Direction.Left)))
  }

  test("every frame ticks, carrying the held direction") {
    assertEquals(subs(FrameTick), Some(Msg.Tick(None)))
  }

  test("space, enter and z confirm") {
    assertEquals(subs(KeyboardEvent.KeyUp(Key.SPACE)), Some(Msg.Confirm))
    assertEquals(subs(KeyboardEvent.KeyUp(Key.ENTER)), Some(Msg.Confirm))
    assertEquals(subs(KeyboardEvent.KeyUp(Key.KEY_Z)), Some(Msg.Confirm))
  }

  test("other input is ignored") {
    assertEquals(subs(KeyboardEvent.KeyDown(Key.KEY_Q)), None)
  }
}
