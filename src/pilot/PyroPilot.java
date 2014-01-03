package pilot;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Stack;

import mapobject.unit.pyro.Pyro;
import structure.Room;
import structure.RoomConnection;

import common.Constants;
import common.DescentMapException;
import common.MapUtils;
import common.RoomSide;

enum PyroPilotState {
  INACTIVE, MOVE_TO_ROOM, TURN_INTO_ROOM;
}


public class PyroPilot extends Pilot {
  private final Stack<RoomSide> path;
  private final HashSet<Room> visited;
  private PyroPilotState state;

  public PyroPilot(Pyro ship) {
    super(ship);
    path = new Stack<RoomSide>();
    visited = new HashSet<Room>();
    state = PyroPilotState.INACTIVE;
  }

  @Override
  public void updateCurrentRoom(Room room) {
    if (current_room == null ||
            (target_room_info != null && room.equals(target_room_info.getValue().neighbor))) {
      current_room = room;
      visitRoom(room);
      initMoveToRoomState();
    }
  }

  public void startPilot() {
    visitRoom(current_room);
    initMoveToRoomState();
  }

  public void initMoveToRoomState() {
    findNextRoom();
    planToMoveToNeighborRoom(target_room_info.getKey());
    state = PyroPilotState.MOVE_TO_ROOM;
  }

  public void initTurnIntoRoomState() {
    planToTurnIntoRoom(target_room_info.getKey());
    state = PyroPilotState.TURN_INTO_ROOM;
  }

  public void setTargetLocation(double target_x, double target_y) {
    this.target_x = target_x;
    this.target_y = target_y;
  }

  public void setTargetDirection(double target_direction) {
    this.target_direction = target_direction;
  }

  @Override
  public PilotMove findNextMove() {
    double current_x = unit.getX();
    double current_y = unit.getY();
    double current_direction = unit.getDirection();
    updateState();
    switch (state) {
      case INACTIVE:
        return null;
      case MOVE_TO_ROOM:
        if (MapUtils.angleTo(current_direction, target_x - current_x, target_y - current_y) < 0) {
          return new PilotMove(MoveDirection.FORWARD, TurnDirection.COUNTER_CLOCKWISE);
        }
        return new PilotMove(MoveDirection.FORWARD, TurnDirection.CLOCKWISE);
      case TURN_INTO_ROOM:
        if (MapUtils.angleTo(current_direction, target_direction) < 0) {
          return new PilotMove(MoveDirection.FORWARD, TurnDirection.COUNTER_CLOCKWISE);
        }
        return new PilotMove(MoveDirection.FORWARD, TurnDirection.CLOCKWISE);
      default:
        throw new DescentMapException("Unexpected PyroPilotState: " + state);
    }
  }

  public Room findNextRoom() {
    for (Entry<RoomSide, RoomConnection> entry : current_room.getNeighbors().entrySet()) {
      Room neighbor = entry.getValue().neighbor;
      if (!visited.contains(neighbor)) {
        path.push(entry.getKey());
        target_room_info = entry;
        return neighbor;
      }
    }
    RoomSide original_direction = path.pop();
    RoomSide return_direction = RoomSide.opposite(original_direction);
    target_room_info =
            new SimpleImmutableEntry<RoomSide, RoomConnection>(return_direction,
                    current_room.getConnectionInDirection(return_direction));
    return target_room_info.getValue().neighbor;
  }

  public void visitRoom(Room room) {
    visited.add(room);
  }

  public void updateState() {
    switch (state) {
      case INACTIVE:
        break;
      case MOVE_TO_ROOM:
        if (Math.abs(target_x - unit.getX()) < unit.getRadius() &&
                Math.abs(target_y - unit.getY()) < unit.getRadius()) {
          initTurnIntoRoomState();
        }
        break;
      case TURN_INTO_ROOM:
        if (Math.abs(target_direction - unit.getDirection()) < Constants.PILOT_DIRECTION_EPSILON) {
          current_room = target_room_info.getValue().neighbor;
          visitRoom(current_room);
          initMoveToRoomState();
        }
        break;
      default:
        throw new DescentMapException("Unexpected PyroPilotState: " + state);
    }
  }

  public void planToMoveToNeighborRoom(RoomSide direction) {
    RoomConnection connection = current_room.getConnectionInDirection(direction);
    double middle = (connection.min + connection.max) / 2.0;
    switch (direction) {
      case EAST:
        target_x = current_room.getSECorner().x - 0.5;
        target_y = middle;
        break;
      case WEST:
        target_x = current_room.getNWCorner().x + 0.5;
        target_y = middle;
        break;
      case NORTH:
        target_x = middle;
        target_y = current_room.getNWCorner().y + 0.5;
        break;
      case SOUTH:
        target_x = middle;
        target_y = current_room.getSECorner().y - 0.5;
        break;
      default:
        throw new DescentMapException("Unexpected RoomSide: " + direction);
    }
  }

  public void planToTurnIntoRoom(RoomSide direction) {
    // target_directions for north and south are reversed because y-coordinates increase down the
    // screen
    switch (direction) {
      case EAST:
        target_direction = 0.0;
        break;
      case NORTH:
        target_direction = Constants.THREE_PI_OVER_TWO;
        break;
      case WEST:
        target_direction = Math.PI;
        break;
      case SOUTH:
        target_direction = Constants.PI_OVER_TWO;
        break;
      default:
        throw new DescentMapException("Unexpected RoomSide: " + direction);
    }
  }
}
