package pilot;

import util.MapUtils;

public class MeleeRobotPilot extends RobotPilot {
  @Override
  public PilotAction findReactToPyroAction(StrafeDirection strafe) {
    return new PilotAction(MoveDirection.FORWARD, strafe, angleToTurnDirection(MapUtils.angleTo(bound_object,
            target_unit)));
  }

  @Override
  public PilotAction findReactToCloakedPyroAction(StrafeDirection strafe) {
    return new PilotAction(MoveDirection.FORWARD, strafe, angleToTurnDirection(MapUtils.angleTo(
            bound_object.getDirection(), target_x - bound_object.getX(), target_y - bound_object.getY())));
  }
}