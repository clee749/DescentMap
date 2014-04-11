package component.builder;

import java.awt.Point;

import structure.MineExteriorRoom;
import structure.Room;
import structure.RoomConnection;

import common.RoomSide;

public class GauntletBuilder extends MapBuilder {
  public static final int ENTRANCE_ROOM_WIDTH = 2;
  public static final int ENTRANCE_ROOM_HEIGHT = 3;
  public static final int GENERATOR_ROOM_WIDTH = 5;

  private final int min_num_generator_rooms;
  private final int generator_room_length;

  public GauntletBuilder(int max_room_size, int min_num_generator_rooms) {
    super(max_room_size);
    this.min_num_generator_rooms = min_num_generator_rooms;
    generator_room_length = max_room_size / 3 * 3;
  }

  @Override
  public boolean isReadyToFinish() {
    return all_rooms.size() - 1 >= min_num_generator_rooms;
  }

  @Override
  public boolean addRoom() {
    if (all_rooms.isEmpty()) {
      addEntranceRoom();
      return true;
    }
    if (all_rooms.size() < 2) {
      addFirstGeneratorRoom();
      return true;
    }
    addGeneralGeneratorRoom();
    return true;
  }

  public void addEntranceRoom() {
    addFirstRoom(ENTRANCE_ROOM_WIDTH, ENTRANCE_ROOM_HEIGHT);
  }

  public void addFirstGeneratorRoom() {
    Room entrance_room = all_rooms.get(0);
    Point entrance_ne_corner = entrance_room.getNECorner();
    Room new_room =
            new Room(new Point(entrance_ne_corner.x, entrance_ne_corner.y -
                    (GENERATOR_ROOM_WIDTH - ENTRANCE_ROOM_HEIGHT) / 2), generator_room_length,
                    GENERATOR_ROOM_WIDTH);
    RoomConnection connection = new RoomConnection(entrance_room, new_room, RoomSide.EAST);
    finalizeRoom(entrance_room, RoomSide.EAST, connection);
  }

  public void addGeneralGeneratorRoom() {
    Room base_room = all_rooms.get(all_rooms.size() - 1);
    Room new_room = new Room(new Point(base_room.getNECorner()), generator_room_length, GENERATOR_ROOM_WIDTH);
    RoomConnection connection = new RoomConnection(base_room, new_room, RoomSide.EAST);
    finalizeRoom(base_room, RoomSide.EAST, connection);
  }

  @Override
  public void placeEntranceAndExitRooms() {
    entrance_edge_room = new EdgeRoom(RoomSide.WEST, westmost_room);

    Point pre_exit_ne_corner = eastmost_room.getNECorner();
    Room exit_room =
            new Room(new Point(pre_exit_ne_corner.x, pre_exit_ne_corner.y + GENERATOR_ROOM_WIDTH / 2),
                    DEFAULT_EXIT_ROOM_LENGTH, 1);
    finalizeRoom(eastmost_room, RoomSide.EAST, new RoomConnection(eastmost_room, exit_room, RoomSide.EAST));
    exit_edge_room = new EdgeRoom(RoomSide.EAST, exit_room);

    exterior_room = new MineExteriorRoom(new Point(exit_room.getNECorner()));
    finalizeRoom(exit_room, RoomSide.EAST, new RoomConnection(exit_room, exterior_room, RoomSide.EAST));
  }
}
