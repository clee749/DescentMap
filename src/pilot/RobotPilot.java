package pilot;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Map.Entry;

import structure.Room;
import structure.RoomConnection;
import util.MapUtils;

import common.Constants;
import common.DescentMapException;
import common.RoomSide;

enum RobotPilotState {
  INACTIVE,
  TURN_TO_ROOM_EXIT,
  MOVE_TO_ROOM_EXIT,
  TURN_INTO_ROOM,
  MOVE_INTO_ROOM,
  TURN_TO_ROOM_INTERIOR,
  MOVE_TO_ROOM_INTERIOR;
}


public class RobotPilot extends Pilot {
  private RobotPilotState state;
  private Room previous_exploration_room;

  public RobotPilot() {
    state = RobotPilotState.INACTIVE;
  }

  @Override
  public void updateCurrentRoom(Room room) {
    super.updateCurrentRoom(room);
    if (state.equals(RobotPilotState.MOVE_INTO_ROOM)) {
      initState(RobotPilotState.TURN_TO_ROOM_INTERIOR);
    }
  }

  public void initState(RobotPilotState next_state) {
    switch (next_state) {
      case INACTIVE:
        previous_exploration_room = null;
        break;
      case TURN_TO_ROOM_EXIT:
        findNextExplorationRoom();
        if (target_room_info == null) {
          initState(RobotPilotState.INACTIVE);
          return;
        }
        else {
          previous_exploration_room = current_room;
          planToMoveToNeighborRoom(target_room_info.getKey());
          planToTurnToTarget();
        }
        break;
      case MOVE_TO_ROOM_EXIT:
        break;
      case TURN_INTO_ROOM:
        planToTurnIntoRoom(target_room_info.getKey());
        break;
      case MOVE_INTO_ROOM:
        break;
      case TURN_TO_ROOM_INTERIOR:
        Point nw_corner = current_room.getNWCorner();
        Point se_corner = current_room.getSECorner();
        double x_range = se_corner.x - nw_corner.x - 2 * object_radius;
        double y_range = se_corner.y - nw_corner.y - 2 * object_radius;
        setTargetLocation(nw_corner.x + object_radius + Math.random() * x_range, nw_corner.y + object_radius +
                Math.random() * y_range);
        planToTurnToTarget();
        break;
      case MOVE_TO_ROOM_INTERIOR:
        break;
      default:
        throw new DescentMapException("Unexpected RobotPilotState: " + state);
    }
    state = next_state;
  }

  @Override
  public PilotMove findNextMove(double s_elapsed) {
    updateState(s_elapsed);
    switch (state) {
      case INACTIVE:
        return new PilotMove(MoveDirection.NONE, TurnDirection.NONE);
      case TURN_TO_ROOM_EXIT:
        if (MapUtils.angleTo(object.getDirection(), target_direction) < 0) {
          return new PilotMove(MoveDirection.NONE, TurnDirection.COUNTER_CLOCKWISE);
        }
        return new PilotMove(MoveDirection.NONE, TurnDirection.CLOCKWISE);
      case MOVE_TO_ROOM_EXIT:
        if (MapUtils.angleTo(object.getDirection(), target_x - object.getX(), target_y - object.getY()) < 0) {
          return new PilotMove(MoveDirection.FORWARD, TurnDirection.COUNTER_CLOCKWISE);
        }
        return new PilotMove(MoveDirection.FORWARD, TurnDirection.CLOCKWISE);
      case TURN_INTO_ROOM:
        if (MapUtils.angleTo(object.getDirection(), target_direction) < 0) {
          return new PilotMove(MoveDirection.NONE, TurnDirection.COUNTER_CLOCKWISE);
        }
        return new PilotMove(MoveDirection.NONE, TurnDirection.CLOCKWISE);
      case MOVE_INTO_ROOM:
        return new PilotMove(MoveDirection.FORWARD, TurnDirection.NONE);
      case TURN_TO_ROOM_INTERIOR:
        if (MapUtils.angleTo(object.getDirection(), target_direction) < 0) {
          return new PilotMove(MoveDirection.NONE, TurnDirection.COUNTER_CLOCKWISE);
        }
        return new PilotMove(MoveDirection.NONE, TurnDirection.CLOCKWISE);
      case MOVE_TO_ROOM_INTERIOR:
        if (MapUtils.angleTo(object.getDirection(), target_x - object.getX(), target_y - object.getY()) < 0) {
          return new PilotMove(MoveDirection.FORWARD, TurnDirection.COUNTER_CLOCKWISE);
        }
        return new PilotMove(MoveDirection.FORWARD, TurnDirection.CLOCKWISE);
      default:
        throw new DescentMapException("Unexpected RobotPilotState: " + state);
    }
  }

  public void updateState(double s_elapsed) {
    switch (state) {
      case INACTIVE:
        if (Math.random() / s_elapsed < Constants.ROBOT_START_EXPLORE_PROB) {
          initState(RobotPilotState.TURN_TO_ROOM_EXIT);
        }
        break;
      case TURN_TO_ROOM_EXIT:
        if (Math.abs(target_direction - object.getDirection()) < Constants.PILOT_DIRECTION_EPSILON) {
          initState(RobotPilotState.MOVE_TO_ROOM_EXIT);
        }
        break;
      case MOVE_TO_ROOM_EXIT:
        if (Math.abs(target_x - object.getX()) < object_radius &&
                Math.abs(target_y - object.getY()) < object_radius) {
          initState(RobotPilotState.TURN_INTO_ROOM);
        }
        break;
      case TURN_INTO_ROOM:
        if (Math.abs(target_direction - object.getDirection()) < Constants.PILOT_DIRECTION_EPSILON) {
          initState(RobotPilotState.MOVE_INTO_ROOM);
        }
        break;
      case MOVE_INTO_ROOM:
        break;
      case TURN_TO_ROOM_INTERIOR:
        if (Math.abs(target_direction - object.getDirection()) < Constants.PILOT_DIRECTION_EPSILON) {
          initState(RobotPilotState.MOVE_TO_ROOM_INTERIOR);
        }
        break;
      case MOVE_TO_ROOM_INTERIOR:
        if (Math.abs(target_x - object.getX()) < object_radius &&
                Math.abs(target_y - object.getY()) < object_radius) {
          if (Math.random() / s_elapsed < Constants.ROBOT_END_EXPLORE_PROB) {
            initState(RobotPilotState.INACTIVE);
          }
          else {
            initState(RobotPilotState.TURN_TO_ROOM_EXIT);
          }
        }
        break;
      default:
        throw new DescentMapException("Unexpected RobotPilotState: " + state);
    }
  }

  public void findNextExplorationRoom() {
    ArrayList<Entry<RoomSide, RoomConnection>> possible_next_infos =
            new ArrayList<Entry<RoomSide, RoomConnection>>();
    for (Entry<RoomSide, RoomConnection> entry : current_room.getNeighbors().entrySet()) {
      Room neighbor = entry.getValue().neighbor;
      if (!neighbor.equals(previous_exploration_room)) {
        possible_next_infos.add(entry);
      }
    }
    if (possible_next_infos.isEmpty()) {
      target_room_info = null;
    }
    else {
      target_room_info = possible_next_infos.get((int) (Math.random() * possible_next_infos.size()));
    }
  }
}
