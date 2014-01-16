package pilot;

import java.util.Map.Entry;

import mapobject.MapObject;
import mapobject.MovableObject;
import mapobject.shot.Shot;
import structure.Room;
import structure.RoomConnection;
import util.MapUtils;

import common.Constants;
import common.DescentMapException;
import common.RoomSide;

public abstract class Pilot {
  protected MovableObject object;
  protected double object_radius;
  protected double object_diameter;
  protected Room current_room;
  protected Entry<RoomSide, RoomConnection> target_room_info;
  protected double target_x;
  protected double target_y;
  protected double target_direction;
  protected MapObject target_object;
  protected Room target_object_room;
  protected Entry<RoomSide, RoomConnection> target_object_room_info;

  public void bindToObject(MovableObject object) {
    this.object = object;
    current_room = object.getRoom();
    object_radius = object.getRadius();
    object_diameter = 2 * object_radius;
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
    setTargetDirection(-MapUtils.absoluteAngleTo(object.getX(), object.getY(), target_x, target_y));
  }

  public boolean canSeeLocationInNeighborRoom(double location_x, double location_y, RoomSide neighbor_side,
          RoomConnection connection) {
    double current_x = object.getX();
    double current_y = object.getY();
    double neighbor_direction = RoomSide.directionToRadians(neighbor_side);
    double angle_to_location =
            MapUtils.angleTo(neighbor_direction, location_x - current_x, location_y - current_y);
    double angle_to_connection_min;
    double angle_to_connection_max;
    switch (neighbor_side) {
      case NORTH:
        double dy = current_room.getNWCorner().y - current_y;
        angle_to_connection_min = MapUtils.angleTo(neighbor_direction, connection.min - current_x, dy);
        angle_to_connection_max = MapUtils.angleTo(neighbor_direction, connection.max - current_x, dy);
        break;
      case SOUTH:
        dy = current_room.getSECorner().y - current_y;
        angle_to_connection_min = MapUtils.angleTo(neighbor_direction, connection.min - current_x, dy);
        angle_to_connection_max = MapUtils.angleTo(neighbor_direction, connection.max - current_x, dy);
        break;
      case WEST:
        double dx = current_room.getNWCorner().x - current_x;
        angle_to_connection_min = MapUtils.angleTo(neighbor_direction, dx, connection.min - current_y);
        angle_to_connection_max = MapUtils.angleTo(neighbor_direction, dx, connection.max - current_y);
        break;
      case EAST:
        dx = current_room.getSECorner().x - current_x;
        angle_to_connection_min = MapUtils.angleTo(neighbor_direction, dx, connection.min - current_y);
        angle_to_connection_max = MapUtils.angleTo(neighbor_direction, dx, connection.max - current_y);
        break;
      default:
        throw new DescentMapException("Unexpected RoomSide: " + neighbor_side);
    }
    return MapUtils.isAngleBetween(angle_to_location, angle_to_connection_min, angle_to_connection_max);
  }

  public StrafeDirection reactToShots() {
    for (Shot shot : current_room.getShots()) {
      if (shot.getSource().equals(object)) {
        continue;
      }
      double angle_to_object = MapUtils.angleTo(shot, object);
      if (Math.abs(angle_to_object) < Constants.PILOT_SHOT_EVASION_THRESHOLD) {
        if (Math.abs(MapUtils.angleTo(shot.getDirection(), object.getDirection())) < MapUtils.PI_OVER_TWO) {
          angle_to_object *= -1;
        }
        return (angle_to_object < 0 ? StrafeDirection.LEFT : StrafeDirection.RIGHT);
      }
    }
    return StrafeDirection.NONE;
  }

  public abstract PilotAction findNextAction(double s_elapsed);
}
