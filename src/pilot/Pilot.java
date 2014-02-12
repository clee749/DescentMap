package pilot;

import mapobject.MovableObject;
import structure.Room;

public abstract class Pilot {
  public static final double DIRECTION_EPSILON = Math.PI / 32;
  public static final double SHOT_EVASION_THRESHOLD = Math.PI / 16;

  protected MovableObject bound_object;
  protected double bound_object_radius;
  protected double bound_object_diameter;
  protected Room current_room;

  public void bindToObject(MovableObject object) {
    bound_object = object;
    current_room = object.getRoom();
    bound_object_radius = object.getRadius();
    bound_object_diameter = 2 * bound_object_radius;
  }

  public void updateCurrentRoom(Room room) {
    current_room = room;
  }

  public TurnDirection angleToTurnDirection(double angle) {
    if (Math.abs(angle) < Pilot.DIRECTION_EPSILON) {
      return TurnDirection.NONE;
    }
    return (angle < 0 ? TurnDirection.COUNTER_CLOCKWISE : TurnDirection.CLOCKWISE);
  }

  public abstract PilotAction findNextAction(double s_elapsed);
}
