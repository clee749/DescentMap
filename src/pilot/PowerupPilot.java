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
      return new PilotAction();
    }

    if (object.getMoveSpeed() <= 0.0) {
      state = PowerupPilotState.INACTIVE;
    }

    switch (state) {
      case INACTIVE:
        return new PilotAction();
      case DRIFT:
        return new PilotAction(MoveDirection.FORWARD);
      default:
        throw new DescentMapException("Unexpected PowerupPilotState: " + state);
    }
  }
}
