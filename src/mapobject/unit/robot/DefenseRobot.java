package mapobject.unit.robot;

import mapobject.MapObject;
import mapobject.MultipleObject;
import structure.Room;
import util.PowerupFactory;
import cannon.LaserCannon;

import common.Constants;
import common.ObjectType;

public class DefenseRobot extends Robot {
  public static final int NUM_LASER_CANNON_POWERUPS_RELEASED = 3;

  public DefenseRobot(Room room, double x_loc, double y_loc, double direction) {
    super(Constants.getRadius(ObjectType.DefenseRobot), new LaserCannon(
            Constants.getDamage(ObjectType.LaserShot), 4), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.DefenseRobot;
  }

  @Override
  public MapObject releasePowerups() {
    double rand = Math.random();
    if (rand < 0.1) {
      return PowerupFactory.newPowerup(ObjectType.Shield, room, x_loc, y_loc);
    }
    if (rand < 0.2) {
      return PowerupFactory.newPowerup(ObjectType.Energy, room, x_loc, y_loc);
    }
    if (rand < 0.3) {
      MultipleObject powerups = new MultipleObject();
      for (int i = 0; i < NUM_LASER_CANNON_POWERUPS_RELEASED; ++i) {
        powerups.addObject(PowerupFactory.newPowerup(ObjectType.LaserCannonPowerup, room, x_loc, y_loc));
      }
      return powerups;
    }
    if (rand < 0.5) {
      return PowerupFactory.newPowerup(ObjectType.QuadLasers, room, x_loc, y_loc);
    }
    return null;
  }
}
