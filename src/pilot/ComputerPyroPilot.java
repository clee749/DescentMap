package pilot;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Map.Entry;

import mapobject.MapObject;
import mapobject.ProximityBomb;
import mapobject.powerup.ConcussionPack;
import mapobject.powerup.Energy;
import mapobject.powerup.HomingPack;
import mapobject.powerup.Powerup;
import mapobject.powerup.ProximityPack;
import mapobject.powerup.Shield;
import mapobject.scenery.Entrance;
import mapobject.scenery.Scenery;
import mapobject.shot.MegaMissile;
import mapobject.shot.Shot;
import mapobject.shot.SmartMissile;
import mapobject.unit.Pyro;
import mapobject.unit.Unit;
import mapobject.unit.robot.Robot;
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
  TURN_TO_OBJECT,
  REACT_TO_CLOAKED_ROBOT;
}


enum PyroTargetType {
  UNIT,
  POWERUP;
}


class RoomAnalysis {
  public boolean is_another_pyro_in_room;
  public boolean is_inside_pyro;
  public boolean is_directly_behind_live_pyro;
  public double closest_pyro_distance2;
  public MapObject closest_target;
  public double closest_target_distance2;
  public double closest_missile_target_distance2;
  public int num_robot_counts;
  public StrafeDirection shot_reaction;
  public boolean is_close_behind_mega;

  public RoomAnalysis() {
    closest_pyro_distance2 = Double.MAX_VALUE;
    closest_target_distance2 = Double.MAX_VALUE;
    closest_missile_target_distance2 = Double.MAX_VALUE;
  }
}


public class ComputerPyroPilot extends PyroPilot {
  private static final Point[] PRIMARY_CANNON_PREFERRED_ENERGIES = getPrimaryCannonPreferredEnergies();

  private static Point[] getPrimaryCannonPreferredEnergies() {
    Point[] energies = new Point[PyroPrimaryCannon.values().length];
    energies[PyroPrimaryCannon.LASER.ordinal()] = new Point(0, 80);
    energies[PyroPrimaryCannon.SPREADFIRE.ordinal()] = new Point(80, 100);
    energies[PyroPrimaryCannon.PLASMA.ordinal()] = new Point(100, 120);
    energies[PyroPrimaryCannon.FUSION.ordinal()] = new Point(120, 200);
    return energies;
  }

  public static final double MAX_TIME_IN_STATE = 60.0;
  public static final double TIME_TURNING_UNTIL_STOP = 5.0;
  public static final double RESPAWN_DELAY = 5.0;
  public static final double SPAWNING_SICKNESS = Entrance.ZUNGGG_TIME - Entrance.TIME_TO_SPAWN;
  public static final double FRIENDLY_FIRE_DIRECTION_EPSILON = MapUtils.PI_OVER_FOUR;
  public static final double SAME_DIRECTION_EPSILON = Math.PI / 36;
  public static final double MIN_DISTANCE_TO_ROBOT = 1.0;
  public static final double PREFERRED_DISTANCE_TO_ROBOT_RANGE = 0.1;
  public static final double CLOSE_ROBOT_DIRECTION_EPSILON = MapUtils.PI_OVER_FOUR;
  public static final double TARGET_ROBOT_WHEN_CLOAKED_PROB = 0.5;
  public static final int BASIC_MISSILE_MIN_SHIELDS = Shot.getDamage(ObjectType.ConcussionMissile);
  public static final double MISSILE_MIN_DISTANCE2 = 4.0;
  public static final double BOMB_DROP_RADIUS = 1.0;
  public static final int SMART_MISSILE_ROBOT_DIVISOR = Shot.getDamage(ObjectType.SmartPlasma);
  public static final int SMART_MISSILE_MIN_ROBOT_COUNTS = SmartMissile.NUM_SMART_PLASMAS;
  public static final int MEGA_MISSILE_MIN_SHIELDS = 48;
  public static final double MEGA_MISSILE_MIN_DISTANCE2 = Math.pow(MegaMissile.SPLASH_DAMAGE_RADIUS, 2);

  private final HashSet<Room> visited;
  private final LinkedList<Scenery> target_sceneries;
  private LinkedList<Entry<RoomSide, RoomConnection>> current_path;
  private PyroPilotState state;
  private double state_time_left;
  private MapObject target_object;
  private PyroTargetType target_type;
  private Powerup target_powerup;
  private double min_distance_to_robot2;
  private double max_distance_to_robot2;
  private double time_turning_to_target;
  private TurnDirection previous_turn_to_target;
  private double respawn_delay_left;
  private double inactive_time_left;

  public ComputerPyroPilot() {
    visited = new HashSet<Room>();
    target_sceneries = new LinkedList<Scenery>();
    current_path = new LinkedList<Entry<RoomSide, RoomConnection>>();
  }

  @Override
  public void startPilot() {
    visitRoom(current_room);
    current_path.clear();
    inactive_time_left = SPAWNING_SICKNESS;
    initState(PyroPilotState.INACTIVE);
  }

  @Override
  public void prepareForRespawn() {
    visited.remove(current_room);
    current_path.clear();
    respawn_delay_left = RESPAWN_DELAY;
    initState(PyroPilotState.INACTIVE);
  }

  @Override
  public void newLevel() {
    visited.clear();
  }

  @Override
  public void handleRespawnDelay(double s_elapsed) {
    respawn_delay_left -= s_elapsed;
  }

  @Override
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
    target_sceneries.clear();
    for (Scenery scenery : room.getSceneries()) {
      if (scenery.getType().equals(ObjectType.RobotGenerator)) {
        target_sceneries.add(scenery);
      }
    }
    if (bound_pyro.getShields() >= 0) {
      if (target_room_info != null && !room.equals(target_room_info.getValue().neighbor)) {
        current_path.clear();
      }
      initState(PyroPilotState.REACT_TO_OBJECT);
    }
  }

  public void initState(PyroPilotState next_state) {
    switch (next_state) {
      case INACTIVE:
        break;
      case MOVE_TO_ROOM_CONNECTION:
        findNextRoom();
        planMoveToRoomConnection(target_room_info.getKey(), bound_object_radius);
        if (bound_pyro.isCannonReloaded() && bound_pyro.isSecondaryCannonReloaded()) {
          selectPrimaryCannon();
        }
        break;
      case MOVE_TO_NEIGHBOR_ROOM:
        planMoveToNeighborRoom(target_room_info.getKey(), bound_object_radius);
        if (bound_pyro.isCannonReloaded() && bound_pyro.isSecondaryCannonReloaded()) {
          selectSecondaryCannon();
        }
        break;
      case REACT_TO_OBJECT:
        target_object = findNextTargetObject();
        if (target_object == null) {
          initState(PyroPilotState.MOVE_TO_ROOM_CONNECTION);
          return;
        }
        if (target_object instanceof Unit) {
          target_unit = (Unit) target_object;
          target_type = PyroTargetType.UNIT;
          double min_distance_to_robot =
                  Math.max(bound_object_diameter + target_object.getRadius(), MIN_DISTANCE_TO_ROBOT);
          min_distance_to_robot2 = Math.pow(min_distance_to_robot, 2);
          max_distance_to_robot2 = Math.pow(min_distance_to_robot + PREFERRED_DISTANCE_TO_ROBOT_RANGE, 2);
          if (target_unit.isCloaked()) {
            initState(PyroPilotState.REACT_TO_CLOAKED_ROBOT);
            return;
          }
        }
        else if (target_object instanceof Powerup) {
          target_powerup = (Powerup) target_object;
          target_type = PyroTargetType.POWERUP;
        }
        previous_turn_to_target = TurnDirection.NONE;
        break;
      case TURN_TO_OBJECT:
        break;
      case REACT_TO_CLOAKED_ROBOT:
        break;
      default:
        throw new DescentMapException("Unexpected PyroPilotState: " + state);
    }
    state = next_state;
    state_time_left = MAX_TIME_IN_STATE;
  }

  @Override
  public PilotAction findNextAction(double s_elapsed) {
    state_time_left -= s_elapsed;
    updateState();

    RoomAnalysis ra = analyzeRoom();
    boolean fire_primary = shouldFirePrimary(ra);
    boolean fire_secondary = shouldFireSecondary(ra);
    boolean drop_bomb = shouldDropBomb(ra);
    MoveDirection move =
            (ra.is_directly_behind_live_pyro || ra.is_close_behind_mega
                    ? MoveDirection.BACKWARD
                    : MoveDirection.FORWARD);
    StrafeDirection strafe = ra.shot_reaction;

    switch (state) {
      case INACTIVE:
        inactive_time_left -= s_elapsed;
        return PilotAction.NO_ACTION;
      case MOVE_TO_ROOM_CONNECTION:
        return new PilotAction(move, strafe, angleToTurnDirection(MapUtils.angleTo(bound_object, target_x,
                target_y)), fire_primary, fire_secondary, drop_bomb);
      case MOVE_TO_NEIGHBOR_ROOM:
        return new PilotAction(move, strafe, angleToTurnDirection(MapUtils.angleTo(bound_object, target_x,
                target_y)), fire_primary, fire_secondary, drop_bomb);
      case REACT_TO_OBJECT:
        if (target_type.equals(PyroTargetType.UNIT) && move.equals(MoveDirection.FORWARD) &&
                !ra.is_inside_pyro) {
          double target_distance = MapUtils.distance2(bound_object, target_object);
          if (target_distance < min_distance_to_robot2) {
            move = MoveDirection.BACKWARD;
          }
          else if (target_distance < max_distance_to_robot2) {
            move = MoveDirection.NONE;
          }
        }
        TurnDirection turn = angleToTurnDirection(MapUtils.angleTo(bound_object, target_object));
        if (!turn.equals(TurnDirection.NONE) && turn.equals(previous_turn_to_target)) {
          time_turning_to_target += s_elapsed;
        }
        else {
          previous_turn_to_target = turn;
          time_turning_to_target = 0.0;
        }
        return new PilotAction(move, strafe, turn, fire_primary, fire_secondary, drop_bomb);
      case TURN_TO_OBJECT:
        return new PilotAction(MoveDirection.NONE, strafe, angleToTurnDirection(MapUtils.angleTo(
                bound_object, target_object)), fire_primary, fire_secondary, false);
      case REACT_TO_CLOAKED_ROBOT:
        if (target_unit.isVisible()) {
          setTargetLocation(target_unit);
        }
        double angle_to_target = MapUtils.angleTo(bound_object, target_x, target_y);
        if (!fire_primary && !target_unit.isVisible() && Math.abs(angle_to_target) < DIRECTION_EPSILON) {
          fire_primary = true;
        }
        return new PilotAction(move, strafe, angleToTurnDirection(angle_to_target), fire_primary,
                fire_secondary, drop_bomb);
      default:
        throw new DescentMapException("Unexpected PyroPilotState: " + state);
    }
  }

  public void updateState() {
    if (state_time_left < 0.0) {
      initState(PyroPilotState.MOVE_TO_ROOM_CONNECTION);
      return;
    }

    switch (state) {
      case INACTIVE:
        if (inactive_time_left < 0.0) {
          initState(PyroPilotState.REACT_TO_OBJECT);
        }
        break;
      case MOVE_TO_ROOM_CONNECTION:
        if (MapUtils.isPointInObject(bound_object, target_x, target_y)) {
          initState(PyroPilotState.MOVE_TO_NEIGHBOR_ROOM);
        }
        break;
      case MOVE_TO_NEIGHBOR_ROOM:
        if (MapUtils.isPointInObject(bound_object, target_x, target_y)) {
          updateCurrentRoom(target_room_info.getValue().neighbor);
          initState(PyroPilotState.REACT_TO_OBJECT);
        }
        break;
      case REACT_TO_OBJECT:
        if (!target_object.isInMap() || !target_object.getRoom().equals(current_room) ||
                ((target_type.equals(PyroTargetType.POWERUP)) && !shouldCollectPowerup(target_powerup))) {
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
      case REACT_TO_CLOAKED_ROBOT:
        if (!target_object.isInMap() ||
                (target_unit.isVisible() && !target_object.getRoom().equals(current_room))) {
          initState(PyroPilotState.REACT_TO_OBJECT);
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
    } while (paths.isEmpty() && !partial_paths.isEmpty());
    if (paths.isEmpty()) {
      // we have already visited all reachable Rooms
      visited.clear();
      visitRoom(current_room);
      findNextPath();
      return;
    }
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

  public RoomAnalysis analyzeRoom() {
    RoomAnalysis ra = new RoomAnalysis();
    analyzePyros(ra);
    analyzeCurrentRoomTargets(ra);
    if (ra.closest_target_distance2 == Double.MAX_VALUE) {
      analyzeNeighborRoomTargets(ra);
    }
    analyzeShots(ra);
    return ra;
  }

  public void analyzePyros(RoomAnalysis ra) {
    for (Pyro pyro : current_room.getPyros()) {
      if (pyro.equals(bound_object) || !pyro.isVisible()) {
        continue;
      }
      ra.is_another_pyro_in_room = true;
      double abs_angle_to_pyro = Math.abs(MapUtils.angleTo(bound_object, pyro));
      if (MapUtils.objectsIntersect(bound_object, pyro, bound_object_diameter)) {
        ra.is_inside_pyro = true;
        if (pyro.getShields() >= 0 && abs_angle_to_pyro < MapUtils.PI_OVER_TWO &&
                Math.abs(pyro.getDirection() - bound_object.getDirection()) < SAME_DIRECTION_EPSILON) {
          ra.is_directly_behind_live_pyro = true;
        }
      }
      if (abs_angle_to_pyro < FRIENDLY_FIRE_DIRECTION_EPSILON) {
        ra.closest_pyro_distance2 =
                Math.min(ra.closest_pyro_distance2, MapUtils.distance2(bound_object, pyro));
      }
    }

    if (ra.closest_pyro_distance2 == Double.MAX_VALUE &&
            (state.equals(PyroPilotState.MOVE_TO_ROOM_CONNECTION) || state.equals(PyroPilotState.MOVE_TO_NEIGHBOR_ROOM))) {
      RoomConnection connection = target_room_info.getValue();
      for (Pyro pyro : connection.neighbor.getPyros()) {
        if (!pyro.isVisible()) {
          continue;
        }
        double abs_angle_to_unit = Math.abs(MapUtils.angleTo(bound_object, pyro));
        if (abs_angle_to_unit < FRIENDLY_FIRE_DIRECTION_EPSILON &&
                MapUtils.canSeeObjectInNeighborRoom(bound_object, pyro, target_room_info.getKey())) {
          ra.closest_pyro_distance2 =
                  Math.min(ra.closest_pyro_distance2, MapUtils.distance2(bound_object, pyro));
        }
      }
    }
  }

  public void analyzeCurrentRoomTargets(RoomAnalysis ra) {
    for (Robot robot : current_room.getRobots()) {
      if (!robot.isVisible()) {
        continue;
      }
      if (!robot.isCloaked()) {
        ra.num_robot_counts += robot.getShields() / SMART_MISSILE_ROBOT_DIVISOR + 1;
      }
      double abs_angle_to_robot = Math.abs(MapUtils.angleTo(bound_object, robot));
      if (abs_angle_to_robot < DIRECTION_EPSILON ||
              (abs_angle_to_robot < CLOSE_ROBOT_DIRECTION_EPSILON && MapUtils.objectsIntersect(bound_object,
                      robot, bound_object_diameter + robot.getRadius()))) {
        double robot_distance2 = MapUtils.distance2(bound_object, robot);
        if (robot_distance2 < ra.closest_target_distance2) {
          ra.closest_target_distance2 = robot_distance2;
          ra.closest_target = robot;
        }
        if ((!robot.isCloaked() || bound_pyro.getSelectedSecondaryCannonType().equals(
                PyroSecondaryCannon.CONCUSSION_MISSILE)) &&
                robot.getShields() >= BASIC_MISSILE_MIN_SHIELDS && robot_distance2 > MISSILE_MIN_DISTANCE2) {
          ra.closest_missile_target_distance2 =
                  Math.min(ra.closest_missile_target_distance2, robot_distance2);
        }
      }
    }
    for (ProximityBomb bomb : current_room.getBombs()) {
      if (Math.abs(MapUtils.angleTo(bound_object, bomb)) < DIRECTION_EPSILON) {
        double bomb_distance2 = MapUtils.distance2(bound_object, bomb);
        if (bomb_distance2 < ra.closest_target_distance2) {
          ra.closest_target_distance2 = bomb_distance2;
          ra.closest_target = bomb;
        }
      }
    }
  }

  public void analyzeNeighborRoomTargets(RoomAnalysis ra) {
    double src_x = bound_object.getX();
    double src_y = bound_object.getY();
    double src_direction = bound_object.getDirection();
    for (Entry<RoomSide, RoomConnection> entry : current_room.getNeighbors().entrySet()) {
      RoomSide neighbor_side = entry.getKey();
      RoomConnection connection = entry.getValue();
      double direction_to_neighbor = RoomSide.directionToRadians(neighbor_side);
      double angle_from_neighbor_to_self = MapUtils.angleTo(direction_to_neighbor, src_direction);
      Point2D.Double angles_to_connection =
              MapUtils.anglesToNeighborConnectionPoints(bound_object, neighbor_side);
      if (angles_to_connection.x < angle_from_neighbor_to_self &&
              angle_from_neighbor_to_self < angles_to_connection.y) {
        for (Robot robot : connection.neighbor.getRobots()) {
          if (!robot.isVisible()) {
            continue;
          }
          double abs_angle_to_unit = Math.abs(MapUtils.angleTo(bound_object, robot));
          double neighbor_angle_to_unit =
                  MapUtils.angleTo(direction_to_neighbor, robot.getX() - src_x, robot.getY() - src_y);
          if (abs_angle_to_unit < DIRECTION_EPSILON && angles_to_connection.x < neighbor_angle_to_unit &&
                  neighbor_angle_to_unit < angles_to_connection.y) {
            double robot_distance2 = MapUtils.distance2(bound_object, robot);
            if (robot_distance2 < ra.closest_target_distance2) {
              ra.closest_target_distance2 = robot_distance2;
              ra.closest_target = robot;
            }
            if ((!robot.isCloaked() || bound_pyro.getSelectedSecondaryCannonType().equals(
                    PyroSecondaryCannon.CONCUSSION_MISSILE)) &&
                    robot.getShields() >= BASIC_MISSILE_MIN_SHIELDS &&
                    robot_distance2 > MISSILE_MIN_DISTANCE2) {
              ra.closest_missile_target_distance2 =
                      Math.min(ra.closest_missile_target_distance2, robot_distance2);
            }
          }
        }
        for (ProximityBomb bomb : connection.neighbor.getBombs()) {
          double abs_angle_to_unit = Math.abs(MapUtils.angleTo(bound_object, bomb));
          double neighbor_angle_to_bomb =
                  MapUtils.angleTo(direction_to_neighbor, bomb.getX() - src_x, bomb.getY() - src_y);
          if (abs_angle_to_unit < DIRECTION_EPSILON && angles_to_connection.x < neighbor_angle_to_bomb &&
                  neighbor_angle_to_bomb < angles_to_connection.y) {
            double bomb_distance2 = MapUtils.distance2(bound_object, bomb);
            if (bomb_distance2 < ra.closest_target_distance2) {
              ra.closest_target_distance2 = bomb_distance2;
              ra.closest_target = bomb;
            }
          }
        }
        break;
      }
    }
  }

  public void analyzeShots(RoomAnalysis ra) {
    double closest_shot_distance2 = Double.MAX_VALUE;
    for (Shot shot : current_room.getShots()) {
      double shot_distance2 = MapUtils.distance2(bound_object, shot);
      if (shot.getType().equals(ObjectType.MegaMissile) &&
              MapUtils.distance2(bound_object, shot) < MEGA_MISSILE_MIN_DISTANCE2 &&
              // Is the Pyro facing the MegaMissile?
              Math.abs(MapUtils.angleTo(bound_object, shot)) < MapUtils.PI_OVER_TWO &&
              // Is the MegaMissile facing away from the Pyro?
              Math.abs(MapUtils.angleTo(shot, bound_object)) > MapUtils.PI_OVER_TWO) {
        ra.is_close_behind_mega = true;
      }
      if (shot.getSource().equals(bound_object) || shot_distance2 > closest_shot_distance2) {
        continue;
      }
      StrafeDirection strafe = findReactionToShot(shot);
      if (!strafe.equals(StrafeDirection.NONE)) {
        ra.shot_reaction = strafe;
        closest_shot_distance2 = MapUtils.distance2(bound_object, shot);
      }
    }
  }

  public boolean shouldFirePrimary(RoomAnalysis ra) {
    return !ra.is_inside_pyro && ra.closest_target_distance2 < ra.closest_pyro_distance2;
  }

  public boolean shouldFireSecondary(RoomAnalysis ra) {
    if (ra.is_inside_pyro) {
      return false;
    }

    switch (bound_pyro.getSelectedSecondaryCannonType()) {
      case CONCUSSION_MISSILE:
        // fall through
      case HOMING_MISSILE:
        return ra.closest_missile_target_distance2 < ra.closest_pyro_distance2 &&
                ra.closest_target_distance2 > MISSILE_MIN_DISTANCE2;
      case PROXIMITY_BOMB:
        return false;
      case SMART_MISSILE:
        return ra.closest_pyro_distance2 == Double.MAX_VALUE &&
                ra.closest_target_distance2 == Double.MAX_VALUE &&
                ra.num_robot_counts >= SMART_MISSILE_MIN_ROBOT_COUNTS;
      case MEGA_MISSILE:
        if (ra.closest_target == null) {
          return false;
        }
        for (Pyro pyro : ra.closest_target.getRoom().getPyros()) {
          if (MapUtils.distance2(pyro, ra.closest_target) < MEGA_MISSILE_MIN_DISTANCE2) {
            return false;
          }
        }
        return ra.closest_missile_target_distance2 < ra.closest_pyro_distance2 &&
                ra.closest_target_distance2 > MEGA_MISSILE_MIN_DISTANCE2 &&
                ra.closest_target instanceof Robot &&
                ((Robot) ra.closest_target).getShields() >= MEGA_MISSILE_MIN_SHIELDS;
      default:
        throw new DescentMapException("Unexpected PyroSecondaryCannon: " +
                bound_pyro.getSelectedSecondaryCannonType());
    }
  }

  public boolean shouldDropBomb(RoomAnalysis ra) {
    if (ra.is_another_pyro_in_room) {
      return false;
    }
    for (Scenery scenery : target_sceneries) {
      if (MapUtils.objectsIntersect(bound_object, scenery, BOMB_DROP_RADIUS)) {
        return true;
      }
    }
    return false;
  }

  public MapObject findNextTargetObject() {
    for (Robot robot : current_room.getRobots()) {
      if (robot.isVisible() && shouldTargetObject(robot) &&
              (!bound_pyro.isCloaked() || Math.random() < TARGET_ROBOT_WHEN_CLOAKED_PROB)) {
        return robot;
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
        return bound_pyro.getShields() + Shield.SHIELD_AMOUNT <= Pyro.MAX_SHIELDS;
      case Energy:
        return bound_pyro.getEnergy() + Energy.ENERGY_AMOUNT <= Pyro.MAX_ENERGY;
      case Cloak:
        return !bound_pyro.isCloaked();
      case QuadLasers:
        return !bound_pyro.hasQuadLasers();
      case LaserCannonPowerup:
        return bound_pyro.getLaserLevel() < LaserCannon.MAX_LEVEL;
      case SpreadfireCannonPowerup:
        return !bound_pyro.hasPrimaryCannon(PyroPrimaryCannon.SPREADFIRE);
      case PlasmaCannonPowerup:
        return !bound_pyro.hasPrimaryCannon(PyroPrimaryCannon.PLASMA);
      case FusionCannonPowerup:
        return !bound_pyro.hasPrimaryCannon(PyroPrimaryCannon.FUSION);
      case ConcussionMissilePowerup:
        return bound_pyro.getSecondaryAmmo(PyroSecondaryCannon.CONCUSSION_MISSILE) < Pyro.getMaxSecondaryAmmo(PyroSecondaryCannon.CONCUSSION_MISSILE);
      case ConcussionPack:
        return bound_pyro.getSecondaryAmmo(PyroSecondaryCannon.CONCUSSION_MISSILE) +
                ConcussionPack.NUM_MISSILES <= Pyro.getMaxSecondaryAmmo(PyroSecondaryCannon.CONCUSSION_MISSILE);
      case HomingMissilePowerup:
        return bound_pyro.getSecondaryAmmo(PyroSecondaryCannon.HOMING_MISSILE) < Pyro.getMaxSecondaryAmmo(PyroSecondaryCannon.HOMING_MISSILE);
      case HomingPack:
        return bound_pyro.getSecondaryAmmo(PyroSecondaryCannon.HOMING_MISSILE) + HomingPack.NUM_MISSILES <= Pyro.getMaxSecondaryAmmo(PyroSecondaryCannon.HOMING_MISSILE);
      case ProximityPack:
        return bound_pyro.getSecondaryAmmo(PyroSecondaryCannon.PROXIMITY_BOMB) + ProximityPack.NUM_BOMBS <= Pyro.getMaxSecondaryAmmo(PyroSecondaryCannon.PROXIMITY_BOMB);
      case SmartMissilePowerup:
        return bound_pyro.getSecondaryAmmo(PyroSecondaryCannon.SMART_MISSILE) < Pyro.getMaxSecondaryAmmo(PyroSecondaryCannon.SMART_MISSILE);
      case MegaMissilePowerup:
        return bound_pyro.getSecondaryAmmo(PyroSecondaryCannon.MEGA_MISSILE) < Pyro.getMaxSecondaryAmmo(PyroSecondaryCannon.MEGA_MISSILE);
      default:
        throw new DescentMapException("Unexpected Powerup: " + powerup);
    }
  }

  public void selectPrimaryCannon() {
    double energy = bound_pyro.getEnergy();
    PyroPrimaryCannon selected_primary_cannon_type = bound_pyro.getSelectedPrimaryCannonType();
    Point preferred_energy = PRIMARY_CANNON_PREFERRED_ENERGIES[selected_primary_cannon_type.ordinal()];
    if (preferred_energy.x <= energy && energy <= preferred_energy.y) {
      return;
    }
    PyroPrimaryCannon preferred_cannon_type = null;
    for (PyroPrimaryCannon cannon_type : PyroPrimaryCannon.values()) {
      if (!bound_pyro.hasPrimaryCannon(cannon_type)) {
        continue;
      }
      preferred_cannon_type = cannon_type;
      preferred_energy = PRIMARY_CANNON_PREFERRED_ENERGIES[cannon_type.ordinal()];
      if (energy <= preferred_energy.y) {
        break;
      }
    }
    bound_pyro.switchPrimaryCannon(preferred_cannon_type, false);
  }

  public void selectSecondaryCannon() {
    Room target_room = target_room_info.getValue().neighbor;
    if (bound_pyro.getSecondaryAmmo(PyroSecondaryCannon.MEGA_MISSILE) > 0) {
      for (Robot robot : target_room.getRobots()) {
        if (robot.getShields() >= MEGA_MISSILE_MIN_SHIELDS) {
          bound_pyro.switchSecondaryCannon(PyroSecondaryCannon.MEGA_MISSILE, false);
          return;
        }
      }
    }
    if (bound_pyro.getSecondaryAmmo(PyroSecondaryCannon.SMART_MISSILE) > 0) {
      int num_robot_counts = 0;
      for (Robot robot : target_room.getRobots()) {
        if (!robot.isCloaked()) {
          num_robot_counts += robot.getShields() / SMART_MISSILE_ROBOT_DIVISOR + 1;
        }
      }
      if (num_robot_counts >= SMART_MISSILE_MIN_ROBOT_COUNTS) {
        bound_pyro.switchSecondaryCannon(PyroSecondaryCannon.SMART_MISSILE, false);
        return;
      }
    }
    if (!bound_pyro.switchSecondaryCannon(PyroSecondaryCannon.HOMING_MISSILE, false)) {
      bound_pyro.switchSecondaryCannon(PyroSecondaryCannon.CONCUSSION_MISSILE, false);
    }
  }
}
