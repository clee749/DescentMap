package pilot;

import structure.Room;

public class ShotPilot extends Pilot {

  public ShotPilot(double object_radius, Room current_room) {
    super(object_radius, current_room);
  }

  @Override
  public PilotMove findNextMove(double current_x, double current_y, double current_direction) {
    return new PilotMove(MoveDirection.FORWARD, TurnDirection.NONE);
  }
}
