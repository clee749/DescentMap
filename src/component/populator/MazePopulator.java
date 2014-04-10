package component.populator;

import java.awt.Point;
import java.util.HashSet;

import mapobject.scenery.Entrance;
import mapobject.scenery.RobotGenerator;
import mapobject.unit.Unit;
import structure.DescentMap;
import structure.Room;
import structure.RoomConnection;

import common.ObjectType;
import common.RoomSide;
import component.builder.MazeBuilder;

public class MazePopulator extends StandardPopulator {
  public static final double MAX_ROBOT_RADIUS = MazeBuilder.HALLWAY_WIDTH / 2.0;

  public MazePopulator(DescentMap map) {
    super(map);
  }

  @Override
  public boolean canPlaceRobotGenerator(Room room, HashSet<Room> restricted_rooms) {
    return room.getSceneries().isEmpty() && !restricted_rooms.contains(room) &&
            (room.getHeight() > 4 || room.getWidth() > 4);
  }

  @Override
  public void placeRobotGenerator(Room room) {
    Point nw_corner = room.getNWCorner();
    double x_loc;
    double y_loc;
    boolean location_found;
    do {
      x_loc = (int) (Math.random() * (room.getWidth() - 1)) + nw_corner.x + 0.5;
      y_loc = (int) (Math.random() * (room.getHeight() - 1)) + nw_corner.y + 0.5;
      location_found = !isLocationNextToRoomConnection(room, x_loc, y_loc);
    } while (!location_found);
    room.addChild(new RobotGenerator(room, x_loc, y_loc,
            ROBOT_GENERATOR_TYPES[(int) (Math.random() * ROBOT_GENERATOR_TYPES.length)],
            RoomSide.values()[(int) (Math.random() * RoomSide.values().length)]));
  }

  public boolean isLocationNextToRoomConnection(Room room, double x_loc, double y_loc) {
    Point nw_corner = room.getNWCorner();
    Point se_corner = room.getSECorner();
    RoomConnection connection = room.getConnectionInDirection(RoomSide.WEST);
    if (connection != null && x_loc - 1 < nw_corner.x && connection.min < y_loc && y_loc < connection.max) {
      return true;
    }
    connection = room.getConnectionInDirection(RoomSide.EAST);
    if (connection != null && x_loc + 1 > se_corner.x && connection.min < y_loc && y_loc < connection.max) {
      return true;
    }
    connection = room.getConnectionInDirection(RoomSide.NORTH);
    if (connection != null && y_loc - 1 < nw_corner.y && connection.min < x_loc && x_loc < connection.max) {
      return true;
    }
    connection = room.getConnectionInDirection(RoomSide.SOUTH);
    if (connection != null && y_loc + 1 > se_corner.y && connection.min < x_loc && x_loc < connection.max) {
      return true;
    }
    return false;
  }

  @Override
  public void placeRobotsInRoom(Room room, Entrance entrance) {
    ObjectType type;
    do {
      type = ObjectType.ROBOTS[(int) (Math.random() * ObjectType.ROBOTS.length)];
    } while (Unit.getRadius(type) > MAX_ROBOT_RADIUS);
    placeRobotInRoom(room, type, entrance);
  }
}
