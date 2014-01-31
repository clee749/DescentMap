package mapobject.unit.robot;

import mapobject.MapObject;
import mapobject.shot.Shot;
import mapobject.unit.Unit;
import structure.Room;
import cannon.LaserCannon;

import common.ObjectType;

public class BabySpider extends Robot {
  public BabySpider(Room room, double x_loc, double y_loc, double direction) {
    super(Unit.getRadius(ObjectType.BabySpider), new LaserCannon(Shot.getDamage(ObjectType.LaserShot), 1),
            room, x_loc, y_loc, direction);
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
