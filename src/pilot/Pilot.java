package pilot;

import java.util.Map.Entry;

import mapobject.MapObject;
import mapobject.MovableObject;
import mapobject.shot.Shot;
import structure.Room;
import structure.RoomConnection;
import util.MapUtils;

import common.DescentMapException;
import common.RoomSide;

public abstract class Pilot {
  public static final double DIRECTION_EPSILON = Math.PI / 32;
  public static final double SHOT_EVASION_THRESHOLD = Math.PI / 16;

  protected MovableObject bound_object;
  protected double bound_object_radius;
  protected double bound_object_diameter;
  protected Room current_room;
  protected Entry<RoomSide, RoomConnection> target_room_info;
  protected double target_x;
  protected double target_y;
  protected double target_direction;
  protected MapObject target_object;
  protected Room target_object_room;
  protected Entry<RoomSide, RoomConnection> target_object_room_info;

  public void bindToObject(MovableObject object) {
    bound_object = object;
    current_room = object.getRoom();
    bound_object_radius = object.getRadius();
    bound_object_diameter = 2 * bound_object_radius;
  }

  public double getTargetX() {
    return target_x;
  }

  public double getTargetY() {
    return target_y;
  }

  public void setTargetLocation(double target_x, double target_y) {
    this.target_x = target_x;
    this.target_y = target_y;
  }

  public void setTargetDirection(double target_direction) {
    this.target_direction = target_direction;
  }

  public void updateCurrentRoom(Room room) {
    current_room = room;
  }

  public void planMoveToRoomConnection(RoomSide direction, double radius) {
    RoomConnection connection = current_room.getConnectionInDirection(direction);
    double middle = (connection.min + connection.max) / 2.0;
    switch (direction) {
      case EAST:
        setTargetLocation(current_room.getSECorner().x - radius, middle);
        break;
      case WEST:
        setTargetLocation(current_room.getNWCorner().x + radius, middle);
        break;
      case NORTH:
        setTargetLocation(middle, current_room.getNWCorner().y + radius);
        break;
      case SOUTH:
        setTargetLocation(middle, current_room.getSECorner().y - radius);
        break;
      default:
        throw new DescentMapException("Unexpected RoomSide: " + direction);
    }
  }

  public void planMoveToNeighborRoom(RoomSide direction, double radius) {
    RoomConnection connection = current_room.getConnectionInDirection(direction);
    double middle = (connection.min + connection.max) / 2.0;
    switch (direction) {
      case EAST:
        setTargetLocation(current_room.getSECorner().x + radius, middle);
        break;
      case WEST:
        setTargetLocation(current_room.getNWCorner().x - radius, middle);
        break;
      case NORTH:
        setTargetLocation(middle, current_room.getNWCorner().y - radius);
        break;
      case SOUTH:
        setTargetLocation(middle, current_room.getSECorner().y + radius);
        break;
      default:
        throw new DescentMapException("Unexpected RoomSide: " + direction);
    }
  }

  public void planTurnIntoRoom(RoomSide direction) {
    setTargetDirection(RoomSide.directionToRadians(direction));
  }

  public void planTurnToTarget() {
    setTargetDirection(-MapUtils
            .absoluteAngleTo(bound_object.getX(), bound_object.getY(), target_x, target_y));
  }

  public StrafeDirection reactToShots() {
    for (Shot shot : current_room.getShots()) {
      if (shot.getSource().equals(bound_object)) {
        continue;
      }
      double angle_to_object = MapUtils.angleTo(shot, bound_object);
      if (Math.abs(angle_to_object) < SHOT_EVASION_THRESHOLD) {
        if (Math.abs(MapUtils.angleTo(shot.getDirection(), bound_object.getDirection())) < MapUtils.PI_OVER_TWO) {
          angle_to_object *= -1;
        }
        return (angle_to_object < 0 ? StrafeDirection.LEFT : StrafeDirection.RIGHT);
      }
    }
    return StrafeDirection.NONE;
  }

  public TurnDirection angleToTurnDirection(double angle) {
    double abs_angle = Math.abs(angle);
    if (abs_angle < Pilot.DIRECTION_EPSILON) {
      return TurnDirection.NONE;
    }
    return (angle < 0 ? TurnDirection.COUNTER_CLOCKWISE : TurnDirection.CLOCKWISE);
  }

  public abstract PilotAction findNextAction(double s_elapsed);
}
