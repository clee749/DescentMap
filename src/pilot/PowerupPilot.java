package pilot;

import common.DescentMapException;

enum PowerupPilotState {
  INACTIVE,
  DRIFT;
}


public class PowerupPilot extends Pilot {
  private PowerupPilotState state;

  public PowerupPilot() {
    state = PowerupPilotState.DRIFT;
  }

  @Override
  public PilotAction findNextAction(double s_elapsed) {
    if (state.equals(PowerupPilotState.INACTIVE)) {
      return PilotAction.NO_ACTION;
    }

    if (bound_object.getMoveSpeed() <= 0.0) {
      state = PowerupPilotState.INACTIVE;
    }

    switch (state) {
      case INACTIVE:
        return PilotAction.NO_ACTION;
      case DRIFT:
        return PilotAction.MOVE_FORWARD;
      default:
        throw new DescentMapException("Unexpected PowerupPilotState: " + state);
    }
  }
}
