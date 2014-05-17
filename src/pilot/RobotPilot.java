package pilot;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map.Entry;

import mapobject.MovableObject;
import mapobject.powerup.Cloak;
import mapobject.shot.Shot;
import mapobject.unit.Pyro;
import mapobject.unit.robot.Robot;
import structure.Room;
import structure.RoomConnection;
import util.MapUtils;

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
  REACT_TO_PYRO,
  REACT_TO_CLOAKED_PYRO;
}


public class RobotPilot extends UnitPilot {
  public static final double TARGET_DIRECTION_EPSILON = MapUtils.PI_OVER_TWO;
  public static final double MIN_DISTANCE_TO_PYRO = 1.0;
  public static final double PREFERRED_DISTANCE_TO_PYRO_RANGE = 0.1;
  public static final double START_EXPLORE_PROB = 0.1;
  public static final double STOP_EXPLORE_PROB = 0.1;
  public static final double EVASIVE_START_EXPLORE_PROB = 0.5;

  protected double room_traversal_margin;
  protected double min_distance_to_pyro2;
  protected double max_distance_to_pyro2;
  protected RobotPilotState state;
  protected Room previous_exploration_room;
  protected double react_to_cloaked_pyro_time_left;
  protected boolean can_growl;

  @Override
  public void bindToObject(MovableObject object) {
    super.bindToObject(object);
    room_traversal_margin = Math.min(bound_object_diameter, 0.5);
    double min_distance_to_pyro = Math.max(bound_object_diameter, MIN_DISTANCE_TO_PYRO);
    min_distance_to_pyro2 = Math.pow(min_distance_to_pyro, 2);
    max_distance_to_pyro2 = Math.pow(min_distance_to_pyro + PREFERRED_DISTANCE_TO_PYRO_RANGE, 2);
  }

  @Override
  public void startPilot() {
    state = RobotPilotState.INACTIVE;
  }

  @Override
  public void updateCurrentRoom(Room room) {
    super.updateCurrentRoom(room);
    initNewRoomState();
  }

  public void initNewRoomState() {
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
          planMoveToRoomConnection(target_room_info.getKey(), room_traversal_margin);
          planTurnToTarget();
        }
        break;
      case MOVE_TO_ROOM_EXIT:
        break;
      case TURN_INTO_ROOM:
        planMoveToNeighborRoom(target_room_info.getKey(), room_traversal_margin);
        planTurnToTarget();
        break;
      case MOVE_INTO_ROOM:
        break;
      case TURN_TO_ROOM_INTERIOR:
        Point2D.Double target_location =
                MapUtils.randomInternalPoint(current_room.getNWCorner(), current_room.getSECorner(),
                        bound_object_radius);
        setTargetLocation(target_location.x, target_location.y);
        planTurnToTarget();
        break;
      case MOVE_TO_ROOM_INTERIOR:
        break;
      case REACT_TO_PYRO:
        initReactToPyroState();
        break;
      case REACT_TO_CLOAKED_PYRO:
        initReactToCloakedPyroState();
        break;
      default:
        throw new DescentMapException("Unexpected RobotPilotState: " + state);
    }

    state = next_state;
    can_growl = false;
  }

  public void initReactToPyroState() {
    previous_exploration_room = null;
  }

  public void initReactToCloakedPyroState() {
    previous_exploration_room = null;
    setTargetLocation(target_unit);
    react_to_cloaked_pyro_time_left = Cloak.CLOAK_TIME;
  }

  @Override
  public PilotAction findNextAction(double s_elapsed) {
    updateState(s_elapsed);
    if (can_growl) {
      ((Robot) bound_object).allowGrowl();
    }
    StrafeDirection strafe = reactToShots();

    switch (state) {
      case INACTIVE:
        return new PilotAction(strafe);
      case TURN_TO_ROOM_EXIT:
        return new PilotAction(strafe, angleToTurnDirection(MapUtils.angleTo(bound_object.getDirection(),
                target_direction)));
      case MOVE_TO_ROOM_EXIT:
        return new PilotAction(MoveDirection.FORWARD, strafe, angleToTurnDirection(MapUtils.angleTo(
                bound_object, target_x, target_y)));
      case TURN_INTO_ROOM:
        return new PilotAction(strafe, angleToTurnDirection(MapUtils.angleTo(bound_object.getDirection(),
                target_direction)));
      case MOVE_INTO_ROOM:
        return new PilotAction(MoveDirection.FORWARD, strafe);
      case TURN_TO_ROOM_INTERIOR:
        return new PilotAction(strafe, angleToTurnDirection(MapUtils.angleTo(bound_object.getDirection(),
                target_direction)));
      case MOVE_TO_ROOM_INTERIOR:
        return new PilotAction(MoveDirection.FORWARD, strafe, angleToTurnDirection(MapUtils.angleTo(
                bound_object, target_x, target_y)));
      case REACT_TO_PYRO:
        return findReactToPyroAction(strafe);
      case REACT_TO_CLOAKED_PYRO:
        react_to_cloaked_pyro_time_left -= s_elapsed;
        return findReactToCloakedPyroAction(strafe);
      default:
        throw new DescentMapException("Unexpected RobotPilotState: " + state);
    }
  }

  public StrafeDirection reactToShots() {
    for (Shot shot : current_room.getShots()) {
      if (shot.getSource().equals(bound_object)) {
        continue;
      }
      StrafeDirection strafe = findReactionToShot(shot);
      if (!strafe.equals(StrafeDirection.NONE)) {
        return strafe;
      }
    }
    return StrafeDirection.NONE;
  }

  public PilotAction findReactToPyroAction(StrafeDirection strafe) {
    return findReactToTargetAction(strafe, target_unit.getX(), target_unit.getY());
  }

  public PilotAction findReactToCloakedPyroAction(StrafeDirection strafe) {
    return findReactToTargetAction(strafe, target_x, target_y);
  }

  public PilotAction findReactToTargetAction(StrafeDirection strafe, double target_x, double target_y) {
    double angle_to_target = MapUtils.angleTo(bound_object, target_x, target_y);
    double abs_angle_to_target = Math.abs(angle_to_target);
    if (abs_angle_to_target > TARGET_DIRECTION_EPSILON) {
      return new PilotAction(strafe, angleToTurnDirection(angle_to_target));
    }
    double distance_to_target2 = MapUtils.distance2(bound_object, target_x, target_y);
    MoveDirection move;
    if (distance_to_target2 > max_distance_to_pyro2) {
      move = MoveDirection.FORWARD;
    }
    else if (distance_to_target2 < min_distance_to_pyro2) {
      move = MoveDirection.BACKWARD;
    }
    else {
      move = MoveDirection.NONE;
    }
    return new PilotAction(move, strafe, angleToTurnDirection(angle_to_target),
            abs_angle_to_target < DIRECTION_EPSILON);
  }

  public void updateState(double s_elapsed) {
    // reacting to a Pyro takes precedence over all other states
    if (!state.equals(RobotPilotState.REACT_TO_PYRO) && searchForPyro()) {
      return;
    }

    // handle states if already reacting to Pyro or no Pyro found
    switch (state) {
      case INACTIVE:
        updateInactiveState(s_elapsed);
        break;
      case TURN_TO_ROOM_EXIT:
        if (MapUtils.isAngleDeltaLessThan(bound_object.getDirection(), target_direction, DIRECTION_EPSILON)) {
          initState(RobotPilotState.MOVE_TO_ROOM_EXIT);
        }
        break;
      case MOVE_TO_ROOM_EXIT:
        if (MapUtils.isPointInObject(bound_object, target_x, target_y)) {
          initState(RobotPilotState.TURN_INTO_ROOM);
        }
        break;
      case TURN_INTO_ROOM:
        if (MapUtils.isAngleDeltaLessThan(bound_object.getDirection(), target_direction, DIRECTION_EPSILON)) {
          initState(RobotPilotState.MOVE_INTO_ROOM);
        }
        break;
      case MOVE_INTO_ROOM:
        break;
      case TURN_TO_ROOM_INTERIOR:
        if (MapUtils.isAngleDeltaLessThan(bound_object.getDirection(), target_direction, DIRECTION_EPSILON)) {
          initState(RobotPilotState.MOVE_TO_ROOM_INTERIOR);
        }
        break;
      case MOVE_TO_ROOM_INTERIOR:
        if (MapUtils.isPointInObject(bound_object, target_x, target_y)) {
          if (Math.random() < STOP_EXPLORE_PROB) {
            initState(RobotPilotState.INACTIVE);
          }
          else {
            initState(RobotPilotState.TURN_TO_ROOM_EXIT);
          }
        }
        break;
      case REACT_TO_PYRO:
        updateReactToPyroState(s_elapsed);
        break;
      case REACT_TO_CLOAKED_PYRO:
        if (react_to_cloaked_pyro_time_left < 0.0) {
          initState(RobotPilotState.INACTIVE);
          return;
        }
        updateReactToCloakedPyroState(s_elapsed);
        break;
      default:
        throw new DescentMapException("Unexpected RobotPilotState: " + state);
    }
  }

  public boolean searchForPyro() {
    // look for a Pyro in the same Room
    Pyro target_pyro = null;
    double smallest_angle_to_pyro = Math.PI;
    for (Pyro pyro : current_room.getPyros()) {
      if (pyro.isVisible()) {
        double abs_angle_to_pyro = Math.abs(MapUtils.angleTo(bound_object, pyro));
        if (abs_angle_to_pyro < smallest_angle_to_pyro) {
          target_pyro = pyro;
          smallest_angle_to_pyro = abs_angle_to_pyro;
        }
      }
    }
    if (target_pyro != null) {
      markPyroAsTarget(target_pyro);
      return true;
    }

    // look for a visible Pyro in a neighbor Room
    for (Entry<RoomSide, RoomConnection> entry : current_room.getNeighbors().entrySet()) {
      RoomConnection connection = entry.getValue();
      for (Pyro pyro : connection.neighbor.getPyros()) {
        if (pyro.isVisible() && MapUtils.canSeeObjectInNeighborRoom(bound_object, pyro, entry.getKey())) {
          markPyroAsTarget(pyro);
          target_object_room_info = entry;
          return true;
        }
      }
    }
    return false;
  }

  public void markPyroAsTarget(Pyro pyro) {
    target_unit = pyro;
    target_object_room = pyro.getRoom();
    initState(pyro.isCloaked() ? RobotPilotState.REACT_TO_CLOAKED_PYRO : RobotPilotState.REACT_TO_PYRO);
  }

  public void updateInactiveState(double s_elapsed) {
    if (Math.random() / s_elapsed < START_EXPLORE_PROB) {
      initState(RobotPilotState.TURN_TO_ROOM_INTERIOR);
    }
    else {
      for (Robot robot : current_room.getRobots()) {
        if (robot.equals(bound_object)) {
          continue;
        }
        if (MapUtils.objectsIntersect(bound_object, robot, bound_object_diameter) &&
                Math.random() / s_elapsed < EVASIVE_START_EXPLORE_PROB) {
          initState(RobotPilotState.TURN_TO_ROOM_INTERIOR);
          break;
        }
      }
    }
  }

  public void updateReactToPyroState(double s_elapsed) {
    can_growl = true;
    if (!target_unit.isInMap()) {
      initState(RobotPilotState.INACTIVE);
      can_growl = true;
    }
    else if (target_object_room.equals(current_room)) {
      if (!target_unit.getRoom().equals(target_object_room)) {
        initState(RobotPilotState.INACTIVE);
      }
      else if (target_unit.isCloaked()) {
        initState(RobotPilotState.REACT_TO_CLOAKED_PYRO);
      }
    }
    else {
      if (!target_unit.getRoom().equals(target_object_room) ||
              !MapUtils.canSeeObjectInNeighborRoom(bound_object, target_unit,
                      target_object_room_info.getKey())) {
        initState(RobotPilotState.INACTIVE);
      }
      else if (target_unit.isCloaked()) {
        initState(RobotPilotState.REACT_TO_CLOAKED_PYRO);
      }
    }
  }

  public void updateReactToCloakedPyroState(double s_elapsed) {
    if (!target_unit.isVisible()) {
      return;
    }
    can_growl = true;
    if (!target_unit.isInMap()) {
      initState(RobotPilotState.INACTIVE);
      can_growl = true;
    }
    else if (target_object_room.equals(current_room)) {
      if (target_unit.getRoom().equals(target_object_room)) {
        if (!target_unit.isCloaked()) {
          initState(RobotPilotState.REACT_TO_PYRO);
          return;
        }
        setTargetLocation(target_unit);
      }
    }
    else if (MapUtils.canSeeObjectInNeighborRoom(bound_object, target_unit, target_object_room_info.getKey())) {
      if (!target_unit.isCloaked()) {
        initState(RobotPilotState.REACT_TO_PYRO);
        return;
      }
      setTargetLocation(target_unit);
    }
  }

  public void findNextExplorationRoom() {
    ArrayList<Entry<RoomSide, RoomConnection>> possible_next_infos = findPossibleNextRoomInfos();
    if (possible_next_infos.isEmpty()) {
      target_room_info = null;
    }
    else {
      target_room_info = possible_next_infos.get((int) (Math.random() * possible_next_infos.size()));
    }
  }

  public ArrayList<Entry<RoomSide, RoomConnection>> findPossibleNextRoomInfos() {
    ArrayList<Entry<RoomSide, RoomConnection>> possible_next_infos =
            new ArrayList<Entry<RoomSide, RoomConnection>>();
    for (Entry<RoomSide, RoomConnection> entry : current_room.getNeighbors().entrySet()) {
      Room neighbor = entry.getValue().neighbor;
      if (!neighbor.equals(previous_exploration_room)) {
        possible_next_infos.add(entry);
      }
    }
    return possible_next_infos;
  }
}
