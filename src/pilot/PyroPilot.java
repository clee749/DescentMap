package pilot;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Stack;

import mapstructure.Room;
import mapstructure.RoomConnection;

import common.RoomSide;

public class PyroPilot implements Pilot {
  private final Stack<Room> path;
  private final HashSet<Room> visited;
  private Room current_room;

  public PyroPilot(Room current_room) {
    path = new Stack<Room>();
    visited = new HashSet<Room>();
    setCurrentRoom(current_room);
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

    // current room, with no unvisited neighbors
    path.pop();

    // previous room, possibly with unvisited neighbors
    return path.peek();
  }

  public void visitRoom(Room room) {
    current_room = room;
    if (visited.add(room)) {
      path.push(room);
    }
  }
}
