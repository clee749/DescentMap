package structure;

import common.RoomSide;

public class LockedDoor {
  public final Room base_room;
  public final RoomSide direction_from_base;
  public final RoomConnection connection;

  public LockedDoor(Room base_room, RoomSide direction_from_base, RoomConnection connection) {
    this.base_room = base_room;
    this.direction_from_base = direction_from_base;
    this.connection = connection;
  }
}
