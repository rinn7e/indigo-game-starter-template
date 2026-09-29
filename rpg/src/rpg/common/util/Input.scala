package rpg.common.util

import indigo.*
import rpg.common.Types.*

object Input:

  def isConfirm(e: GlobalEvent): Boolean =
    e match
      case KeyboardEvent.KeyUp(Key.SPACE) => true
      case KeyboardEvent.KeyUp(Key.ENTER) => true
      case KeyboardEvent.KeyUp(Key.KEY_Z) => true
      case _                              => false

  def direction(key: Key): Option[Direction] =
    key match
      case Key.ARROW_UP | Key.KEY_W    => Some(Direction.Up)
      case Key.ARROW_DOWN | Key.KEY_S  => Some(Direction.Down)
      case Key.ARROW_LEFT | Key.KEY_A  => Some(Direction.Left)
      case Key.ARROW_RIGHT | Key.KEY_D => Some(Direction.Right)
      case _                           => None

  /** The direction key currently held down, if any. */
  def heldDirection(keyboard: Keyboard): Option[Direction] =
    if keyboard.keysAreDown(Key.ARROW_UP) || keyboard.keysAreDown(Key.KEY_W) then Some(Direction.Up)
    else if keyboard.keysAreDown(Key.ARROW_DOWN) || keyboard.keysAreDown(Key.KEY_S) then
      Some(Direction.Down)
    else if keyboard.keysAreDown(Key.ARROW_LEFT) || keyboard.keysAreDown(Key.KEY_A) then
      Some(Direction.Left)
    else if keyboard.keysAreDown(Key.ARROW_RIGHT) || keyboard.keysAreDown(Key.KEY_D) then
      Some(Direction.Right)
    else None
