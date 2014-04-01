package mapobject.unit.robot;

import mapobject.MapObject;
import mapobject.shot.Shot;
import mapobject.unit.Unit;
import structure.Room;
import util.PowerupFactory;
import cannon.LaserCannon;

import common.ObjectType;

public class PlatformLaser extends Robot {
  public PlatformLaser(Room room, double x_loc, double y_loc, double direction) {
    super(Unit.getRadius(ObjectType.PlatformLaser), new LaserCannon(Shot.getDamage(ObjectType.LaserShot),
            false, 3), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.PlatformLaser;
  }

  @Override
  public MapObject releasePowerups() {
    if (Math.random() < 0.1) {
      return PowerupFactory.newReleasedPowerup(ObjectType.Shield, room, x_loc, y_loc);
    }
    return null;
  }
}
