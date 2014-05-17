package mapobject.unit.robot;

import mapobject.MapObject;
import mapobject.shot.Shot;
import mapobject.unit.Unit;
import structure.LockedDoor;
import structure.Room;
import cannon.SmartMissileCannon;

import common.ObjectType;

public class BigGuy extends BossRobot {
  public BigGuy(LockedDoor exit_door, Room room, double x_loc, double y_loc, double direction) {
    super(Unit.getRadius(ObjectType.BigGuy), new SmartMissileCannon(Shot.getDamage(ObjectType.SmartMissile),
            Shot.getDamage(ObjectType.SmartPlasma), false), exit_door, room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.BigGuy;
  }

  @Override
  public MapObject releasePowerups() {
    return null;
  }
}
