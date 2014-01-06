package pilot;

import java.util.Map.Entry;

import mapobject.MapObject;
import mapobject.MovableMapObject;
import structure.Room;
import structure.RoomConnection;
import util.MapUtils;

import common.DescentMapException;
import common.RoomSide;

public abstract class Pilot {
  protected MovableMapObject object;
  protected double object_radius;
  // we cannot just use object.getRoom because we could go into an unintentional Room while turning
  protected Room current_room;
  protected Entry<RoomSide, RoomConnection> target_room_info;
  protected double target_x;
  protected double target_y;
  protected double target_direction;
  protected MapObject target_object;

  public void bindToObject(MovableMapObject object) {
    this.object = object;
    current_room = object.getRoom();
    object_radius = object.getRadius();
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

  public void planMoveToRoomConnection(RoomSide direction) {
    RoomConnection connection = current_room.getConnectionInDirection(direction);
    double middle = (connection.min + connection.max) / 2.0;
    switch (direction) {
      case EAST:
        setTargetLocation(current_room.getSECorner().x - object_radius, middle);
        break;
      case WEST:
        setTargetLocation(current_room.getNWCorner().x + object_radius, middle);
        break;
      case NORTH:
        setTargetLocation(middle, current_room.getNWCorner().y + object_radius);
        break;
      case SOUTH:
        setTargetLocation(middle, current_room.getSECorner().y - object_radius);
        break;
      default:
        throw new DescentMapException("Unexpected RoomSide: " + direction);
    }
  }

  public void planMoveToNeighborRoom(RoomSide direction) {
    RoomConnection connection = current_room.getConnectionInDirection(direction);
    double middle = (connection.min + connection.max) / 2.0;
    switch (direction) {
      case EAST:
        setTargetLocation(current_room.getSECorner().x + object_radius, middle);
        break;
      case WEST:
        setTargetLocation(current_room.getNWCorner().x - object_radius, middle);
        break;
      case NORTH:
        setTargetLocation(middle, current_room.getNWCorner().y - object_radius);
        break;
      case SOUTH:
        setTargetLocation(middle, current_room.getSECorner().y + object_radius);
        break;
      default:
        throw new DescentMapException("Unexpected RoomSide: " + direction);
    }
  }

  public void planToTurnIntoRoom(RoomSide direction) {
    // target_directions for north and south reversed because y-coordinates increase down screen
    switch (direction) {
      case EAST:
        setTargetDirection(0.0);
        break;
      case NORTH:
        setTargetDirection(MapUtils.THREE_PI_OVER_TWO);
        break;
      case WEST:
        setTargetDirection(Math.PI);
        break;
      case SOUTH:
        setTargetDirection(MapUtils.PI_OVER_TWO);
        break;
      default:
        throw new DescentMapException("Unexpected RoomSide: " + direction);
    }
  }

  public void planToTurnToTarget() {
    setTargetDirection(-MapUtils.absoluteAngleTo(object.getX(), object.getY(), target_x, target_y));
  }

  public abstract PilotMove findNextMove(double s_elapsed);
}
