package mapobject.unit.robot;

import mapobject.MapObject;
import mapobject.MultipleObject;
import mapobject.shot.Shot;
import mapobject.unit.Unit;
import structure.Room;
import util.PowerupFactory;
import cannon.LaserCannon;

import common.ObjectType;

public class DefenseRobot extends Robot {
  public static final int NUM_LASER_CANNON_POWERUPS_RELEASED = 3;

  public DefenseRobot(Room room, double x_loc, double y_loc, double direction) {
    super(Unit.getRadius(ObjectType.DefenseRobot), new LaserCannon(Shot.getDamage(ObjectType.LaserShot),
            false, 4), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.DefenseRobot;
  }

  @Override
  public MapObject releasePowerups() {
    double rand = Math.random();
    if (rand < 0.1) {
      return PowerupFactory.newReleasedPowerup(ObjectType.Shield, room, x_loc, y_loc);
    }
    if (rand < 0.2) {
      return PowerupFactory.newReleasedPowerup(ObjectType.Energy, room, x_loc, y_loc);
    }
    if (rand < 0.3) {
      MultipleObject powerups = new MultipleObject();
      for (int i = 0; i < NUM_LASER_CANNON_POWERUPS_RELEASED; ++i) {
        powerups.addObject(PowerupFactory.newReleasedPowerup(ObjectType.LaserCannonPowerup, room, x_loc,
                y_loc));
      }
      return powerups;
    }
    if (rand < 0.5) {
      return PowerupFactory.newReleasedPowerup(ObjectType.QuadLasers, room, x_loc, y_loc);
    }
    return null;
  }
}
