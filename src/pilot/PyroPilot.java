package pilot;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Stack;

import mapobject.MapObject;
import mapobject.MovableObject;
import mapobject.powerup.Powerup;
import mapobject.unit.Unit;
import mapobject.unit.pyro.Pyro;
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
  MOVE_TO_NEIGHBOR_ROOM,
  REACT_TO_OBJECT,
  TURN_TO_OBJECT;
}


public class PyroPilot extends Pilot {
  private final Stack<RoomSide> path;
  private final HashSet<Room> visited;
  private PyroPilotState state;
  private Pyro pyro;
  private double time_turning_to_target;
  private TurnDirection previous_turn_to_target;

  public PyroPilot() {
    path = new Stack<RoomSide>();
    visited = new HashSet<Room>();
    state = PyroPilotState.INACTIVE;
  }

  @Override
  public void bindToObject(MovableObject object) {
    super.bindToObject(object);
    if (object.getType().equals(ObjectType.Pyro)) {
      pyro = (Pyro) object;
    }
  }

  public void startPilot() {
    visitRoom(current_room);
    initState(PyroPilotState.REACT_TO_OBJECT);
  }

  public void visitRoom(Room room) {
    visited.add(room);
  }

  @Override
  public void updateCurrentRoom(Room room) {
    if (target_room_info == null || room.equals(target_room_info.getValue().neighbor)) {
      super.updateCurrentRoom(room);
      visitRoom(room);
      initState(PyroPilotState.REACT_TO_OBJECT);
    }
  }

  public void initState(PyroPilotState next_state) {
    switch (next_state) {
      case INACTIVE:
        break;
      case MOVE_TO_ROOM_CONNECTION:
        findNextRoom();
        planMoveToRoomConnection(target_room_info.getKey(), object_radius);
        break;
      case MOVE_TO_NEIGHBOR_ROOM:
        planMoveToNeighborRoom(target_room_info.getKey(), object_radius);
        break;
      case REACT_TO_OBJECT:
        target_object = findNextTargetObject();
        if (target_object == null) {
          initState(PyroPilotState.MOVE_TO_ROOM_CONNECTION);
          return;
        }
        previous_turn_to_target = TurnDirection.NONE;
        break;
      case TURN_TO_OBJECT:
        break;
      default:
        throw new DescentMapException("Unexpected PyroPilotState: " + state);
    }
    state = next_state;
  }

  @Override
  public PilotAction findNextAction(double s_elapsed) {
    updateState();
    StrafeDirection strafe = StrafeDirection.NONE;

    boolean fire_cannon = false;
    for (Unit unit : current_room.getUnits()) {
      if (unit.getType().equals(ObjectType.Pyro)) {
        continue;
      }
      double abs_angle_to_unit = Math.abs(MapUtils.angleTo(object, unit));
      if (abs_angle_to_unit < Constants.PILOT_DIRECTION_EPSILON) {
        fire_cannon = true;
        break;
      }
    }

    switch (state) {
      case INACTIVE:
        return null;
      case MOVE_TO_ROOM_CONNECTION:
        return new PilotAction(MoveDirection.FORWARD, strafe, TurnDirection.angleToTurnDirection(MapUtils
                .angleTo(object.getDirection(), target_x - object.getX(), target_y - object.getY())),
                fire_cannon);
      case MOVE_TO_NEIGHBOR_ROOM:
        return new PilotAction(MoveDirection.FORWARD, strafe, TurnDirection.angleToTurnDirection(MapUtils
                .angleTo(object.getDirection(), target_x - object.getX(), target_y - object.getY())),
                fire_cannon);
      case REACT_TO_OBJECT:
        TurnDirection turn = TurnDirection.angleToTurnDirection(MapUtils.angleTo(object, target_object));
        if (turn.equals(previous_turn_to_target)) {
          time_turning_to_target += s_elapsed;
        }
        else {
          previous_turn_to_target = turn;
          time_turning_to_target = 0.0;
        }
        return new PilotAction(MoveDirection.FORWARD, strafe, turn, fire_cannon);
      case TURN_TO_OBJECT:
        return new PilotAction(MoveDirection.NONE, strafe, TurnDirection.angleToTurnDirection(MapUtils
                .angleTo(object, target_object)), fire_cannon);
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
          initState(PyroPilotState.REACT_TO_OBJECT);
        }
        break;
      case REACT_TO_OBJECT:
        if (!target_object.isInMap() ||
                ((target_object instanceof Powerup) && !shouldCollectPowerup((Powerup) target_object))) {
          initState(PyroPilotState.REACT_TO_OBJECT);
        }
        else if (time_turning_to_target > Constants.PILOT_TIME_TURNING_UNTIL_STOP) {
          initState(PyroPilotState.TURN_TO_OBJECT);
        }
        break;
      case TURN_TO_OBJECT:
        if (MapUtils.angleTo(object, target_object) < Constants.PILOT_DIRECTION_EPSILON) {
          // no need to init state because we are still targeting the same object
          state = PyroPilotState.REACT_TO_OBJECT;
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

  public MapObject findNextTargetObject() {
    for (Unit unit : current_room.getUnits()) {
      if (!unit.getType().equals(ObjectType.Pyro) && shouldTargetObject(unit)) {
        return unit;
      }
    }
    for (Powerup powerup : current_room.getPowerups()) {
      if (shouldTargetObject(powerup) && shouldCollectPowerup(powerup)) {
        return powerup;
      }
    }
    return null;
  }

  public boolean shouldTargetObject(MapObject other_object) {
    double abs_angle_to_object = Math.abs(MapUtils.angleTo(object, other_object));
    return Math.random() < (Math.PI - abs_angle_to_object) / Math.PI;
  }

  public boolean shouldCollectPowerup(Powerup powerup) {
    if (pyro == null) {
      return false;
    }

    switch (powerup.getType()) {
      case Shield:
        return pyro.getShields() + Constants.POWERUP_SHIELD_AMOUNT <= Constants.PYRO_MAX_SHIELDS;
      default:
        throw new DescentMapException("Unexpected Powerup: " + powerup);
    }
  }
}
