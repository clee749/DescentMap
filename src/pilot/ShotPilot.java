package pilot;

public class ShotPilot extends Pilot {
  @Override
  public PilotAction findNextAction(double s_elapsed) {
    return PilotAction.MOVE_FORWARD;
  }
}
