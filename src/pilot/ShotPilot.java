package pilot;

public class ShotPilot extends Pilot {
  @Override
  public PilotMove findNextMove(double s_elapsed) {
    return new PilotMove(MoveDirection.FORWARD, TurnDirection.NONE);
  }
}
