package pilot;

import java.util.Map.Entry;

import mapobject.MapObject;
import mapobject.shot.Shot;
import mapobject.unit.Unit;
import structure.Room;
import structure.RoomConnection;
import util.MapUtils;

import common.DescentMapException;
import common.RoomSide;

public abstract class UnitPilot extends Pilot {
  public static final double SHOT_EVASION_THRESHOLD = Math.PI / 16;

  protected Entry<RoomSide, RoomConnection> target_room_info;
  protected double target_x;
  protected double target_y;
  protected double target_direction;
  protected Unit target_unit;
  protected Room target_object_room;
  protected Entry<RoomSide, RoomConnection> target_object_room_info;

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

  public void setTargetLocation(MapObject target) {
    target_x = target.getX();
    target_y = target.getY();
  }

  public void setTargetDirection(double target_direction) {
    this.target_direction = target_direction;
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
    setTargetDirection(MapUtils.absoluteAngleTo(bound_object.getX(), bound_object.getY(), target_x, target_y));
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
}
