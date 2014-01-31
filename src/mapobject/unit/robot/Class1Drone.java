package mapobject.unit.robot;

import mapobject.MapObject;
import mapobject.shot.Shot;
import mapobject.unit.Unit;
import structure.Room;
import util.PowerupFactory;
import cannon.FireballCannon;

import common.ObjectType;

public class Class1Drone extends Robot {
  public Class1Drone(Room room, double x_loc, double y_loc, double direction) {
    super(Unit.getRadius(ObjectType.Class1Drone),
            new FireballCannon(Shot.getDamage(ObjectType.FireballShot)), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Class1Drone;
  }

  @Override
  public MapObject releasePowerups() {
    if (Math.random() < 0.1) {
      return PowerupFactory.newPowerup(ObjectType.Energy, room, x_loc, y_loc);
    }
    return null;
  }
}
