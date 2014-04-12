package component.builder;

import java.awt.Point;
import java.util.ArrayList;

import structure.MineExteriorRoom;
import structure.Room;
import structure.RoomConnection;
import util.MapUtils;

import common.RoomSide;

class EdgeRoom {
  public final RoomSide direction;
  public final Room room;

  public EdgeRoom(RoomSide direction, Room room) {
    this.direction = direction;
    this.room = room;
  }
}


public abstract class MapBuilder {
  public static final int DEFAULT_EXIT_ROOM_LENGTH = 5;

  protected final int max_room_size;
  protected final ArrayList<Room> all_rooms;
  protected Room westmost_room;
  protected Room eastmost_room;
  protected Room northmost_room;
  protected Room southmost_room;
  protected EdgeRoom entrance_edge_room;
  protected EdgeRoom exit_edge_room;
  protected MineExteriorRoom exterior_room;

  public MapBuilder(int max_room_size) {
    this.max_room_size = max_room_size;
    all_rooms = new ArrayList<Room>();
  }

  public ArrayList<Room> getAllRooms() {
    return all_rooms;
  }

  public int getMinX() {
    return westmost_room.getNWCorner().x;
  }

  public int getMaxX() {
    return eastmost_room.getSECorner().x;
  }

  public int getMinY() {
    return northmost_room.getNWCorner().y;
  }

  public int getMaxY() {
    return southmost_room.getSECorner().y;
  }

  public Room getEntranceRoom() {
    return entrance_edge_room.room;
  }

  public Room getExitRoom() {
    return exit_edge_room.room;
  }

  public RoomSide getEntranceSide() {
    return entrance_edge_room.direction;
  }

  public RoomSide getExitSide() {
    return exit_edge_room.direction;
  }

  public MineExteriorRoom getExteriorRoom() {
    return exterior_room;
  }

  public Room addFirstRoom(int width, int height) {
    Room room = new Room(new Point(0, 0), new Point(width, height));
    all_rooms.add(room);
    westmost_room = room;
    eastmost_room = room;
    northmost_room = room;
    southmost_room = room;
    return room;
  }

  public boolean roomFits(Room new_room, Room base_room) {
    Point new_nw_corner = new_room.getNWCorner();
    Point new_se_corner = new_room.getSECorner();
    for (Room old_room : all_rooms) {
      if (old_room.equals(base_room)) {
        continue;
      }
      Point old_nw_corner = old_room.getNWCorner();
      Point old_se_corner = old_room.getSECorner();
      // TODO: allow the new Room to connect to multiple existing Rooms for cycles
      if (MapUtils.rectanglesIntersect(old_nw_corner, old_se_corner, new_nw_corner, new_se_corner)) {
        return false;
      }
    }
    return true;
  }

  public void finalizeRoom(Room base_room, RoomSide base_to_new_direction,
          RoomConnection base_to_new_connection) {
    Room new_room = base_to_new_connection.neighbor;
    base_room.addNeighbor(base_to_new_direction, base_to_new_connection);
    all_rooms.add(new_room);
    RoomSide opposite_direction = RoomSide.opposite(base_to_new_direction);
    new_room.addNeighbor(opposite_direction, new RoomConnection(new_room, base_room, opposite_direction));
    updateMapRanges(new_room);
  }

  public void updateMapRanges(Room new_room) {
    Point new_nw_corner = new_room.getNWCorner();
    Point new_se_corner = new_room.getSECorner();
    if (new_nw_corner.x < westmost_room.getNWCorner().x) {
      westmost_room = new_room;
    }
    if (new_se_corner.x > eastmost_room.getSECorner().x) {
      eastmost_room = new_room;
    }
    if (new_nw_corner.y < northmost_room.getNWCorner().y) {
      northmost_room = new_room;
    }
    if (new_se_corner.y > southmost_room.getSECorner().y) {
      southmost_room = new_room;
    }
  }

  public abstract boolean isReadyToFinish();

  public abstract boolean addRoom();

  public abstract void finish();
}
