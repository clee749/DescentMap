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
  public static final int NUM_ROOMS_PER_ENERGY_CENTER = 20;
  public static final int MAX_ROOM_AREA_FOR_ENERGY_CENTER = 10;
  public static final int ENERGY_CENTER_PLACEMENT_RADIUS = 2;
  public static final int NUM_ROOMS_PER_ROBOT_GENERATOR = 10;
  public static final int MIN_ROOM_DIMENSION_FOR_ROBOT_GENERATOR = 3;
  public static final ObjectType[] ROBOT_GENERATOR_TYPES = {ObjectType.AdvancedLifter, ObjectType.Bomber,
          ObjectType.Class2Drone, ObjectType.DefenseRobot, ObjectType.LightHulk, ObjectType.MediumLifter,
          ObjectType.PlatformLaser, ObjectType.SecondaryLifter};

  // robots
  public static final int ALL_ROBOTS_MIN_ROOM_AREA = 50;
  public static final double ROBOT_PLACEMENT_PROB = 0.5;
  public static final double MIN_ROBOT_DISTANCE_FROM_ENTRANCE = 2.0;
  public static final ObjectType[] ROBOTS_TO_POPULATE = {ObjectType.AdvancedLifter, ObjectType.Bomber,
          ObjectType.Class1Drone, ObjectType.Class2Drone, ObjectType.DefenseRobot, ObjectType.HeavyDriller,
          ObjectType.HeavyHulk, ObjectType.LightHulk, ObjectType.MediumHulk, ObjectType.MediumHulkCloaked,
          ObjectType.MediumLifter, ObjectType.PlatformLaser, ObjectType.PlatformMissile,
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
    Entrance entrance = placeEntrance(map);
    placeExit(map);
    placeEnergyCenters(map, special_rooms);
    placeRobotGenerators(map, special_rooms);
    for (Room room : map.getAllRooms()) {
      if (room.equals(exit_room) || room.equals(exterior_room)) {
        continue;
      }
      placeRobotsInRoom(room, (room.equals(entrance_room) ? entrance : null));
    }
  }

  public static Entrance placeEntrance(DescentMap map) {
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
    return entrance;
  }

  public static Exit placeExit(DescentMap map) {
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
    Exit exit = new Exit(exit_room, x_loc, y_loc);
    exit_room.addChild(exit);
    return exit;
  }

  public static void placeEnergyCenters(DescentMap map, HashSet<Room> restricted_rooms) {
    ArrayList<Room> all_rooms = map.getAllRooms();
    int num_energy_centers = Math.max(all_rooms.size() / NUM_ROOMS_PER_ENERGY_CENTER, 1);
    ArrayList<Room> possible_rooms = new ArrayList<Room>();
    for (Room room : all_rooms) {
      if (restricted_rooms.contains(room)) {
        continue;
      }
      int height = room.getHeight();
      int width = room.getWidth();
      if (height == 1 || width == 1 || height * width <= MAX_ROOM_AREA_FOR_ENERGY_CENTER) {
        possible_rooms.add(room);
      }
    }
    for (int count = 0; count < num_energy_centers && !possible_rooms.isEmpty(); ++count) {
      Room room = possible_rooms.remove((int) (Math.random() * possible_rooms.size()));
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
  }

  public static void placeRobotGenerators(DescentMap map, HashSet<Room> restricted_rooms) {
    ArrayList<Room> all_rooms = map.getAllRooms();
    int num_robot_generators = Math.max(all_rooms.size() / NUM_ROOMS_PER_ROBOT_GENERATOR, 1);
    ArrayList<Room> possible_rooms = new ArrayList<Room>();
    for (Room room : all_rooms) {
      if (restricted_rooms.contains(room)) {
        continue;
      }
      if (room.getSceneries().isEmpty() && room.getHeight() >= MIN_ROOM_DIMENSION_FOR_ROBOT_GENERATOR &&
              room.getWidth() >= MIN_ROOM_DIMENSION_FOR_ROBOT_GENERATOR && !restricted_rooms.contains(room)) {
        possible_rooms.add(room);
      }
    }
    for (int count = 0; count < num_robot_generators && !possible_rooms.isEmpty(); ++count) {
      Room room = possible_rooms.remove((int) (Math.random() * possible_rooms.size()));
      Point nw_corner = room.getNWCorner();
      Point se_corner = room.getSECorner();
      double x_loc = (int) (Math.random() * (se_corner.x - nw_corner.x - 2)) + nw_corner.x + 1.5;
      double y_loc = (int) (Math.random() * (se_corner.y - nw_corner.y - 2)) + nw_corner.y + 1.5;
      room.addChild(new RobotGenerator(room, x_loc, y_loc,
              ROBOT_GENERATOR_TYPES[(int) (Math.random() * ROBOT_GENERATOR_TYPES.length)],
              RoomSide.values()[(int) (Math.random() * RoomSide.values().length)]));
    }
  }

  public static void placeRobotsInRoom(Room room, Entrance entrance) {
    int room_area = room.getHeight() * room.getWidth();
    for (ObjectType type : ROBOTS_TO_POPULATE) {
      if (Math.random() < ROBOT_PLACEMENT_PROB &&
              (Unit.getStartingShields(type) <= room_area || room_area >= ALL_ROBOTS_MIN_ROOM_AREA)) {
        placeRobotInRoom(room, type, entrance);
      }
    }
  }

  public static void placeRobotInRoom(Room room, ObjectType type, Entrance entrance) {
    Point2D.Double location =
            MapUtils.randomInternalPoint(room.getNWCorner(), room.getSECorner(), Unit.getRadius(type));
    if (entrance == null || Math.abs(location.x - entrance.getX()) > MIN_ROBOT_DISTANCE_FROM_ENTRANCE ||
            Math.abs(location.y - entrance.getY()) > MIN_ROBOT_DISTANCE_FROM_ENTRANCE) {
      room.addChild(RobotFactory.newRobot(type, room, location.x, location.y, Math.random() * MapUtils.TWO_PI));
    }
  }
}
