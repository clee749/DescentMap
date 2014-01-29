package mapobject.unit.robot;

import mapobject.MapObject;
import structure.Room;
import util.PowerupFactory;
import cannon.LaserCannon;

import common.Constants;
import common.ObjectType;

public class LightHulk extends Robot {
  public LightHulk(Room room, double x_loc, double y_loc, double direction) {
    super(Constants.getRadius(ObjectType.LightHulk), new LaserCannon(
            Constants.getDamage(ObjectType.LaserShot), 4), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.LightHulk;
  }

  @Override
  public MapObject releasePowerups() {
    double rand = Math.random();
    if (rand < 0.1) {
      return PowerupFactory.newPowerup(ObjectType.Shield, room, x_loc, y_loc);
    }
    if (rand < 0.5) {
      return PowerupFactory.newPowerup(ObjectType.LaserCannonPowerup, room, x_loc, y_loc);
    }
    return null;
  }
}
