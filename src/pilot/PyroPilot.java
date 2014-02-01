package pilot;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Stack;

import mapobject.MapObject;
import mapobject.MovableObject;
import mapobject.powerup.ConcussionPack;
import mapobject.powerup.Energy;
import mapobject.powerup.HomingPack;
import mapobject.powerup.Powerup;
import mapobject.powerup.Shield;
import mapobject.scenery.Entrance;
import mapobject.shot.Shot;
import mapobject.unit.Pyro;
import mapobject.unit.Unit;
import pyro.PyroPrimaryCannon;
import pyro.PyroSecondaryCannon;
import structure.Room;
import structure.RoomConnection;
import util.MapUtils;
import cannon.LaserCannon;

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
  private static final Point[] PRIMARY_CANNON_PREFERRED_ENERGIES = getPrimaryCannonPreferredEnergies();

  private static Point[] getPrimaryCannonPreferredEnergies() {
    Point[] energies = new Point[PyroPrimaryCannon.values().length];
    energies[PyroPrimaryCannon.LASER.ordinal()] = new Point(0, 100);
    energies[PyroPrimaryCannon.PLASMA.ordinal()] = new Point(100, 200);
    return energies;
  }

  public static final double TIME_TURNING_UNTIL_STOP = 5.0;
  public static final double RESPAWN_DELAY = 5.0;
  public static final double SPAWNING_SICKNESS = Entrance.ZUNGGG_TIME - Entrance.TIME_TO_SPAWN;
  public static final int MISSILE_SHIELD_THRESHOLD = Shot.getDamage(ObjectType.ConcussionMissile);
  public static final double MISSILE_MIN_DISTANCE2 = 4.0;

  private final HashSet<Room> visited;
  private LinkedList<Entry<RoomSide, RoomConnection>> current_path;
  private PyroPilotState state;
  private Pyro pyro;
  private double time_turning_to_target;
  private TurnDirection previous_turn_to_target;
  private double respawn_delay_left;
  private double inactive_time_left;

  public PyroPilot() {
    visited = new HashSet<Room>();
    current_path = new LinkedList<Entry<RoomSide, RoomConnection>>();
    state = PyroPilotState.INACTIVE;
  }

  @Override
  public void bindToObject(MovableObject object) {
    super.bindToObject(object);
    pyro = (Pyro) object;
  }

  public void startPilot() {
    visitRoom(current_room);
    inactive_time_left = SPAWNING_SICKNESS;
    initState(PyroPilotState.INACTIVE);
  }

  public void prepareForRespawn() {
    visited.remove(current_room);
    current_path.clear();
    respawn_delay_left = RESPAWN_DELAY;
    initState(PyroPilotState.INACTIVE);
  }

  public void handleRespawnDelay(double s_elapsed) {
    respawn_delay_left -= s_elapsed;
  }

  public boolean isReadyToRespawn() {
    return respawn_delay_left < 0.0;
  }

  public void visitRoom(Room room) {
    visited.add(room);
  }

  @Override
  public void updateCurrentRoom(Room room) {
    super.updateCurrentRoom(room);
    visitRoom(room);
    if (target_room_info != null && !room.equals(target_room_info.getValue().neighbor)) {
      current_path.clear();
    }
    initState(PyroPilotState.REACT_TO_OBJECT);
  }

  public void initState(PyroPilotState next_state) {
    switch (next_state) {
      case INACTIVE:
        break;
      case MOVE_TO_ROOM_CONNECTION:
        findNextRoom();
        planMoveToRoomConnection(target_room_info.getKey(), bound_object_radius);
        if (pyro.isCannonReloaded() && pyro.isSecondaryCannonReloaded()) {
          selectPrimaryCannon();
        }
        break;
      case MOVE_TO_NEIGHBOR_ROOM:
        planMoveToNeighborRoom(target_room_info.getKey(), bound_object_radius);
        if (pyro.isCannonReloaded() && pyro.isSecondaryCannonReloaded()) {
          selectSecondaryCannon();
        }
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
    // StrafeDirection strafe = reactToShots();

    boolean fire_cannon = false;
    boolean fire_secondary = false;
    for (Unit unit : current_room.getRobots()) {
      if (Math.abs(MapUtils.angleTo(bound_object, unit)) < DIRECTION_EPSILON) {
        fire_cannon = true;
        MapObject last_missile_fired = pyro.getLastMissileFired();
        if ((last_missile_fired == null || !last_missile_fired.isInMap()) &&
                unit.getShields() > MISSILE_SHIELD_THRESHOLD &&
                MapUtils.distance2(bound_object, unit) > MISSILE_MIN_DISTANCE2) {
          fire_secondary = true;
          break;
        }
      }
    }

    if (!fire_cannon && state.equals(PyroPilotState.MOVE_TO_ROOM_CONNECTION)) {
      RoomConnection connection = target_room_info.getValue();
      for (Unit unit : connection.neighbor.getRobots()) {
        double abs_angle_to_unit = Math.abs(MapUtils.angleTo(bound_object, unit));
        if (abs_angle_to_unit < DIRECTION_EPSILON &&
                MapUtils.canSeeObjectInNeighborRoom(bound_object, unit, target_room_info.getKey())) {
          fire_cannon = true;
          MapObject last_missile_fired = pyro.getLastMissileFired();
          if ((last_missile_fired == null || !last_missile_fired.isInMap()) &&
                  unit.getShields() > MISSILE_SHIELD_THRESHOLD &&
                  MapUtils.distance2(bound_object, unit) > MISSILE_MIN_DISTANCE2) {
            fire_secondary = true;
            break;
          }
        }
      }
    }

    switch (state) {
      case INACTIVE:
        inactive_time_left -= s_elapsed;
        return PilotAction.NO_ACTION;
      case MOVE_TO_ROOM_CONNECTION:
        return new PilotAction(MoveDirection.FORWARD, strafe,
                angleToTurnDirection(MapUtils.angleTo(bound_object.getDirection(),
                        target_x - bound_object.getX(), target_y - bound_object.getY())), fire_cannon,
                fire_secondary);
      case MOVE_TO_NEIGHBOR_ROOM:
        return new PilotAction(MoveDirection.FORWARD, strafe,
                angleToTurnDirection(MapUtils.angleTo(bound_object.getDirection(),
                        target_x - bound_object.getX(), target_y - bound_object.getY())), fire_cannon,
                fire_secondary);
      case REACT_TO_OBJECT:
        TurnDirection turn = angleToTurnDirection(MapUtils.angleTo(bound_object, target_object));
        if (!turn.equals(TurnDirection.NONE) && turn.equals(previous_turn_to_target)) {
          time_turning_to_target += s_elapsed;
        }
        else {
          previous_turn_to_target = turn;
          time_turning_to_target = 0.0;
        }
        return new PilotAction(MoveDirection.FORWARD, strafe, turn, fire_cannon, fire_secondary);
      case TURN_TO_OBJECT:
        return new PilotAction(MoveDirection.NONE, strafe, angleToTurnDirection(MapUtils.angleTo(
                bound_object, target_object)), fire_cannon, fire_secondary);
      default:
        throw new DescentMapException("Unexpected PyroPilotState: " + state);
    }
  }

  public void updateState() {
    switch (state) {
      case INACTIVE:
        if (inactive_time_left < 0.0) {
          initState(PyroPilotState.REACT_TO_OBJECT);
        }
        break;
      case MOVE_TO_ROOM_CONNECTION:
        if (Math.abs(target_x - bound_object.getX()) < bound_object_radius &&
                Math.abs(target_y - bound_object.getY()) < bound_object_radius) {
          initState(PyroPilotState.MOVE_TO_NEIGHBOR_ROOM);
        }
        break;
      case MOVE_TO_NEIGHBOR_ROOM:
        if (Math.abs(target_x - bound_object.getX()) < bound_object_radius &&
                Math.abs(target_y - bound_object.getY()) < bound_object_radius) {
          updateCurrentRoom(target_room_info.getValue().neighbor);
          initState(PyroPilotState.REACT_TO_OBJECT);
        }
        break;
      case REACT_TO_OBJECT:
        if (!target_object.isInMap() || !target_object.getRoom().equals(current_room) ||
                ((target_object instanceof Powerup) && !shouldCollectPowerup((Powerup) target_object))) {
          initState(PyroPilotState.REACT_TO_OBJECT);
        }
        else if (time_turning_to_target > TIME_TURNING_UNTIL_STOP) {
          initState(PyroPilotState.TURN_TO_OBJECT);
        }
        break;
      case TURN_TO_OBJECT:
        if (Math.abs(MapUtils.angleTo(bound_object, target_object)) < DIRECTION_EPSILON) {
          // no need to init state because we are still targeting the same object
          state = PyroPilotState.REACT_TO_OBJECT;
        }
        break;
      default:
        throw new DescentMapException("Unexpected PyroPilotState: " + state);
    }
  }

  public void findNextRoom() {
    // if we still have a path to follow, then follow it
    if (!current_path.isEmpty()) {
      target_room_info = current_path.pop();
      return;
    }

    // see if we still need to visit a neighbor Room
    ArrayList<Entry<RoomSide, RoomConnection>> possible_next_infos =
            new ArrayList<Entry<RoomSide, RoomConnection>>();
    for (Entry<RoomSide, RoomConnection> entry : current_room.getNeighbors().entrySet()) {
      Room neighbor = entry.getValue().neighbor;
      if (!visited.contains(neighbor)) {
        possible_next_infos.add(entry);
      }
    }

    // select an unvisited neighbor Room at random
    if (!possible_next_infos.isEmpty()) {
      target_room_info = possible_next_infos.get((int) (Math.random() * possible_next_infos.size()));
    }

    // find a path to an unvisited Room at min depth from the current Room
    else {
      findNextPath();
      target_room_info = current_path.pop();
    }
  }

  public void findNextPath() {
    ArrayList<LinkedList<Entry<RoomSide, RoomConnection>>> paths =
            new ArrayList<LinkedList<Entry<RoomSide, RoomConnection>>>();
    ArrayList<LinkedList<Entry<RoomSide, RoomConnection>>> partial_paths =
            new ArrayList<LinkedList<Entry<RoomSide, RoomConnection>>>();
    HashSet<Room> checked_rooms = new HashSet<Room>();

    // init partial_paths with the current neighbor Rooms because we already know they are all
    // visited
    for (Entry<RoomSide, RoomConnection> entry : current_room.getNeighbors().entrySet()) {
      LinkedList<Entry<RoomSide, RoomConnection>> partial_path =
              new LinkedList<Entry<RoomSide, RoomConnection>>();
      partial_path.push(entry);
      partial_paths.add(partial_path);
    }

    do {
      partial_paths = checkConnectedRooms(paths, partial_paths, checked_rooms);
    } while (paths.isEmpty());
    current_path = paths.get((int) (Math.random() * paths.size()));
  }

  @SuppressWarnings("unchecked")
  public ArrayList<LinkedList<Entry<RoomSide, RoomConnection>>> checkConnectedRooms(
          ArrayList<LinkedList<Entry<RoomSide, RoomConnection>>> paths_to_unvisited,
          ArrayList<LinkedList<Entry<RoomSide, RoomConnection>>> partial_paths, HashSet<Room> checked_rooms) {

    ArrayList<LinkedList<Entry<RoomSide, RoomConnection>>> next_partial_paths =
            new ArrayList<LinkedList<Entry<RoomSide, RoomConnection>>>();
    for (LinkedList<Entry<RoomSide, RoomConnection>> partial_path : partial_paths) {
      Room base_room = partial_path.getLast().getValue().neighbor;
      for (Entry<RoomSide, RoomConnection> entry : base_room.getNeighbors().entrySet()) {
        Room neighbor = entry.getValue().neighbor;
        if (!checked_rooms.add(neighbor)) {
          continue;
        }
        LinkedList<Entry<RoomSide, RoomConnection>> next_partial_path =
                (LinkedList<Entry<RoomSide, RoomConnection>>) partial_path.clone();
        next_partial_path.add(entry);
        next_partial_paths.add(next_partial_path);
        if (!visited.contains(neighbor)) {
          paths_to_unvisited.add(next_partial_path);
        }
      }
    }
    return next_partial_paths;
  }

  public ArrayList<Stack<RoomSide>> findPossiblePaths(HashSet<Room> checked_rooms, Room room_to_check) {
    if (!checked_rooms.add(room_to_check)) {
      return null;
    }

    ArrayList<Stack<RoomSide>> paths = new ArrayList<Stack<RoomSide>>();
    if (!visited.contains(room_to_check)) {
      paths.add(new Stack<RoomSide>());
      return paths;
    }

    for (Entry<RoomSide, RoomConnection> entry : room_to_check.getNeighbors().entrySet()) {
      ArrayList<Stack<RoomSide>> future_paths = findPossiblePaths(checked_rooms, entry.getValue().neighbor);
      if (future_paths != null) {
        for (Stack<RoomSide> path : future_paths) {
          path.push(entry.getKey());
        }
        paths.addAll(future_paths);
      }
    }

    return paths;
  }

  public MapObject findNextTargetObject() {
    for (Unit unit : current_room.getRobots()) {
      if (shouldTargetObject(unit)) {
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
    double abs_angle_to_object = Math.abs(MapUtils.angleTo(bound_object, other_object));
    return Math.random() < (Math.PI - abs_angle_to_object) / Math.PI;
  }

  public boolean shouldCollectPowerup(Powerup powerup) {
    switch (powerup.getType()) {
      case Shield:
        return pyro.getShields() + Shield.SHIELD_AMOUNT <= Pyro.MAX_SHIELDS;
      case Energy:
        return pyro.getEnergy() + Energy.ENERGY_AMOUNT <= Pyro.MAX_ENERGY;
      case QuadLasers:
        return !pyro.hasQuadLasers();
      case LaserCannonPowerup:
        return pyro.getLaserLevel() < LaserCannon.MAX_LEVEL;
      case PlasmaCannonPowerup:
        return !pyro.hasPrimaryCannon(PyroPrimaryCannon.PLASMA);
      case ConcussionMissilePowerup:
        return pyro.getSecondaryAmmo(PyroSecondaryCannon.CONCUSSION_MISSILE) < Pyro
                .getMaxSecondaryAmmo(PyroSecondaryCannon.CONCUSSION_MISSILE);
      case ConcussionPack:
        return pyro.getSecondaryAmmo(PyroSecondaryCannon.CONCUSSION_MISSILE) + ConcussionPack.NUM_MISSILES <= Pyro
                .getMaxSecondaryAmmo(PyroSecondaryCannon.CONCUSSION_MISSILE);
      case HomingMissilePowerup:
        return pyro.getSecondaryAmmo(PyroSecondaryCannon.HOMING_MISSILE) < Pyro
                .getMaxSecondaryAmmo(PyroSecondaryCannon.HOMING_MISSILE);
      case HomingPack:
        return pyro.getSecondaryAmmo(PyroSecondaryCannon.HOMING_MISSILE) + HomingPack.NUM_MISSILES <= Pyro
                .getMaxSecondaryAmmo(PyroSecondaryCannon.HOMING_MISSILE);
      default:
        throw new DescentMapException("Unexpected Powerup: " + powerup);
    }
  }

  public void selectPrimaryCannon() {
    double energy = pyro.getEnergy();
    PyroPrimaryCannon selected_primary_cannon_type = pyro.getSelectedPrimaryCannonType();
    Point preferred_energy = PRIMARY_CANNON_PREFERRED_ENERGIES[selected_primary_cannon_type.ordinal()];
    if (preferred_energy.x <= energy && energy <= preferred_energy.y) {
      return;
    }
    for (PyroPrimaryCannon cannon_type : PyroPrimaryCannon.values()) {
      preferred_energy = PRIMARY_CANNON_PREFERRED_ENERGIES[cannon_type.ordinal()];
      if (preferred_energy.x <= energy && energy <= preferred_energy.y) {
        pyro.switchPrimaryCannon(cannon_type);
        break;
      }
    }
  }

  public void selectSecondaryCannon() {
    if (pyro.getSelectedSecondaryCannonType().equals(PyroSecondaryCannon.CONCUSSION_MISSILE) &&
            pyro.getSecondaryAmmo(PyroSecondaryCannon.HOMING_MISSILE) > 0) {
      pyro.switchSecondaryCannon(PyroSecondaryCannon.HOMING_MISSILE);
    }
  }
}
