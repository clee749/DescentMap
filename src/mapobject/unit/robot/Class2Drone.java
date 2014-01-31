package mapobject.unit.robot;

import mapobject.MapObject;
import mapobject.shot.Shot;
import mapobject.unit.Unit;
import structure.Room;
import util.PowerupFactory;
import cannon.LaserCannon;

import common.ObjectType;

public class Class2Drone extends Robot {
  public Class2Drone(Room room, double x_loc, double y_loc, double direction) {
    super(Unit.getRadius(ObjectType.Class2Drone), new LaserCannon(Shot.getDamage(ObjectType.LaserShot), 2),
            room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Class2Drone;
  }

  @Override
  public MapObject releasePowerups() {
    if (Math.random() < 0.1) {
      return PowerupFactory.newPowerup(ObjectType.Shield, room, x_loc, y_loc);
    }
    return null;
  }
}
