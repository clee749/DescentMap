package pilot;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import mapobject.MovableObject;
import mapobject.unit.Pyro;
import mapobject.unit.Unit;
import mapobject.unit.robot.BossRobot;
import structure.DescentMap;
import structure.Room;
import util.MapUtils;

import common.ObjectType;

public class BossRobotPilot extends RobotPilot {
  protected BossRobot bound_robot;
  protected double min_pyro_teleport_distance2;

  @Override
  public void bindToObject(MovableObject object) {
    super.bindToObject(object);
    bound_robot = (BossRobot) object;
    min_pyro_teleport_distance2 = Math.pow(bound_object_radius + Unit.getRadius(ObjectType.Pyro), 2);
  }

  @Override
  public void initNewRoomState() {
    initState(RobotPilotState.INACTIVE);
  }

  @Override
  public StrafeDirection reactToShots() {
    return StrafeDirection.NONE;
  }

  @Override
  public PilotAction findReactToTargetAction(StrafeDirection strafe, double target_x, double target_y) {
    bound_robot.startTeleportSequence();
    double angle_to_target = MapUtils.angleTo(bound_object, target_x, target_y);
    return new PilotAction(strafe, angleToTurnDirection(angle_to_target),
            Math.abs(angle_to_target) < DIRECTION_EPSILON);
  }

  @Override
  public void updateInactiveState(double s_elapsed) {

  }

  public TeleportLocation findTeleportLocation(DescentMap map) {
    ArrayList<Room> possible_rooms = new ArrayList<Room>();
    for (Room room : map.getAllRooms()) {
      if (room.getHeight() > bound_object_diameter && room.getWidth() > bound_object_diameter) {
        possible_rooms.add(room);
      }
    }
    Room dest_room;
    Point2D.Double location;
    do {
      dest_room = possible_rooms.remove((int) (Math.random() * possible_rooms.size()));
      location =
              MapUtils.randomInternalPoint(dest_room.getNWCorner(), dest_room.getSECorner(),
                      bound_object_radius);
      for (Pyro pyro : dest_room.getPyros()) {
        if (MapUtils.distance2(pyro, location.x, location.y) < min_pyro_teleport_distance2) {
          dest_room = null;
          break;
        }
      }
    } while (dest_room == null && !possible_rooms.isEmpty());
    return (dest_room != null ? new TeleportLocation(dest_room, location.x, location.y) : null);
  }
}
