package component.builder;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

import structure.MineExteriorRoom;
import structure.Room;
import structure.RoomConnection;

import common.RoomSide;

public class StandardBuilder extends MapBuilder {
  protected final int target_value;
  protected final ArrayList<Room> available_rooms;

  public StandardBuilder(int max_room_size, int target_num_rooms) {
    super(max_room_size);
    target_value = target_num_rooms;
    available_rooms = new ArrayList<Room>();
  }

  @Override
  public boolean isReadyToFinish() {
    return all_rooms.size() >= target_value;
  }

  @Override
  public boolean addRoom() {
    if (all_rooms.isEmpty()) {
      addFirstRoom();
      return true;
    }
    if (available_rooms.isEmpty()) {
      available_rooms.addAll(all_rooms);
      return false;
    }
    return addRandomRoom();
  }

  public void addFirstRoom() {
    available_rooms.add(addFirstRoom(randomRoomDim(), randomRoomDim()));
  }

  /**
   * Choose a random room to build off from the list of available rooms. Choose a random direction,
   * size, and connection location for the new room. If the room fits, use it. Else, increment the
   * direction enum, select a new random connection location, and try again with another random
   * room. If all available directions fail, then take the room off the list of available rooms.
   */
  public boolean addRandomRoom() {
    int room_index = (int) (Math.random() * available_rooms.size());
    Room base_room = available_rooms.get(room_index);
    ArrayList<RoomSide> possible_directions = base_room.findUnconnectedSides();
    int direction_index = (int) (Math.random() * possible_directions.size());
    RoomConnection connection = null;
    RoomSide direction = null;
    boolean new_room_found = false;
    for (int num_tries = 0; num_tries < possible_directions.size(); ++num_tries) {
      direction = possible_directions.get((direction_index + num_tries) % possible_directions.size());
      Dimension new_room_dims = chooseNewRoomDims(base_room, direction);
      connection = positionRoom(base_room, new_room_dims, direction);
      if (roomFits(connection.neighbor, base_room)) {
        new_room_found = true;
        break;
      }
    }
    if (!new_room_found) {
      available_rooms.remove(room_index);
      return false;
    }
    finalizeRoom(base_room, direction, connection);
    return true;
  }

  public int randomRoomDim() {
    return (int) (Math.random() * max_room_size) + 1;
  }

  public Dimension chooseNewRoomDims(Room base_room, RoomSide direction) {
    return new Dimension(randomRoomDim(), randomRoomDim());
  }

  public RoomConnection positionRoom(Room base_room, Dimension new_room_dims, RoomSide direction) {
    Point base_corner = base_room.getNWCorner();
    if (direction.equals(RoomSide.EAST) || direction.equals(RoomSide.WEST)) {
      int new_corner_offset = chooseNewCornerOffset(base_room.getHeight(), new_room_dims.height);
      Point new_nw_corner;
      if (direction.equals(RoomSide.EAST)) {
        new_nw_corner = new Point(base_corner.x + base_room.getWidth(), base_corner.y + new_corner_offset);
      }
      else {
        new_nw_corner = new Point(base_corner.x - new_room_dims.width, base_corner.y + new_corner_offset);
      }
      Point new_se_corner =
              new Point(new_nw_corner.x + new_room_dims.width, new_nw_corner.y + new_room_dims.height);
      Room new_room = new Room(new_nw_corner, new_se_corner);
      return new RoomConnection(Math.max(new_nw_corner.y, base_corner.y), Math.min(new_se_corner.y,
              base_room.getSECorner().y), new_room);
    }
    else {
      int new_corner_offset = chooseNewCornerOffset(base_room.getWidth(), new_room_dims.width);
      Point new_nw_corner;
      if (direction.equals(RoomSide.NORTH)) {
        new_nw_corner = new Point(base_corner.x + new_corner_offset, base_corner.y - new_room_dims.height);
      }
      else {
        new_nw_corner = new Point(base_corner.x + new_corner_offset, base_corner.y + base_room.getHeight());
      }
      Point new_se_corner =
              new Point(new_nw_corner.x + new_room_dims.width, new_nw_corner.y + new_room_dims.height);
      Room new_room = new Room(new_nw_corner, new_se_corner);
      return new RoomConnection(Math.max(new_nw_corner.x, base_corner.x), Math.min(new_se_corner.x,
              base_room.getSECorner().x), new_room);
    }
  }

  public int chooseNewCornerOffset(int base_dim, int new_dim) {
    return (int) (Math.random() * (base_dim + new_dim - 1)) - new_dim + 1;
  }

  @Override
  public void finalizeRoom(Room base_room, RoomSide base_to_new_direction,
          RoomConnection base_to_new_connection) {
    super.finalizeRoom(base_room, base_to_new_direction, base_to_new_connection);
    available_rooms.add(base_to_new_connection.neighbor);
  }

  @Override
  public void finish() {
    ArrayList<EdgeRoom> edge_rooms = new ArrayList<EdgeRoom>();
    edge_rooms.add(new EdgeRoom(RoomSide.EAST, eastmost_room));
    edge_rooms.add(new EdgeRoom(RoomSide.NORTH, northmost_room));
    edge_rooms.add(new EdgeRoom(RoomSide.WEST, westmost_room));
    edge_rooms.add(new EdgeRoom(RoomSide.SOUTH, southmost_room));

    entrance_edge_room = edge_rooms.remove((int) (Math.random() * edge_rooms.size()));

    EdgeRoom pre_exit_edge_room = null;
    do {
      pre_exit_edge_room = edge_rooms.remove((int) (Math.random() * edge_rooms.size()));
      if (pre_exit_edge_room.room.equals(entrance_edge_room.room)) {
        pre_exit_edge_room = null;
      }
    } while (pre_exit_edge_room == null);
    RoomSide direction = pre_exit_edge_room.direction;
    Dimension exit_room_dims;
    if (direction.equals(RoomSide.EAST) || direction.equals(RoomSide.WEST)) {
      exit_room_dims = new Dimension(DEFAULT_EXIT_ROOM_LENGTH, 1);
    }
    else {
      exit_room_dims = new Dimension(1, DEFAULT_EXIT_ROOM_LENGTH);
    }
    RoomConnection connection = positionRoom(pre_exit_edge_room.room, exit_room_dims, direction);
    pre_exit_edge_room.room.addNeighbor(direction, connection);
    RoomSide opposite_direction = RoomSide.opposite(direction);
    Room exit_room = connection.neighbor;
    exit_room.addNeighbor(opposite_direction, new RoomConnection(exit_room, pre_exit_edge_room.room,
            opposite_direction));
    exit_edge_room = new EdgeRoom(direction, exit_room);
    all_rooms.add(exit_room);
    // no need to call updateMapRanges here because MineExteriorRoom will be in same direction

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
    exterior_room = new MineExteriorRoom(exterior_nw_corner);
    exit_room.addNeighbor(direction, new RoomConnection(exit_room, exterior_room, direction));
    exterior_room.addNeighbor(opposite_direction, new RoomConnection(exterior_room, exit_room,
            opposite_direction));
    all_rooms.add(exterior_room);
    updateMapRanges(exterior_room);
  }
}
