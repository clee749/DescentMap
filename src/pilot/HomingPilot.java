package pilot;

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
  public PilotAction findNextAction(double s_elapsed) {
    if (target_object == null || !target_object.isInMap()) {
      updateTarget();
      return PilotAction.MOVE_FORWARD;
    }
    double angle_to_target = MapUtils.angleTo(bound_object, target_object);
    if (Math.abs(angle_to_target) > max_angle_to_target) {
      target_object = null;
    }
    TurnDirection turn = angleToTurnDirection(angle_to_target);
    return new PilotAction(MoveDirection.FORWARD, turn);
  }

  public void updateTarget() {
    target_object = findNewTargetInRoom(current_room, null);
    if (target_object == null) {
      for (RoomSide direction : RoomSide.values()) {
        RoomConnection connection = current_room.getConnectionInDirection(direction);
        if (connection != null && canSeeIntoRoom(direction, connection)) {
          target_object = findNewTargetInRoom(connection.neighbor, direction);
          break;
        }
      }
    }
  }

  public boolean canSeeIntoRoom(RoomSide neighbor_side, RoomConnection connection) {
    double src_x = bound_object.getX();
    double src_y = bound_object.getY();
    double src_direction = bound_object.getDirection();
    double angle_to_connection_min;
    double angle_to_connection_max;
    switch (neighbor_side) {
      case NORTH:
        double dy = current_room.getNWCorner().y - src_y;
        angle_to_connection_min = MapUtils.angleTo(src_direction, connection.min - src_x, dy);
        angle_to_connection_max = MapUtils.angleTo(src_direction, connection.max - src_x, dy);
        break;
      case SOUTH:
        dy = current_room.getSECorner().y - src_y;
        angle_to_connection_min = MapUtils.angleTo(src_direction, connection.min - src_x, dy);
        angle_to_connection_max = MapUtils.angleTo(src_direction, connection.max - src_x, dy);
        break;
      case WEST:
        double dx = current_room.getNWCorner().x - src_x;
        angle_to_connection_min = MapUtils.angleTo(src_direction, dx, connection.min - src_y);
        angle_to_connection_max = MapUtils.angleTo(src_direction, dx, connection.max - src_y);
        break;
      case EAST:
        dx = current_room.getSECorner().x - src_x;
        angle_to_connection_min = MapUtils.angleTo(src_direction, dx, connection.min - src_y);
        angle_to_connection_max = MapUtils.angleTo(src_direction, dx, connection.max - src_y);
        break;
      default:
        throw new DescentMapException("Unexpected RoomSide: " + neighbor_side);
    }
    return Math.abs(angle_to_connection_min) < max_angle_to_target ||
            Math.abs(angle_to_connection_max) < max_angle_to_target;
  }

  public Unit findNewTargetInRoom(Room room, RoomSide neighbor_side) {
    Unit new_target = null;
    double smallest_angle_to_target = max_angle_to_target;
    switch (target_type) {
      case PYRO:
        for (Pyro pyro : room.getPyros()) {
          double abs_angle_to_target = Math.abs(MapUtils.angleTo(bound_object, pyro));
          if (abs_angle_to_target < smallest_angle_to_target &&
                  (neighbor_side == null || MapUtils.canSeeObjectInNeighborRoom(bound_object, pyro,
                          neighbor_side))) {
            new_target = pyro;
            smallest_angle_to_target = abs_angle_to_target;
          }
        }
        break;
      case ROBOT:
        for (Unit unit : room.getRobots()) {
          double abs_angle_to_target = Math.abs(MapUtils.angleTo(bound_object, unit));
          if (abs_angle_to_target < smallest_angle_to_target &&
                  (neighbor_side == null || MapUtils.canSeeObjectInNeighborRoom(bound_object, unit,
                          neighbor_side))) {
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
