package mapobject.shot;

import mapobject.MapObject;
import mapobject.unit.Unit;
import pilot.HomingPilot;
import structure.Room;
import util.MapUtils;

import common.ObjectType;

public class SmartPlasma extends ExplosiveShot {
  public static final double MAX_ANGLE_TO_TARGET = MapUtils.PI_OVER_FOUR;

  public SmartPlasma(MapObject source, int damage, Room room, double x_loc, double y_loc, double direction) {
    super(new HomingPilot(source, Math.PI), source, damage, room, x_loc, y_loc, direction);
    Unit target_unit = ((HomingPilot) pilot).getTargetUnit();
    if (target_unit != null) {
      this.direction = MapUtils.absoluteAngleTo(x_loc, y_loc, target_unit.getX(), target_unit.getY());
    }
    ((HomingPilot) pilot).setMaxAngleToTarget(MAX_ANGLE_TO_TARGET);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.SmartPlasma;
  }
}
