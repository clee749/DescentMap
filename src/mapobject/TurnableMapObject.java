package mapobject;

import pilot.Pilot;
import pilot.TurnDirection;
import structure.Room;

import common.Constants;
import common.MapUtils;

public abstract class TurnableMapObject extends MovableMapObject {
  protected final double turn_speed;

  public TurnableMapObject(Pilot pilot, Room room, double x_loc, double y_loc, double direction) {
    super(pilot, room, x_loc, y_loc, direction);
    turn_speed = Constants.getTurnSpeed(type);
  }

  @Override
  public void computeNextLocation(double s_elapsed) {
    super.computeNextLocation(s_elapsed);
    if (next_movement == null) {
      return;
    }

    TurnDirection turn = next_movement.turn;
    if (turn != null) {
      switch (turn) {
        case COUNTER_CLOCKWISE:
          direction = MapUtils.normalizeAngle(direction + turn_speed * s_elapsed);
          break;
        case CLOCKWISE:
          direction = MapUtils.normalizeAngle(direction - turn_speed * s_elapsed);
          break;
      }
    }
  }
}
