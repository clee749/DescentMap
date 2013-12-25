package mapstructure;

import common.RoomSide;

public class RoomConnection {
  private final int min;
  private final int max;
  private final Room neighbor;
  
  public RoomConnection(int min, int max, Room neighbor) {
    this.min = min;
    this.max = max;
    this.neighbor = neighbor;
  }
  
  public RoomConnection(Room base_room, Room neighbor, RoomSide direction) {
    this.neighbor = neighbor;
    if (direction.equals(RoomSide.East) || direction.equals(RoomSide.West)) {
      min = Math.max(neighbor.getNWCorner().y, base_room.getNWCorner().y);
      max = Math.min(neighbor.getSECorner().y, base_room.getSECorner().y);
    }
    else {
      min = Math.max(neighbor.getNWCorner().x, base_room.getNWCorner().x);
      max = Math.min(neighbor.getSECorner().x, base_room.getSECorner().x);
    }
  }
  
  public int getMin() {
    return min;
  }
  
  public int getMax() {
    return max;
  }
  
  public Room getNeighbor() {
    return neighbor;
  }
}
