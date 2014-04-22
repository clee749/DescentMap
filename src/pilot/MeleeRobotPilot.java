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
    return new PilotAction(MoveDirection.FORWARD, strafe, angleToTurnDirection(MapUtils.angleTo(bound_object,
            target_x, target_y)));
  }

  @Override
  public void updateReactToCloakedPyroState(double s_elapsed) {
    if (MapUtils.isPointInObject(bound_object, target_x, target_y)) {
      initState(RobotPilotState.TURN_TO_ROOM_INTERIOR);
      return;
    }
    super.updateReactToCloakedPyroState(s_elapsed);
  }
}
