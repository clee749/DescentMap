package component;

import java.awt.Point;
import java.awt.geom.Point2D;

import mapobject.scenery.Entrance;
import mapobject.scenery.Exit;
import mapobject.unit.Unit;
import structure.DescentMap;
import structure.Room;
import util.MapUtils;
import util.RobotFactory;

import common.DescentMapException;
import common.ObjectType;
import common.RoomSide;

public class MapPopulator {
  public static int ALL_ROBOTS_MIN_ROOM_AREA = 50;
  public static double ROBOT_PLACEMENT_PROB = 0.5;
  public static ObjectType[] ROBOTS_TO_POPULATE = {ObjectType.Bomber, ObjectType.Class1Drone,
          ObjectType.Class2Drone, ObjectType.DefenseRobot, ObjectType.HeavyDriller, ObjectType.HeavyHulk,
          ObjectType.LightHulk, ObjectType.MediumHulk, ObjectType.PlatformLaser, ObjectType.PlatformMissile,
          ObjectType.SecondaryLifter, ObjectType.Spider};

  protected MapPopulator() {

  }

  public static void populateMap(DescentMap map) {
    placeEntrance(map);
    placeExit(map);
    Room exit_room = map.getExitRoom();
    Room exterior_room = map.getExteriorRoom();
    for (Room room : map.getAllRooms()) {
      if (room.equals(exit_room) || room.equals(exterior_room)) {
        continue;
      }
      placeRobotsInRoom(room);
    }
  }

  public static void placeEntrance(DescentMap map) {
    Room entrance_room = map.getEntranceRoom();
    Point nw_corner = entrance_room.getNWCorner();
    Point se_corner = entrance_room.getSECorner();
    Entrance entrance =
            new Entrance(entrance_room, (nw_corner.x + se_corner.x) / 2.0, (nw_corner.y + se_corner.y) / 2.0,
                    RoomSide.opposite(map.getEntranceSide()));
    entrance_room.addChild(entrance);
    map.setEntrance(entrance);
    map.setCenterObject(entrance);
  }

  public static void placeExit(DescentMap map) {
    Room exit_room = map.getExitRoom();
    Point nw_corner = exit_room.getNWCorner();
    Point se_corner = exit_room.getSECorner();
    double x_loc;
    double y_loc;
    switch (map.getExitSide()) {
      case NORTH:
        x_loc = se_corner.x - 0.5;
        y_loc = se_corner.y - 0.5;
        break;
      case SOUTH:
        x_loc = nw_corner.x + 0.5;
        y_loc = nw_corner.y + 0.5;
        break;
      case WEST:
        x_loc = se_corner.x - 0.5;
        y_loc = se_corner.y - 0.5;
        break;
      case EAST:
        x_loc = nw_corner.x + 0.5;
        y_loc = nw_corner.y + 0.5;
        break;
      default:
        throw new DescentMapException("Unexpected RoomSide: " + map.getExitSide());
    }
    exit_room.addChild(new Exit(exit_room, x_loc, y_loc));
  }

  public static void placeRobotsInRoom(Room room) {
    int room_area = room.getHeight() * room.getWidth();
    for (ObjectType type : ROBOTS_TO_POPULATE) {
      if (Math.random() < ROBOT_PLACEMENT_PROB &&
              (Unit.getStartingShields(type) <= room_area || room_area > ALL_ROBOTS_MIN_ROOM_AREA)) {
        placeRobotInRoom(room, type);
      }
    }
  }

  public static void placeRobotInRoom(Room room, ObjectType type) {
    Point2D.Double location =
            MapUtils.randomInternalPoint(room.getNWCorner(), room.getSECorner(), Unit.getRadius(type));
    room.addChild(RobotFactory.newRobot(type, room, location.x, location.y, Math.random() * MapUtils.TWO_PI));
  }
}
