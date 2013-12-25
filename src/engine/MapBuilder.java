package engine;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

import mapstructure.Room;
import mapstructure.RoomConnection;

import common.RoomSide;

public class MapBuilder {
  private final int max_room_size;
  private final ArrayList<Room> all_rooms;
  private final ArrayList<Room> available_rooms;
  private int min_x;
  private int max_x;
  private int min_y;
  private int max_y;
  
  public MapBuilder(int max_room_size) {
    this.max_room_size = max_room_size;
    all_rooms = new ArrayList<Room>();
    available_rooms = new ArrayList<Room>();
  }
  
  public ArrayList<Room> getRooms() {
    return all_rooms;
  }
  
  public void addFirstRoom() {
    int width = (int) (Math.random() * max_room_size) + 1;
    int height = (int) (Math.random() * max_room_size) + 1;
    Room room = new Room(new Point(0, 0), new Point(width, height));
    all_rooms.add(room);
    available_rooms.add(room);
    min_x = 0;
    max_x = width;
    min_y = 0;
    max_y = height;
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
            new Dimension((int) (Math.random() * max_room_size) + 1, (int) (Math.random() * max_room_size) + 1);
    ArrayList<RoomSide> possible_directions = base_room.findUnconnectedSides();
    int direction_index = (int) (Math.random() * possible_directions.size());
    RoomConnection connection = null;
    RoomSide direction = null;
    boolean new_room_found = false;
    for (int num_tries = 0; num_tries < possible_directions.size(); ++num_tries) {
      direction = possible_directions.get((direction_index + num_tries) % possible_directions.size());
      connection = positionRoom(base_room, new_room_dims, direction);
      if (roomFits(connection.getNeighbor())) {
        new_room_found = true;
        break;
      }
    }
    if (!new_room_found) {
      // TODO: retry with another random room
      available_rooms.remove(room_index);
      return;
    }
    Room new_room = connection.getNeighbor();
    base_room.addNeighbor(direction, connection);
    all_rooms.add(new_room);
    available_rooms.add(new_room);
    RoomSide opposite_direction = RoomSide.opposite(direction);
    new_room.addNeighbor(opposite_direction, new RoomConnection(new_room, base_room, opposite_direction));
    updateMapRanges(new_room);
  }
  
  public RoomConnection positionRoom(Room base_room, Dimension new_room_dims, RoomSide direction) {
    Point base_corner = base_room.getNWCorner();
    if (direction.equals(RoomSide.East) || direction.equals(RoomSide.West)) {
      int base_dim = base_room.getHeight();
      int new_dim = new_room_dims.height;
      int new_corner_range = base_dim + new_dim - 1;
      int new_corner_offset = (int) (Math.random() * new_corner_range) - base_dim + 1;
      Point new_nw_corner;
      if (direction.equals(RoomSide.East)) {
        new_nw_corner = new Point(base_corner.x + base_room.getWidth(), base_corner.y - new_corner_offset);
      }
      else {
        new_nw_corner = new Point(base_corner.x - new_room_dims.width, base_corner.y - new_corner_offset);
      }
      Point new_se_corner = new Point(new_nw_corner.x + new_room_dims.width, new_nw_corner.y + new_room_dims.height);
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
      if (direction.equals(RoomSide.North)) {
        new_nw_corner = new Point(base_corner.x - new_corner_offset, base_corner.y - new_room_dims.height);
      }
      else {
        new_nw_corner = new Point(base_corner.x - new_corner_offset, base_corner.y + base_room.getHeight());
      }
      Point new_se_corner = new Point(new_nw_corner.x + new_room_dims.width, new_nw_corner.y + new_room_dims.height);
      Room new_room = new Room(new_nw_corner, new_se_corner);
      return new RoomConnection(Math.max(new_nw_corner.x, base_corner.x), Math.min(new_se_corner.x,
              base_room.getSECorner().x), new_room);
    }
  }
  
  public boolean roomFits(Room room) {
    Point new_nw_corner = room.getNWCorner();
    Point new_se_corner = room.getSECorner();
    for (Room old_room : all_rooms) {
      Point old_nw_corner = old_room.getNWCorner();
      Point old_se_corner = old_room.getSECorner();
      if (old_se_corner.x > new_nw_corner.x && old_nw_corner.x < new_se_corner.x && old_se_corner.y > new_nw_corner.y
              && old_nw_corner.y < new_se_corner.y) {
        return false;
      }
    }
    return true;
  }
  
  public void updateMapRanges(Room new_room) {
    Point new_nw_corner = new_room.getNWCorner();
    Point new_se_corner = new_room.getSECorner();
    if (new_nw_corner.x < min_x) {
      min_x = new_nw_corner.x;
    }
    if (new_se_corner.x > max_x) {
      max_x = new_se_corner.x;
    }
    if (new_nw_corner.y < min_y) {
      min_y = new_nw_corner.y;
    }
    if (new_se_corner.y > max_y) {
      max_y = new_se_corner.y;
    }
  }
}
