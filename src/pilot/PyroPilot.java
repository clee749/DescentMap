package pilot;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import mapstructure.Room;
import mapstructure.RoomConnection;

import common.RoomSide;

public class PyroPilot implements Pilot {
  private final LinkedList<Room> path;
  private final HashSet<Room> visited;
  private Room current_room;

  public PyroPilot() {
    path = new LinkedList<Room>();
    visited = new HashSet<Room>();
  }

  public void setCurrentRoom(Room current_room) {
    this.current_room = current_room;
    visitRoom(current_room);
  }

  @Override
  public PilotMove nextMove() {
    return null;
  }

  public Room nextRoom() {
    for (Entry<RoomSide, RoomConnection> entry : current_room.getNeighbors().entrySet()) {
      Room neighbor = entry.getValue().neighbor;
      if (!visited.contains(neighbor)) {
        return neighbor;
      }
    }
    return path.pop();
  }

  public void visitRoom(Room room) {
    current_room = room;
    if (visited.add(room)) {
      path.add(room);
    }
  }
}
