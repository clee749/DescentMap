package pilot;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Stack;

import mapobject.unit.Unit;
import structure.Room;
import structure.RoomConnection;
import util.MapUtils;

import common.Constants;
import common.DescentMapException;
import common.ObjectType;
import common.RoomSide;

enum PyroPilotState {
  INACTIVE,
  MOVE_TO_ROOM_CONNECTION,
  MOVE_TO_NEIGHBOR_ROOM;
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
    initState(PyroPilotState.MOVE_TO_ROOM_CONNECTION);
  }

  public void visitRoom(Room room) {
    visited.add(room);
  }

  @Override
  public void updateCurrentRoom(Room room) {
    if (target_room_info == null || room.equals(target_room_info.getValue().neighbor)) {
      super.updateCurrentRoom(room);
      visitRoom(room);
      initState(PyroPilotState.MOVE_TO_ROOM_CONNECTION);
    }
  }

  public void initState(PyroPilotState next_state) {
    switch (next_state) {
      case INACTIVE:
        break;
      case MOVE_TO_ROOM_CONNECTION:
        findNextRoom();
        planMoveToRoomConnection(target_room_info.getKey());
        break;
      case MOVE_TO_NEIGHBOR_ROOM:
        planMoveToNeighborRoom(target_room_info.getKey());
        break;
      default:
        throw new DescentMapException("Unexpected PyroPilotState: " + state);
    }
    state = next_state;
  }

  @Override
  public PilotAction findNextAction(double s_elapsed) {
    updateState();

    boolean fire_cannon = false;
    for (Unit unit : current_room.getUnits()) {
      if (unit.getType().equals(ObjectType.Pyro)) {
        continue;
      }
      double abs_angle_to_unit =
              Math.abs(MapUtils.angleTo(object.getDirection(), unit.getX() - object.getX(), unit.getY() -
                      object.getY()));
      if (abs_angle_to_unit < Constants.PILOT_DIRECTION_EPSILON) {
        fire_cannon = true;
        break;
      }
    }

    switch (state) {
      case INACTIVE:
        return null;
      case MOVE_TO_ROOM_CONNECTION:
        return new PilotAction(MoveDirection.FORWARD, TurnDirection.angleToTurnDirection(MapUtils.angleTo(
                object.getDirection(), target_x - object.getX(), target_y - object.getY())), fire_cannon);
      case MOVE_TO_NEIGHBOR_ROOM:
        return new PilotAction(MoveDirection.FORWARD, TurnDirection.angleToTurnDirection(MapUtils.angleTo(
                object.getDirection(), target_x - object.getX(), target_y - object.getY())), fire_cannon);
      default:
        throw new DescentMapException("Unexpected PyroPilotState: " + state);
    }
  }

  public void updateState() {
    switch (state) {
      case INACTIVE:
        break;
      case MOVE_TO_ROOM_CONNECTION:
        if (Math.abs(target_x - object.getX()) < object_radius &&
                Math.abs(target_y - object.getY()) < object_radius) {
          initState(PyroPilotState.MOVE_TO_NEIGHBOR_ROOM);
        }
        break;
      case MOVE_TO_NEIGHBOR_ROOM:
        if (Math.abs(target_x - object.getX()) < object_radius &&
                Math.abs(target_y - object.getY()) < object_radius) {
          updateCurrentRoom(target_room_info.getValue().neighbor);
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
