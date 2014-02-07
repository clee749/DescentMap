package component;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;

import mapobject.scenery.EnergyCenter;
import mapobject.scenery.Entrance;
import mapobject.scenery.Exit;
import mapobject.scenery.RobotGenerator;
import mapobject.unit.Unit;
import structure.DescentMap;
import structure.Room;
import util.MapUtils;
import util.RobotFactory;

import common.DescentMapException;
import common.ObjectType;
import common.RoomSide;

public class MapPopulator {
  // sceneries
  public static final int MAX_ROOM_AREA_FOR_ENERGY_CENTER = 10;
  public static final int ENERGY_CENTER_PLACEMENT_RADIUS = 2;
  public static final ObjectType[] ROBOT_GENERATOR_TYPES =
          {ObjectType.Bomber, ObjectType.Class2Drone, ObjectType.DefenseRobot, ObjectType.LightHulk,
                  ObjectType.PlatformLaser, ObjectType.SecondaryLifter};

  // robots
  public static final int ALL_ROBOTS_MIN_ROOM_AREA = 50;
  public static final double ROBOT_PLACEMENT_PROB = 0.5;
  public static final ObjectType[] ROBOTS_TO_POPULATE = {ObjectType.Bomber, ObjectType.Class1Drone,
          ObjectType.Class2Drone, ObjectType.DefenseRobot, ObjectType.HeavyDriller, ObjectType.HeavyHulk,
          ObjectType.LightHulk, ObjectType.MediumHulk, ObjectType.PlatformLaser, ObjectType.PlatformMissile,
          ObjectType.SecondaryLifter, ObjectType.Spider};

  protected MapPopulator() {

  }

  public static void populateMap(DescentMap map) {
    Room entrance_room = map.getEntranceRoom();
    Room exit_room = map.getExitRoom();
    Room exterior_room = map.getExteriorRoom();
    HashSet<Room> special_rooms = new HashSet<Room>();
    special_rooms.add(entrance_room);
    special_rooms.add(exit_room);
    special_rooms.add(exterior_room);
    placeEntrance(map);
    placeExit(map);
    placeEnergyCenters(map, special_rooms);
    placeRobotGenerators(map, special_rooms);
    for (Room room : map.getAllRooms()) {
      if (room.equals(exit_room) || room.equals(exterior_room)) {
        continue;
      }
      placeRobotsInRoom(room);
    }
  }

  public static void placeEntrance(DescentMap map) {
    Room entrance_room = map.getEntranceRoom();
    RoomSide entrance_side = map.getEntranceSide();
    Point nw_corner = entrance_room.getNWCorner();
    Point se_corner = entrance_room.getSECorner();
    double x_loc;
    double y_loc;
    switch (entrance_side) {
      case NORTH:
        x_loc = (int) (Math.random() * (se_corner.x - nw_corner.x)) + nw_corner.x + 0.5;
        y_loc = nw_corner.y + 0.5;
        break;
      case SOUTH:
        x_loc = (int) (Math.random() * (se_corner.x - nw_corner.x)) + nw_corner.x + 0.5;
        y_loc = se_corner.y - 0.5;
        break;
      case WEST:
        x_loc = nw_corner.x + 0.5;
        y_loc = (int) (Math.random() * (se_corner.y - nw_corner.y)) + nw_corner.y + 0.5;
        break;
      case EAST:
        x_loc = se_corner.x - 0.5;
        y_loc = (int) (Math.random() * (se_corner.y - nw_corner.y)) + nw_corner.y + 0.5;
        break;
      default:
        throw new DescentMapException("Unexpected RoomSide: " + entrance_side);
    }
    Entrance entrance = new Entrance(entrance_room, x_loc, y_loc, RoomSide.opposite(entrance_side));
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

  public static void placeEnergyCenters(DescentMap map, HashSet<Room> restricted_rooms) {
    ArrayList<Room> possible_rooms = new ArrayList<Room>();
    for (Room room : map.getAllRooms()) {
      if (restricted_rooms.contains(room)) {
        continue;
      }
      int height = room.getHeight();
      int width = room.getWidth();
      if (height == 1 || width == 1 || height * width <= MAX_ROOM_AREA_FOR_ENERGY_CENTER) {
        possible_rooms.add(room);
      }
    }
    if (possible_rooms.isEmpty()) {
      return;
    }
    Room room = possible_rooms.get((int) (Math.random() * possible_rooms.size()));
    Point nw_corner = room.getNWCorner();
    Point se_corner = room.getSECorner();
    double center_x = (int) (Math.random() * (se_corner.x - nw_corner.x)) + nw_corner.x + 0.5;
    double center_y = (int) (Math.random() * (se_corner.y - nw_corner.y)) + nw_corner.y + 0.5;
    for (int dx = -ENERGY_CENTER_PLACEMENT_RADIUS; dx <= ENERGY_CENTER_PLACEMENT_RADIUS; ++dx) {
      for (int dy = -ENERGY_CENTER_PLACEMENT_RADIUS; dy <= ENERGY_CENTER_PLACEMENT_RADIUS; ++dy) {
        double x_loc = center_x + dx;
        double y_loc = center_y + dy;
        if (nw_corner.x < x_loc && x_loc < se_corner.x && nw_corner.y < y_loc && y_loc < se_corner.y) {
          room.addChild(new EnergyCenter(room, x_loc, y_loc));
        }
      }
    }
  }

  public static void placeRobotGenerators(DescentMap map, HashSet<Room> restricted_rooms) {
    ArrayList<Room> rooms = map.getAllRooms();
    Room room;
    do {
      room = rooms.get((int) (Math.random() * rooms.size()));
      if (!room.getSceneries().isEmpty() || room.getHeight() * room.getWidth() < 2 ||
              restricted_rooms.contains(room)) {
        room = null;
      }
    } while (room == null);
    Point nw_corner = room.getNWCorner();
    Point se_corner = room.getSECorner();
    room.addChild(new RobotGenerator(room, (int) (Math.random() * (se_corner.x - nw_corner.x)) + nw_corner.x +
            0.5, (int) (Math.random() * (se_corner.y - nw_corner.y)) + nw_corner.y + 0.5,
            ROBOT_GENERATOR_TYPES[(int) (Math.random() * ROBOT_GENERATOR_TYPES.length)],
            RoomSide.values()[RoomSide.values().length - 1]));
  }

  public static void placeRobotsInRoom(Room room) {
    int room_area = room.getHeight() * room.getWidth();
    for (ObjectType type : ROBOTS_TO_POPULATE) {
      if (Math.random() < ROBOT_PLACEMENT_PROB &&
              (Unit.getStartingShields(type) <= room_area || room_area >= ALL_ROBOTS_MIN_ROOM_AREA)) {
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
