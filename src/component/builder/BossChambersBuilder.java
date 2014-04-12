package component.builder;

import java.awt.Point;

import structure.MineExteriorRoom;
import structure.Room;
import structure.RoomConnection;

import common.RoomSide;

public class BossChambersBuilder extends MapBuilder {
  public static final int CONNECTOR_ROOM_LENGTH = 3;

  private final int min_width;
  private final int min_height;
  private final int boss_chamber_size;
  private final int connector_room_offset;
  private boolean is_ready_to_finish;
  private Room current_boss_chamber;
  private int current_x;
  private int current_y;
  private boolean is_new_row_started;

  public BossChambersBuilder(int max_room_size, int min_width, int min_height) {
    super(max_room_size);
    this.min_width = min_width;
    this.min_height = min_height;
    boss_chamber_size = (max_room_size % 2 == 0 ? max_room_size - 1 : max_room_size);
    connector_room_offset = this.boss_chamber_size / 2;
  }

  @Override
  public boolean isReadyToFinish() {
    return is_ready_to_finish;
  }

  @Override
  public boolean addRoom() {
    if (all_rooms.isEmpty()) {
      addEntranceRoom();
      return true;
    }
    // if (all_rooms.size() < 2) {
    // addFirstBossChamber();
    // return true;
    // }
    addGeneralRoom();
    return true;
  }

  public void addEntranceRoom() {
    current_boss_chamber = addFirstRoom(boss_chamber_size, boss_chamber_size);
    current_x = 1;
    current_y = 1;
  }

  public void addFirstBossChamber() {
    Room entrance_room = all_rooms.get(0);
    Point entrance_ne_corner = entrance_room.getNECorner();
    current_boss_chamber =
            new Room(new Point(entrance_ne_corner.x, entrance_ne_corner.y - connector_room_offset),
                    boss_chamber_size, boss_chamber_size);
    RoomConnection connection = new RoomConnection(entrance_room, current_boss_chamber, RoomSide.EAST);
    finalizeRoom(entrance_room, RoomSide.EAST, connection);
    current_x = 1;
    current_y = 1;
  }

  public void addGeneralRoom() {
    if (current_x == min_width &&
            (current_y < 2 || current_boss_chamber.getConnectionInDirection(RoomSide.NORTH) != null)) {
      if (!is_new_row_started) {
        startNewRow();
      }
      else {
        addFirstBossChamberInRow();
      }
    }
    else {
      if (current_y > 1 && current_boss_chamber.getConnectionInDirection(RoomSide.NORTH) == null) {
        addVerticalConnectorRoom();
        if (current_x == min_width && current_y == min_height) {
          is_ready_to_finish = true;
        }
      }
      else if (current_boss_chamber.getConnectionInDirection(RoomSide.EAST) == null) {
        addHorizontalConnectorRoom();
      }
      else {
        addNextBossChamberInRow();
      }
    }
  }

  public void startNewRow() {
    int base_room_index = all_rooms.size() - min_width - (min_width - 1);
    if (current_y > 1) {
      base_room_index -= min_width - 1;
    }
    Room base_room = all_rooms.get(base_room_index);
    Point base_sw_corner = base_room.getSWCorner();
    Room new_room =
            new Room(new Point(base_sw_corner.x + connector_room_offset, base_sw_corner.y), 1,
                    CONNECTOR_ROOM_LENGTH);
    RoomConnection connection = new RoomConnection(base_room, new_room, RoomSide.SOUTH);
    finalizeRoom(base_room, RoomSide.SOUTH, connection);
    is_new_row_started = true;
  }

  public void addFirstBossChamberInRow() {
    Room base_room = all_rooms.get(all_rooms.size() - 1);
    Point base_sw_corner = base_room.getSWCorner();
    current_boss_chamber =
            new Room(new Point(base_sw_corner.x - connector_room_offset, base_sw_corner.y),
                    boss_chamber_size, boss_chamber_size);
    RoomConnection connection = new RoomConnection(base_room, current_boss_chamber, RoomSide.SOUTH);
    finalizeRoom(base_room, RoomSide.SOUTH, connection);
    current_x = 1;
    ++current_y;
    is_new_row_started = false;
  }

  public void addVerticalConnectorRoom() {
    Point base_nw_corner = current_boss_chamber.getNWCorner();
    Room new_room =
            new Room(new Point(base_nw_corner.x + connector_room_offset, base_nw_corner.y -
                    CONNECTOR_ROOM_LENGTH), 1, CONNECTOR_ROOM_LENGTH);
    RoomConnection connection = new RoomConnection(current_boss_chamber, new_room, RoomSide.NORTH);
    finalizeRoom(current_boss_chamber, RoomSide.NORTH, connection);
    int other_boss_chamber_index =
            all_rooms.size() - min_width - min_width - (current_y < 3 ? current_x : min_width + 1);
    Room other_boss_chamber = all_rooms.get(other_boss_chamber_index);
    new_room.addNeighbor(RoomSide.NORTH, new RoomConnection(new_room, other_boss_chamber, RoomSide.NORTH));
    other_boss_chamber.addNeighbor(RoomSide.SOUTH, new RoomConnection(other_boss_chamber, new_room,
            RoomSide.SOUTH));
  }

  public void addHorizontalConnectorRoom() {
    Point base_ne_corner = current_boss_chamber.getNECorner();
    Room new_room =
            new Room(new Point(base_ne_corner.x, base_ne_corner.y + connector_room_offset),
                    CONNECTOR_ROOM_LENGTH, 1);
    RoomConnection connection = new RoomConnection(current_boss_chamber, new_room, RoomSide.EAST);
    finalizeRoom(current_boss_chamber, RoomSide.EAST, connection);
  }

  public void addNextBossChamberInRow() {
    Room base_room = all_rooms.get(all_rooms.size() - 1);
    Point base_ne_corner = base_room.getNECorner();
    current_boss_chamber =
            new Room(new Point(base_ne_corner.x, base_ne_corner.y - connector_room_offset),
                    boss_chamber_size, boss_chamber_size);
    RoomConnection connection = new RoomConnection(base_room, current_boss_chamber, RoomSide.EAST);
    finalizeRoom(base_room, RoomSide.EAST, connection);
    ++current_x;
  }

  @Override
  public void finish() {
    Room entrance_base_room = all_rooms.get(min_width + (min_width - 1));
    Point entrance_base_ne_corner = entrance_base_room.getNECorner();
    int connector_middle_offset = entrance_base_room.getHeight() / 2;
    Room entrance_room =
            new Room(
                    new Point(entrance_base_ne_corner.x, entrance_base_ne_corner.y + connector_middle_offset),
                    Math.min(connector_room_offset * 2 - 1 + CONNECTOR_ROOM_LENGTH, max_room_size), 1);
    RoomConnection connection = new RoomConnection(entrance_base_room, entrance_room, RoomSide.EAST);
    finalizeRoom(entrance_base_room, RoomSide.EAST, connection);
    entrance_edge_room = new EdgeRoom(RoomSide.WEST, entrance_room);

    Room pre_exit_room = all_rooms.get(all_rooms.size() - 2);
    Point pre_exit_ne_corner = pre_exit_room.getNECorner();
    Room exit_room =
            new Room(new Point(pre_exit_ne_corner.x, pre_exit_ne_corner.y + connector_middle_offset),
                    DEFAULT_EXIT_ROOM_LENGTH, 1);
    finalizeRoom(pre_exit_room, RoomSide.EAST, new RoomConnection(pre_exit_room, exit_room, RoomSide.EAST));
    exit_edge_room = new EdgeRoom(RoomSide.EAST, exit_room);

    exterior_room = new MineExteriorRoom(new Point(exit_room.getNECorner()));
    finalizeRoom(exit_room, RoomSide.EAST, new RoomConnection(exit_room, exterior_room, RoomSide.EAST));
  }
}
