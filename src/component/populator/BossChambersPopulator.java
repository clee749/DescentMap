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

public class BossChambersPopulator extends MapPopulator {
  protected static final ObjectType[] GENERATOR_POWERUPS =
          {ObjectType.MegaMissilePowerup, ObjectType.SmartMissilePowerup};

  public BossChambersPopulator(DescentMap map) {
    super(map);
  }

  @Override
  public void populateMap() {
    placeEntrance();
    placeExit();
    populateRooms();
  }

  public void placeEntrance() {
    Room entrance_room = map.getEntranceRoom();
    Point ne_corner = entrance_room.getNECorner();
    Entrance entrance = new Entrance(entrance_room, ne_corner.x - 0.5, ne_corner.y + 0.5, RoomSide.WEST);
    entrance_room.addChild(entrance);
    map.setEntrance(entrance);
    map.setCenterObject(entrance);
  }

  public void placeExit() {
    Room exit_room = map.getExitRoom();
    Point nw_corner = exit_room.getNWCorner();
    exit_room.addChild(new Exit(exit_room, nw_corner.x + 0.5, nw_corner.y + 0.5));
  }

  public void populateRooms() {
    ArrayList<Room> all_rooms = map.getAllRooms();
    Room room = all_rooms.get(0);
    int boss_chamber_size = room.getHeight();
    for (int room_index = 0; room_index < all_rooms.size() - 3; ++room_index) {
      room = all_rooms.get(room_index);
      if (room.getHeight() < boss_chamber_size || room.getWidth() < boss_chamber_size) {
        populateConnectorRoom(room);
      }
      else {
        populateBossChamber(room);
      }
    }
  }

  public void populateConnectorRoom(Room room) {
    Point nw_corner = room.getNWCorner();
    Point se_corner = room.getSECorner();
    room.addChild(PowerupFactory.newStationaryPowerup(ObjectType.SpreadfireCannonPowerup, room,
            nw_corner.x + 0.5, nw_corner.y + 0.5));
    room.addChild(PowerupFactory.newStationaryPowerup(ObjectType.SpreadfireCannonPowerup, room,
            se_corner.x - 0.5, se_corner.y - 0.5));
  }

  public void populateBossChamber(Room room) {
    Point nw_corner = room.getNWCorner();
    double far_side_offset = room.getHeight() - 1.5;
    populateBossChamberRoomLocation(room, nw_corner.x + 1.5, nw_corner.y + 1.5, RoomSide.EAST, RoomSide.SOUTH);
    populateBossChamberRoomLocation(room, nw_corner.x + far_side_offset, nw_corner.y + 1.5, RoomSide.WEST,
            RoomSide.SOUTH);
    populateBossChamberRoomLocation(room, nw_corner.x + 1.5, nw_corner.y + far_side_offset, RoomSide.EAST,
            RoomSide.NORTH);
    populateBossChamberRoomLocation(room, nw_corner.x + far_side_offset, nw_corner.y + far_side_offset,
            RoomSide.NORTH, RoomSide.WEST);
  }

  public void populateBossChamberRoomLocation(Room room, double x_loc, double y_loc,
          RoomSide generator_direction1, RoomSide generator_direction2) {
    room.addChild(new RobotGenerator(room, x_loc, y_loc,
            ObjectType.STANDARD_ROBOTS[(int) (Math.random() * ObjectType.STANDARD_ROBOTS.length)],
            (Math.random() < 0.5 ? generator_direction1 : generator_direction2)));
    room.addChild(PowerupFactory.newStationaryPowerup(
            GENERATOR_POWERUPS[(int) (Math.random() * GENERATOR_POWERUPS.length)], room, x_loc, y_loc));
  }
}
