package component;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

import structure.MineExteriorRoom;
import structure.Room;
import structure.RoomConnection;

import common.Constants;
import common.MapUtils;
import common.RoomSide;

class EdgeRoom {
  public final RoomSide direction;
  public final Room room;

  public EdgeRoom(RoomSide direction, Room room) {
    this.direction = direction;
    this.room = room;
  }
}


public class MapBuilder {
  private final int max_room_size;
  private final ArrayList<Room> all_rooms;
  private final ArrayList<Room> available_rooms;
  private Room westmost_room;
  private Room eastmost_room;
  private Room northmost_room;
  private Room southmost_room;
  private Room entrance_room;
  private Room exit_room;

  public MapBuilder(int max_room_size) {
    this.max_room_size = max_room_size;
    all_rooms = new ArrayList<Room>();
    available_rooms = new ArrayList<Room>();
  }

  public ArrayList<Room> getRooms() {
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
    return entrance_room;
  }

  public Room getExitRoom() {
    return exit_room;
  }

  public void addFirstRoom() {
    int width = (int) (Math.random() * max_room_size) + 1;
    int height = (int) (Math.random() * max_room_size) + 1;
    Room room = new Room(new Point(0, 0), new Point(width, height));
    all_rooms.add(room);
    available_rooms.add(room);
    westmost_room = room;
    eastmost_room = room;
    northmost_room = room;
    southmost_room = room;
    entrance_room = room;
  }

  /**
   * Choose a random room to build off from the list of available rooms. Choose a random direction,
   * size, and connection location for the new room. If the room fits, use it. TODO: allow the new
   * room to connect to multiple existing rooms for cycles Else, increment the direction enum,
   * select a new random connection location, and try again with the same room. If all available
   * directions fail, then take the room off the list of available rooms.
   */
  public void addRoom() {
    if (all_rooms.isEmpty()) {
      addFirstRoom();
      return;
    }
    if (available_rooms.isEmpty()) {
      return;
    }
    int room_index = (int) (Math.random() * available_rooms.size());
    Room base_room = available_rooms.get(room_index);
    Dimension new_room_dims =
            new Dimension((int) (Math.random() * max_room_size) + 1,
                    (int) (Math.random() * max_room_size) + 1);
    ArrayList<RoomSide> possible_directions = base_room.findUnconnectedSides();
    int direction_index = (int) (Math.random() * possible_directions.size());
    RoomConnection connection = null;
    RoomSide direction = null;
    boolean new_room_found = false;
    for (int num_tries = 0; num_tries < possible_directions.size(); ++num_tries) {
      direction = possible_directions.get((direction_index + num_tries) % possible_directions.size());
      connection = positionRoom(base_room, new_room_dims, direction);
      if (roomFits(connection.neighbor, base_room)) {
        new_room_found = true;
        break;
      }
    }
    if (!new_room_found) {
      // TODO: retry with another random room
      available_rooms.remove(room_index);
      return;
    }
    Room new_room = connection.neighbor;
    base_room.addNeighbor(direction, connection);
    all_rooms.add(new_room);
    available_rooms.add(new_room);
    RoomSide opposite_direction = RoomSide.opposite(direction);
    new_room.addNeighbor(opposite_direction, new RoomConnection(new_room, base_room, opposite_direction));
    updateMapRanges(new_room);
  }

  public void placeEntranceAndExitRooms() {
    ArrayList<EdgeRoom> edge_rooms = new ArrayList<EdgeRoom>();
    edge_rooms.add(new EdgeRoom(RoomSide.EAST, eastmost_room));
    edge_rooms.add(new EdgeRoom(RoomSide.NORTH, northmost_room));
    edge_rooms.add(new EdgeRoom(RoomSide.WEST, westmost_room));
    edge_rooms.add(new EdgeRoom(RoomSide.SOUTH, southmost_room));

    EdgeRoom entrance_edge_room = edge_rooms.remove((int) (Math.random() * edge_rooms.size()));
    entrance_room = entrance_edge_room.room;
    MineExteriorRoom entrance_exterior_room =
            new MineExteriorRoom(entrance_room.getNWCorner(), entrance_room.getSECorner());
    all_rooms.add(entrance_exterior_room);

    EdgeRoom exit_edge_room = edge_rooms.remove((int) (Math.random() * edge_rooms.size()));
    RoomSide direction = exit_edge_room.direction;
    Dimension exit_room_dims;
    if (direction.equals(RoomSide.EAST) || direction.equals(RoomSide.WEST)) {
      exit_room_dims = new Dimension(Constants.BUILDER_EXIT_ROOM_LENGTH, 1);
    }
    else {
      exit_room_dims = new Dimension(1, Constants.BUILDER_EXIT_ROOM_LENGTH);
    }
    RoomConnection connection = positionRoom(exit_edge_room.room, exit_room_dims, direction);
    exit_edge_room.room.addNeighbor(direction, connection);
    RoomSide opposite_direction = RoomSide.opposite(direction);
    exit_room = connection.neighbor;
    exit_room.addNeighbor(opposite_direction, new RoomConnection(exit_room, exit_edge_room.room,
            opposite_direction));
    all_rooms.add(exit_room);
    // no need to call updateMapRanges here because the MineExteriorRoom will be in the same
    // direction

    Point exterior_nw_corner;
    if (direction.equals(RoomSide.EAST)) {
      exterior_nw_corner = exit_room.getNECorner();
    }
    else if (direction.equals(RoomSide.NORTH)) {
      Point base_corner = exit_room.getNWCorner();
      exterior_nw_corner = new Point(base_corner.x, base_corner.y - 1);
    }
    else if (direction.equals(RoomSide.WEST)) {
      Point base_corner = exit_room.getNWCorner();
      exterior_nw_corner = new Point(base_corner.x - 1, base_corner.y);
    }
    else {
      exterior_nw_corner = exit_room.getSWCorner();
    }
    MineExteriorRoom exterior =
            new MineExteriorRoom(exterior_nw_corner, new Point(exterior_nw_corner.x + 1,
                    exterior_nw_corner.y + 1));
    exit_room.addNeighbor(direction, new RoomConnection(exit_room, exterior, direction));
    all_rooms.add(exterior);
    updateMapRanges(exterior);
  }

  public RoomConnection positionRoom(Room base_room, Dimension new_room_dims, RoomSide direction) {
    Point base_corner = base_room.getNWCorner();
    if (direction.equals(RoomSide.EAST) || direction.equals(RoomSide.WEST)) {
      int base_dim = base_room.getHeight();
      int new_dim = new_room_dims.height;
      int new_corner_range = base_dim + new_dim - 1;
      int new_corner_offset = (int) (Math.random() * new_corner_range) - base_dim + 1;
      Point new_nw_corner;
      if (direction.equals(RoomSide.EAST)) {
        new_nw_corner = new Point(base_corner.x + base_room.getWidth(), base_corner.y - new_corner_offset);
      }
      else {
        new_nw_corner = new Point(base_corner.x - new_room_dims.width, base_corner.y - new_corner_offset);
      }
      Point new_se_corner =
              new Point(new_nw_corner.x + new_room_dims.width, new_nw_corner.y + new_room_dims.height);
      Room new_room = new Room(new_nw_corner, new_se_corner);
      return new RoomConnection(Math.max(new_nw_corner.y, base_corner.y), Math.min(new_se_corner.y,
              base_room.getSECorner().y), new_room);
    }
    else {
      int base_dim = base_room.getWidth();
      int new_dim = new_room_dims.width;
      int new_corner_range = base_dim + new_dim - 1;
      int new_corner_offset = (int) (Math.random() * new_corner_range) - base_dim + 1;
      Point new_nw_corner;
      if (direction.equals(RoomSide.NORTH)) {
        new_nw_corner = new Point(base_corner.x - new_corner_offset, base_corner.y - new_room_dims.height);
      }
      else {
        new_nw_corner = new Point(base_corner.x - new_corner_offset, base_corner.y + base_room.getHeight());
      }
      Point new_se_corner =
              new Point(new_nw_corner.x + new_room_dims.width, new_nw_corner.y + new_room_dims.height);
      Room new_room = new Room(new_nw_corner, new_se_corner);
      return new RoomConnection(Math.max(new_nw_corner.x, base_corner.x), Math.min(new_se_corner.x,
              base_room.getSECorner().x), new_room);
    }
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
      if (MapUtils.rectanglesIntersect(old_nw_corner, old_se_corner, new_nw_corner, new_se_corner)) {
        return false;
      }
    }
    return true;
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
}
