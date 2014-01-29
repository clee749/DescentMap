package mapobject.unit.robot;

import mapobject.MapObject;
import structure.Room;
import util.PowerupFactory;
import cannon.FireballCannon;

import common.Constants;
import common.ObjectType;

public class Class1Drone extends Robot {
  public Class1Drone(Room room, double x_loc, double y_loc, double direction) {
    super(Constants.getRadius(ObjectType.Class1Drone), new FireballCannon(
            Constants.getDamage(ObjectType.FireballShot)), room, x_loc, y_loc, direction);
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
