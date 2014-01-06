package pilot;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Stack;

import structure.Room;
import structure.RoomConnection;
import util.MapUtils;

import common.Constants;
import common.DescentMapException;
import common.RoomSide;

enum PyroPilotState {
  INACTIVE,
  MOVE_TO_ROOM,
  TURN_INTO_ROOM;
}


public class PyroPilot extends Pilot {
  private final Stack<RoomSide> path;
  private final HashSet<Room> visited;
  private PyroPilotState state;

  public PyroPilot() {
    path = new Stack<RoomSide>();
    visited = new HashSet<Room>();
    state = PyroPilotState.INACTIVE;
  }

  public void startPilot() {
    visitRoom(current_room);
    initState(PyroPilotState.MOVE_TO_ROOM);
  }

  public void visitRoom(Room room) {
    visited.add(room);
  }

  @Override
  public void updateCurrentRoom(Room room) {
    if (target_room_info == null || room.equals(target_room_info.getValue().neighbor)) {
      super.updateCurrentRoom(room);
      visitRoom(room);
      initState(PyroPilotState.MOVE_TO_ROOM);
    }
  }

  public void initState(PyroPilotState next_state) {
    switch (next_state) {
      case INACTIVE:
        break;
      case MOVE_TO_ROOM:
        findNextRoom();
        planToMoveToNeighborRoom(target_room_info.getKey());
        break;
      case TURN_INTO_ROOM:
        planToTurnIntoRoom(target_room_info.getKey());
        break;
      default:
        throw new DescentMapException("Unexpected PyroPilotState: " + state);
    }
    state = next_state;
  }

  @Override
  public PilotMove findNextMove(double s_elapsed) {
    updateState();
    switch (state) {
      case INACTIVE:
        return null;
      case MOVE_TO_ROOM:
        if (MapUtils.angleTo(object.getDirection(), target_x - object.getX(), target_y - object.getY()) < 0) {
          return new PilotMove(MoveDirection.FORWARD, TurnDirection.COUNTER_CLOCKWISE);
        }
        return new PilotMove(MoveDirection.FORWARD, TurnDirection.CLOCKWISE);
      case TURN_INTO_ROOM:
        if (MapUtils.angleTo(object.getDirection(), target_direction) < 0) {
          return new PilotMove(MoveDirection.FORWARD, TurnDirection.COUNTER_CLOCKWISE);
        }
        return new PilotMove(MoveDirection.FORWARD, TurnDirection.CLOCKWISE);
      default:
        throw new DescentMapException("Unexpected PyroPilotState: " + state);
    }
  }

  public void updateState() {
    switch (state) {
      case INACTIVE:
        break;
      case MOVE_TO_ROOM:
        if (Math.abs(target_x - object.getX()) < object_radius &&
                Math.abs(target_y - object.getY()) < object_radius) {
          initState(PyroPilotState.TURN_INTO_ROOM);
        }
        break;
      case TURN_INTO_ROOM:
        if (Math.abs(target_direction - object.getDirection()) < Constants.PILOT_DIRECTION_EPSILON) {
          current_room = target_room_info.getValue().neighbor;
          visitRoom(current_room);
          initState(PyroPilotState.MOVE_TO_ROOM);
        }
        break;
      default:
        throw new DescentMapException("Unexpected PyroPilotState: " + state);
    }
  }

  public void findNextRoom() {
    ArrayList<Entry<RoomSide, RoomConnection>> possible_next_infos =
            new ArrayList<Entry<RoomSide, RoomConnection>>();
    for (Entry<RoomSide, RoomConnection> entry : current_room.getNeighbors().entrySet()) {
      Room neighbor = entry.getValue().neighbor;
      if (!visited.contains(neighbor)) {
        possible_next_infos.add(entry);
      }
    }
    if (!possible_next_infos.isEmpty()) {
      target_room_info = possible_next_infos.get((int) (Math.random() * possible_next_infos.size()));
      path.push(target_room_info.getKey());
    }
    else {
      RoomSide original_direction = path.pop();
      RoomSide return_direction = RoomSide.opposite(original_direction);
      target_room_info =
              new SimpleImmutableEntry<RoomSide, RoomConnection>(return_direction,
                      current_room.getConnectionInDirection(return_direction));
    }
  }
}
