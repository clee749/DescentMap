package pilot;

import common.Constants;

public enum TurnDirection {
  NONE,
  COUNTER_CLOCKWISE,
  CLOCKWISE;

  public static TurnDirection angleToTurnDirection(double angle) {
    double abs_angle = Math.abs(angle);
    if (abs_angle < Constants.PILOT_DIRECTION_EPSILON) {
      return TurnDirection.NONE;
    }
    return (angle < 0 ? TurnDirection.COUNTER_CLOCKWISE : TurnDirection.CLOCKWISE);
  }
}
