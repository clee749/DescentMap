package pilot;

import java.awt.geom.Point2D;
import java.util.Map.Entry;

import mapobject.MovableObject;
import mapobject.unit.Pyro;
import mapobject.unit.Unit;
import structure.Room;
import structure.RoomConnection;
import util.MapUtils;

import common.DescentMapException;
import common.RoomSide;

public class HomingPilot extends Pilot {
  private final HomingTargetType target_type;
  private final double max_angle_to_target;

  public HomingPilot(HomingTargetType target_type, double max_angle_to_target) {
    this.target_type = target_type;
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
    if (target_object == null || !target_object.isInMap() ||
            !target_object.getRoom().equals(target_object_room)) {
      updateTarget();
      return PilotAction.MOVE_FORWARD;
    }
    double angle_to_target = MapUtils.angleTo(bound_object, target_object);
    if (Math.abs(angle_to_target) > max_angle_to_target ||
            (!target_object_room.equals(current_room) && !MapUtils.canSeeObjectInNeighborRoom(bound_object,
                    target_object, target_object_room_info.getKey()))) {
      updateTarget();
      return PilotAction.MOVE_FORWARD;
    }
    TurnDirection turn = angleToTurnDirection(angle_to_target);
    return new PilotAction(MoveDirection.FORWARD, turn);
  }

  public void updateTarget() {
    target_object = findNewTargetInCurrentRoom();
    if (target_object != null) {
      target_object_room = current_room;
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
        target_object = findNewTargetInNeighborRoom(neighbor_side, angles_to_connection);
        if (target_object != null) {
          target_object_room = connection.neighbor;
          target_object_room_info = entry;
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
          double abs_angle_to_target = Math.abs(MapUtils.angleTo(bound_object, pyro));
          if (abs_angle_to_target < smallest_angle_to_target) {
            new_target = pyro;
            smallest_angle_to_target = abs_angle_to_target;
          }
        }
        break;
      case ROBOT:
        for (Unit unit : current_room.getRobots()) {
          double abs_angle_to_target = Math.abs(MapUtils.angleTo(bound_object, unit));
          if (abs_angle_to_target < smallest_angle_to_target) {
            new_target = unit;
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
        for (Unit unit : current_room.getNeighborInDirection(neighbor_side).getRobots()) {
          double abs_angle_to_target = Math.abs(MapUtils.angleTo(bound_object, unit));
          if (abs_angle_to_target < smallest_angle_to_target &&
                  (neighbor_side == null || MapUtils.canSeeObjectInNeighborRoom(bound_object, unit,
                          neighbor_side, angles_to_connection))) {
            new_target = unit;
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
