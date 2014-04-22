package pilot;

import java.awt.geom.Point2D;

import util.MapUtils;

public class BomberPilot extends RobotPilot {
  public static final double MAX_REACTION_TARGET_TIME = 5.0;

  private double reaction_target_time_left;

  @Override
  public void initReactToPyroState() {
    super.initReactToPyroState();
    findRandomTargetLocation();
  }

  public void findRandomTargetLocation() {
    Point2D.Double target_location =
            MapUtils.randomInternalPoint(current_room.getNWCorner(), current_room.getSECorner(),
                    bound_object_radius);
    setTargetLocation(target_location.x, target_location.y);
    reaction_target_time_left = MAX_REACTION_TARGET_TIME;
  }

  @Override
  public PilotAction findReactToPyroAction(StrafeDirection strafe) {
    return new PilotAction(MoveDirection.FORWARD, angleToTurnDirection(MapUtils.angleTo(bound_object,
            target_x, target_y)), true);
  }

  @Override
  public PilotAction findReactToCloakedPyroAction(StrafeDirection strafe) {
    return findReactToPyroAction(strafe);
  }

  @Override
  public void updateReactToPyroState(double s_elapsed) {
    if ((Math.abs(target_x - bound_object.getX()) < bound_object_radius && Math.abs(target_y -
            bound_object.getY()) < bound_object_radius) ||
            reaction_target_time_left < 0.0) {
      findRandomTargetLocation();
      return;
    }
    super.updateReactToPyroState(s_elapsed);
    reaction_target_time_left -= s_elapsed;
  }

  @Override
  public void updateReactToCloakedPyroState(double s_elapsed) {
    if ((Math.abs(target_x - bound_object.getX()) < bound_object_radius && Math.abs(target_y -
            bound_object.getY()) < bound_object_radius) ||
            reaction_target_time_left < 0.0) {
      findRandomTargetLocation();
      return;
    }
    super.updateReactToCloakedPyroState(s_elapsed);
    reaction_target_time_left -= s_elapsed;
  }
}
