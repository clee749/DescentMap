package mapobject.unit.robot;

import mapobject.MapObject;
import mapobject.shot.Shot;
import mapobject.unit.Unit;
import structure.LockedDoor;
import structure.Room;
import cannon.MegaMissileCannon;

import common.ObjectType;

public class FinalBoss extends BossRobot {
  public FinalBoss(LockedDoor exit_door, Room room, double x_loc, double y_loc, double direction) {
    super(Unit.getRadius(ObjectType.FinalBoss), new MegaMissileCannon(Shot.getDamage(ObjectType.MegaMissile),
            false), exit_door, room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.FinalBoss;
  }

  @Override
  public MapObject releasePowerups() {
    return null;
  }
}
