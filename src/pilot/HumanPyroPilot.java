package pilot;

import java.awt.event.KeyEvent;

import pyro.PyroPrimaryCannon;
import pyro.PyroSecondaryCannon;

public class HumanPyroPilot extends PyroPilot {
  private boolean is_moving_forward;
  private boolean is_moving_backward;
  private boolean is_strafing_left;
  private boolean is_strafing_right;
  private boolean is_turning_counter_clockwise;
  private boolean is_turning_clockwise;
  private boolean is_firing_cannon;
  private boolean is_firing_secondary;
  private boolean is_dropping_bomb;

  private boolean is_ready_for_respawn;

  @Override
  public void newLevel() {

  }

  @Override
  public void startPilot() {
    is_moving_forward = false;
    is_moving_backward = false;
    is_strafing_left = false;
    is_strafing_right = false;
    is_turning_counter_clockwise = false;
    is_turning_clockwise = false;
    is_firing_cannon = false;
    is_firing_secondary = false;
    is_dropping_bomb = false;
  }

  @Override
  public boolean isReadyToRespawn() {
    return is_ready_for_respawn;
  }

  @Override
  public void handleRespawnDelay(double s_elapsed) {

  }

  @Override
  public void prepareForRespawn() {
    is_ready_for_respawn = false;
  }

  @Override
  public PilotAction findNextAction(double s_elapsed) {
    MoveDirection move = MoveDirection.NONE;
    if (is_moving_forward) {
      if (!is_moving_backward) {
        move = MoveDirection.FORWARD;
      }
    }
    else if (is_moving_backward) {
      move = MoveDirection.BACKWARD;
    }

    StrafeDirection strafe = StrafeDirection.NONE;
    if (is_strafing_left) {
      if (!is_strafing_right) {
        strafe = StrafeDirection.LEFT;
      }
    }
    else if (is_strafing_right) {
      strafe = StrafeDirection.RIGHT;
    }

    TurnDirection turn = TurnDirection.NONE;
    if (is_turning_counter_clockwise) {
      if (!is_turning_clockwise) {
        turn = TurnDirection.COUNTER_CLOCKWISE;
      }
    }
    else if (is_turning_clockwise) {
      turn = TurnDirection.CLOCKWISE;
    }

    PilotAction action =
            new PilotAction(move, strafe, turn, is_firing_cannon, is_firing_secondary, is_dropping_bomb);
    is_dropping_bomb = false;
    return action;
  }

  public void handleKeyPressed(int key_code) {
    is_ready_for_respawn = true;
    if (!setCommand(key_code, true)) {
      switch (key_code) {
        case KeyEvent.VK_1:
          bound_pyro.switchPrimaryCannon(PyroPrimaryCannon.LASER, true);
          break;
        case KeyEvent.VK_3:
          bound_pyro.switchPrimaryCannon(PyroPrimaryCannon.SPREADFIRE, true);
          break;
        case KeyEvent.VK_4:
          bound_pyro.switchPrimaryCannon(PyroPrimaryCannon.PLASMA, true);
          break;
        case KeyEvent.VK_5:
          bound_pyro.switchPrimaryCannon(PyroPrimaryCannon.FUSION, true);
          break;
        case KeyEvent.VK_6:
          bound_pyro.switchSecondaryCannon(PyroSecondaryCannon.CONCUSSION_MISSILE, true);
          break;
        case KeyEvent.VK_7:
          bound_pyro.switchSecondaryCannon(PyroSecondaryCannon.HOMING_MISSILE, true);
          break;
        case KeyEvent.VK_8:
          bound_pyro.switchSecondaryCannon(PyroSecondaryCannon.PROXIMITY_BOMB, true);
          break;
      }
    }
  }

  public void handleKeyReleased(int key_code) {
    setCommand(key_code, false);
  }

  public boolean setCommand(int key_code, boolean value) {
    switch (key_code) {
      case KeyEvent.VK_A:
        is_moving_forward = value;
        break;
      case KeyEvent.VK_Z:
        is_moving_backward = value;
        break;
      case KeyEvent.VK_X:
        is_strafing_left = value;
        break;
      case KeyEvent.VK_V:
        is_strafing_right = value;
        break;
      case KeyEvent.VK_LEFT:
        is_turning_clockwise = value;
        break;
      case KeyEvent.VK_RIGHT:
        is_turning_counter_clockwise = value;
        break;
      case KeyEvent.VK_CONTROL:
        is_firing_cannon = value;
        break;
      case KeyEvent.VK_SPACE:
        is_firing_secondary = value;
        break;
      case KeyEvent.VK_B:
        is_dropping_bomb = value;
        break;
      default:
        return false;
    }
    return true;
  }
}
