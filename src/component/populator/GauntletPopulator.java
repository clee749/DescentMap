package component.populator;

import java.awt.Point;
import java.util.ArrayList;

import mapobject.scenery.Entrance;
import mapobject.scenery.Exit;
import mapobject.scenery.RobotGenerator;
import structure.DescentMap;
import structure.Room;
import util.PowerupFactory;

import common.ObjectType;
import common.RoomSide;

public class GauntletPopulator extends MapPopulator {
  private static final ObjectType[] GENERATOR_ROOM_POWERUPS =
          {ObjectType.MegaMissilePowerup, ObjectType.SmartMissilePowerup};

  public GauntletPopulator(DescentMap map) {
    super(map);
  }

  @Override
  public void populateMap() {
    Entrance entrance = placeEntrance();
    placeExit();
    placeEntrancePowerups(entrance);
    populateGeneratorRooms();
  }

  public Entrance placeEntrance() {
    Room entrance_room = map.getEntranceRoom();
    Point nw_corner = entrance_room.getNWCorner();
    Entrance entrance =
            new Entrance(entrance_room, nw_corner.x + 0.5, nw_corner.y + entrance_room.getHeight() / 2.0,
                    RoomSide.EAST);
    entrance_room.addChild(entrance);
    map.setEntrance(entrance);
    map.setCenterObject(entrance);
    return entrance;
  }

  public void placeExit() {
    Room exit_room = map.getExitRoom();
    Point nw_corner = exit_room.getNWCorner();
    exit_room.addChild(new Exit(exit_room, nw_corner.x + 0.5, nw_corner.y + 0.5));
  }

  public void placeEntrancePowerups(Entrance entrance) {
    ArrayList<Room> all_rooms = map.getAllRooms();
    Room room = all_rooms.get(0);
    Point nw_corner = room.getNWCorner();
    int entrance_y_count = (int) entrance.getY() - nw_corner.x;
    for (int x_count = 0; x_count < room.getWidth(); ++x_count) {
      for (int y_count = 0; y_count < room.getHeight(); ++y_count) {
        if (x_count == 0 && y_count == entrance_y_count) {
          continue;
        }
        room.addChild(PowerupFactory.newStationaryPowerup(ObjectType.SpreadfireCannonPowerup, room,
                nw_corner.x + 0.5 + x_count, nw_corner.y + 0.5 + y_count));
      }
    }
  }

  public void populateGeneratorRooms() {
    ArrayList<Room> all_rooms = map.getAllRooms();
    Room room = all_rooms.get(1);
    int num_generators_per_side = room.getWidth() / 3;
    double south_row_dy = room.getHeight() - 1.5;
    for (int room_index = 1; room_index < all_rooms.size() - 2; ++room_index) {
      room = all_rooms.get(room_index);
      Point nw_corner = room.getNWCorner();
      for (int count = 0; count < num_generators_per_side; ++count) {
        populateGeneratorRoomLocation(room, nw_corner.x + 1.5 + count * 3, nw_corner.y + 1.5, RoomSide.SOUTH);
        populateGeneratorRoomLocation(room, nw_corner.x + 1.5 + count * 3, nw_corner.y + south_row_dy,
                RoomSide.NORTH);
      }
    }
  }

  public void populateGeneratorRoomLocation(Room room, double x_loc, double y_loc,
          RoomSide generator_direction) {
    room.addChild(new RobotGenerator(room, x_loc, y_loc,
            ObjectType.STANDARD_ROBOTS[(int) (Math.random() * ObjectType.STANDARD_ROBOTS.length)],
            generator_direction));
    room.addChild(PowerupFactory.newStationaryPowerup(
            GENERATOR_ROOM_POWERUPS[(int) (Math.random() * GENERATOR_ROOM_POWERUPS.length)], room, x_loc,
            y_loc));
  }
}
