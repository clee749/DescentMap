package pilot;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map.Entry;

import mapobject.unit.pyro.Pyro;
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
  MOVE_TO_ROOM_INTERIOR,
  REACT_TO_PYRO;
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
    initState(RobotPilotState.TURN_TO_ROOM_INTERIOR);
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
          planMoveToRoomConnection(target_room_info.getKey(), object_radius);
          planTurnToTarget();
        }
        break;
      case MOVE_TO_ROOM_EXIT:
        break;
      case TURN_INTO_ROOM:
        planMoveToNeighborRoom(target_room_info.getKey(), object_diameter);
        planTurnToTarget();
        break;
      case MOVE_INTO_ROOM:
        break;
      case TURN_TO_ROOM_INTERIOR:
        Point2D.Double target_location =
                MapUtils.randomInternalPoint(current_room.getNWCorner(), current_room.getSECorner(),
                        object_radius);
        setTargetLocation(target_location.x, target_location.y);
        planTurnToTarget();
        break;
      case MOVE_TO_ROOM_INTERIOR:
        break;
      case REACT_TO_PYRO:
        break;
      default:
        throw new DescentMapException("Unexpected RobotPilotState: " + state);
    }
    state = next_state;
  }

  @Override
  public PilotAction findNextAction(double s_elapsed) {
    updateState(s_elapsed);
    switch (state) {
      case INACTIVE:
        return new PilotAction();
      case TURN_TO_ROOM_EXIT:
        return new PilotAction(TurnDirection.angleToTurnDirection(MapUtils.angleTo(object.getDirection(),
                target_direction)));
      case MOVE_TO_ROOM_EXIT:
        return new PilotAction(MoveDirection.FORWARD, TurnDirection.angleToTurnDirection(MapUtils.angleTo(
                object.getDirection(), target_x - object.getX(), target_y - object.getY())));
      case TURN_INTO_ROOM:
        return new PilotAction(TurnDirection.angleToTurnDirection(MapUtils.angleTo(object.getDirection(),
                target_direction)));
      case MOVE_INTO_ROOM:
        return new PilotAction(MoveDirection.FORWARD);
      case TURN_TO_ROOM_INTERIOR:
        return new PilotAction(TurnDirection.angleToTurnDirection(MapUtils.angleTo(object.getDirection(),
                target_direction)));
      case MOVE_TO_ROOM_INTERIOR:
        return new PilotAction(MoveDirection.FORWARD, TurnDirection.angleToTurnDirection(MapUtils.angleTo(
                object.getDirection(), target_x - object.getX(), target_y - object.getY())));
      case REACT_TO_PYRO:
        double angle_to_target =
                MapUtils.angleTo(object.getDirection(), target_object.getX() - object.getX(),
                        target_object.getY() - object.getY());
        double abs_angle_to_target = Math.abs(angle_to_target);
        if (abs_angle_to_target < Constants.PILOT_ROBOT_TARGET_DIRECTION_EPSILON) {
          return new PilotAction(MoveDirection.FORWARD, TurnDirection.angleToTurnDirection(angle_to_target),
                  abs_angle_to_target < Constants.PILOT_DIRECTION_EPSILON);
        }
        return new PilotAction(TurnDirection.angleToTurnDirection(angle_to_target));
      default:
        throw new DescentMapException("Unexpected RobotPilotState: " + state);
    }
  }

  public void updateState(double s_elapsed) {
    // reacting to a Pyro takes precedence over all other states
    if (!state.equals(RobotPilotState.REACT_TO_PYRO)) {
      // look for a Pyro in the same Room
      for (Pyro pyro : current_room.getPyros()) {
        target_object = pyro;
        target_object_room = pyro.getRoom();
        initState(RobotPilotState.REACT_TO_PYRO);
        return;
      }

      // look for a visible Pyro in a neighbor Room
      for (Entry<RoomSide, RoomConnection> entry : current_room.getNeighbors().entrySet()) {
        RoomConnection connection = entry.getValue();
        for (Pyro pyro : connection.neighbor.getPyros()) {
          if (canSeeLocationInNeighborRoom(pyro.getX(), pyro.getY(), entry.getKey(), connection)) {
            target_object = pyro;
            target_object_room = connection.neighbor;
            target_object_room_info = entry;
            initState(RobotPilotState.REACT_TO_PYRO);
            return;
          }
        }
      }
    }

    // handle states if already reacting to Pyro or no Pyro found
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
      case REACT_TO_PYRO:
        if (target_object_room.equals(current_room)) {
          if (!target_object.getRoom().equals(target_object_room)) {
            initState(RobotPilotState.INACTIVE);
          }
        }
        else if (!target_object.getRoom().equals(target_object_room) ||
                !canSeeLocationInNeighborRoom(target_object.getX(), target_object.getY(),
                        target_object_room_info.getKey(), target_object_room_info.getValue())) {
          initState(RobotPilotState.INACTIVE);
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
