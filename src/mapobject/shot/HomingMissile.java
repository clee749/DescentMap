package mapobject.shot;

import mapobject.MapObject;
import pilot.HomingPilot;
import structure.Room;
import util.MapUtils;

import common.ObjectType;

public class HomingMissile extends Missile {
  public static final double MAX_ANGLE_TO_TARGET = MapUtils.PI_OVER_FOUR;

  public HomingMissile(MapObject source, int damage, Room room, double x_loc, double y_loc, double direction) {
    super(new HomingPilot(source, MAX_ANGLE_TO_TARGET), source, damage, room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.HomingMissile;
  }
}
