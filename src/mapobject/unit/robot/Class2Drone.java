package mapobject.unit.robot;

import mapobject.MapObject;
import mapobject.powerup.Shield;
import structure.Room;
import util.MapUtils;
import cannon.LaserCannon;

import common.Constants;
import common.ObjectType;

public class Class2Drone extends Robot {

  public Class2Drone(Room room, double x_loc, double y_loc, double direction) {
    super(Constants.getRadius(ObjectType.Class2Drone), new LaserCannon(
            Constants.getDamage(ObjectType.LaserShot), 2), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Class2Drone;
  }

  @Override
  public MapObject releasePowerups() {
    if (Math.random() < 0.1) {
      return new Shield(room, x_loc, y_loc, Math.random() * MapUtils.TWO_PI, Math.random() *
              Constants.POWERUP_MAX_SPEED);
    }
    return null;
  }
}
