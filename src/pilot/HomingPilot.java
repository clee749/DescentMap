package pilot;

import java.awt.geom.Point2D;
import java.util.Map.Entry;

import mapobject.MapObject;
import mapobject.MovableObject;
import mapobject.unit.Pyro;
import mapobject.unit.Unit;
import mapobject.unit.robot.Robot;
import structure.Room;
import structure.RoomConnection;
import util.MapUtils;

import common.DescentMapException;
import common.ObjectType;
import common.RoomSide;

enum HomingTargetType {
  PYRO,
  ROBOT;
}


public class HomingPilot extends Pilot {
  private final HomingTargetType target_type;
  private double max_angle_to_target;
  private Unit target_unit;
  private Room target_unit_room;
  private Entry<RoomSide, RoomConnection> target_unit_room_info;

  public HomingPilot(MapObject source, double max_angle_to_target) {
    target_type = (source.getType().equals(ObjectType.Pyro) ? HomingTargetType.ROBOT : HomingTargetType.PYRO);
    this.max_angle_to_target = max_angle_to_target;
  }

  public Unit getTargetUnit() {
    return target_unit;
  }

  public void setMaxAngleToTarget(double max_angle_to_target) {
    this.max_angle_to_target = max_angle_to_target;
  }

  @Override
  public void bindToObject(MovableObject object) {
    super.bindToObject(object);
    updateTarget();
  }

  @Override
  public void updateCurrentRoom(Room room) {
    super.updateCurrentRoom(room);
    updateTarget();
  }

  @Override
  public PilotAction findNextAction(double s_elapsed) {
    if (target_unit == null || !target_unit.isInMap() || target_unit.isCloaked() ||
            !target_unit.getRoom().equals(target_unit_room)) {
      updateTarget();
      return PilotAction.MOVE_FORWARD;
    }
    double angle_to_target = MapUtils.angleTo(bound_object, target_unit);
    if (Math.abs(angle_to_target) > max_angle_to_target ||
            (!target_unit_room.equals(current_room) && !MapUtils.canSeeObjectInNeighborRoom(bound_object,
                    target_unit, target_unit_room_info.getKey()))) {
      updateTarget();
      return PilotAction.MOVE_FORWARD;
    }
    TurnDirection turn = angleToTurnDirection(angle_to_target);
    return new PilotAction(MoveDirection.FORWARD, turn);
  }

  public void updateTarget() {
    target_unit = findNewTargetInCurrentRoom();
    if (target_unit != null) {
      target_unit_room = current_room;
      return;
    }
    double src_direction = bound_object.getDirection();
    for (Entry<RoomSide, RoomConnection> entry : current_room.getNeighbors().entrySet()) {
      RoomSide neighbor_side = entry.getKey();
      RoomConnection connection = entry.getValue();
      double direction_to_neighbor = RoomSide.directionToRadians(neighbor_side);
      double angle_from_neighbor_to_self = MapUtils.angleTo(direction_to_neighbor, src_direction);
      Point2D.Double angles_to_connection =
              MapUtils.anglesToNeighborConnectionPoints(bound_object, neighbor_side);
      if (angles_to_connection.x - max_angle_to_target < angle_from_neighbor_to_self &&
              angle_from_neighbor_to_self < angles_to_connection.y + max_angle_to_target) {
        target_unit = findNewTargetInNeighborRoom(neighbor_side, angles_to_connection);
        if (target_unit != null) {
          target_unit_room = connection.neighbor;
          target_unit_room_info = entry;
          break;
        }
      }
    }
  }

  public Unit findNewTargetInCurrentRoom() {
    Unit new_target = null;
    double smallest_angle_to_target = max_angle_to_target;
    switch (target_type) {
      case PYRO:
        for (Pyro pyro : current_room.getPyros()) {
          if (pyro.isCloaked()) {
            continue;
          }
          double abs_angle_to_target = Math.abs(MapUtils.angleTo(bound_object, pyro));
          if (abs_angle_to_target < smallest_angle_to_target) {
            new_target = pyro;
            smallest_angle_to_target = abs_angle_to_target;
          }
        }
        break;
      case ROBOT:
        for (Robot robot : current_room.getRobots()) {
          if (robot.isCloaked()) {
            continue;
          }
          double abs_angle_to_target = Math.abs(MapUtils.angleTo(bound_object, robot));
          if (abs_angle_to_target < smallest_angle_to_target) {
            new_target = robot;
            smallest_angle_to_target = abs_angle_to_target;
          }
        }
        break;
      default:
        throw new DescentMapException("Unexpected HomingTargetType: " + target_type);
    }
    return new_target;
  }

  public Unit findNewTargetInNeighborRoom(RoomSide neighbor_side, Point2D.Double angles_to_connection) {
    Unit new_target = null;
    double smallest_angle_to_target = max_angle_to_target;
    switch (target_type) {
      case PYRO:
        for (Pyro pyro : current_room.getNeighborInDirection(neighbor_side).getPyros()) {
          if (pyro.isCloaked()) {
            continue;
          }
          double abs_angle_to_target = Math.abs(MapUtils.angleTo(bound_object, pyro));
          if (abs_angle_to_target < smallest_angle_to_target &&
                  (neighbor_side == null || MapUtils.canSeeObjectInNeighborRoom(bound_object, pyro,
                          neighbor_side, angles_to_connection))) {
            new_target = pyro;
            smallest_angle_to_target = abs_angle_to_target;
          }
        }
        break;
      case ROBOT:
        for (Robot robot : current_room.getNeighborInDirection(neighbor_side).getRobots()) {
          if (robot.isCloaked()) {
            continue;
          }
          double abs_angle_to_target = Math.abs(MapUtils.angleTo(bound_object, robot));
          if (abs_angle_to_target < smallest_angle_to_target &&
                  (neighbor_side == null || MapUtils.canSeeObjectInNeighborRoom(bound_object, robot,
                          neighbor_side, angles_to_connection))) {
            new_target = robot;
            smallest_angle_to_target = abs_angle_to_target;
          }
        }
        break;
      default:
        throw new DescentMapException("Unexpected HomingTargetType: " + target_type);
    }
    return new_target;
  }
}
