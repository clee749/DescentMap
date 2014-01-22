package mapobject.unit.robot;

import mapobject.MapObject;
import structure.Room;
import cannon.LaserCannon;

import common.Constants;
import common.ObjectType;

public class BabySpider extends Robot {
  public BabySpider(Room room, double x_loc, double y_loc, double direction) {
    super(Constants.getRadius(ObjectType.BabySpider), new LaserCannon(
            Constants.getDamage(ObjectType.LaserShot), 1), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.BabySpider;
  }

  @Override
  public MapObject releasePowerups() {
    return null;
  }
}
