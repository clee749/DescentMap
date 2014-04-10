package component.builder;

import java.awt.Dimension;

import structure.Room;

import common.RoomSide;

public class MazeBuilder extends StandardBuilder {
  public static final int HALLWAY_WIDTH = 2;

  private final int room_dim_range;
  private int total_room_area;

  public MazeBuilder(int max_room_size, int target_total_area) {
    super(max_room_size, target_total_area);
    room_dim_range = max_room_size - HALLWAY_WIDTH + 1;
  }

  @Override
  public boolean isReadyToFinish() {
    return total_room_area >= target_value;
  }

  @Override
  public void addFirstRoom() {
    int room_length = randomRoomDim();
    if (Math.random() < 0.5) {
      available_rooms.add(addFirstRoom(HALLWAY_WIDTH, room_length));
    }
    else {
      available_rooms.add(addFirstRoom(room_length, HALLWAY_WIDTH));
    }
    total_room_area = room_length;
  }

  @Override
  public int randomRoomDim() {
    return (int) (Math.random() * room_dim_range) + HALLWAY_WIDTH;
  }

  @Override
  public Dimension chooseNewRoomDims(Room base_room, RoomSide direction) {
    if (direction.equals(RoomSide.NORTH) || direction.equals(RoomSide.SOUTH)) {
      return new Dimension(HALLWAY_WIDTH, randomRoomDim());
    }
    else {
      return new Dimension(randomRoomDim(), HALLWAY_WIDTH);
    }
  }

  @Override
  public int chooseNewCornerOffset(int base_dim, int new_dim) {
    return (int) (Math.random() * (base_dim - HALLWAY_WIDTH + 1));
  }

  @Override
  public void updateMapRanges(Room new_room) {
    super.updateMapRanges(new_room);
    total_room_area += new_room.getHeight() * new_room.getWidth();
  }
}
