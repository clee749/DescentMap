package util;

import mapobject.powerup.ConcussionMissilePowerup;
import mapobject.powerup.ConcussionPack;
import mapobject.powerup.Energy;
import mapobject.powerup.HomingMissilePowerup;
import mapobject.powerup.LaserCannonPowerup;
import mapobject.powerup.PlasmaCannonPowerup;
import mapobject.powerup.Powerup;
import mapobject.powerup.QuadLasers;
import mapobject.powerup.Shield;
import structure.Room;

import common.DescentMapException;
import common.ObjectType;

public class PowerupFactory {
  public static final double POWERUP_MAX_SPEED = 2.0;

  protected PowerupFactory() {

  }

  public static Powerup newPowerup(ObjectType type, Room room, double x_loc, double y_loc) {
    switch (type) {
      case ConcussionMissilePowerup:
        return new ConcussionMissilePowerup(room, x_loc, y_loc, randomPowerupDirection(),
                randomPowerupSpeed());
      case ConcussionPack:
        return new ConcussionPack(room, x_loc, y_loc, randomPowerupDirection(), randomPowerupSpeed());
      case Energy:
        return new Energy(room, x_loc, y_loc, randomPowerupDirection(), randomPowerupSpeed());
      case HomingMissilePowerup:
        return new HomingMissilePowerup(room, x_loc, y_loc, randomPowerupDirection(), randomPowerupSpeed());
      case QuadLasers:
        return new QuadLasers(room, x_loc, y_loc, randomPowerupDirection(), randomPowerupSpeed());
      case LaserCannonPowerup:
        return new LaserCannonPowerup(room, x_loc, y_loc, randomPowerupDirection(), randomPowerupSpeed());
      case PlasmaCannonPowerup:
        return new PlasmaCannonPowerup(room, x_loc, y_loc, randomPowerupDirection(), randomPowerupSpeed());
      case Shield:
        return new Shield(room, x_loc, y_loc, randomPowerupDirection(), randomPowerupSpeed());
      default:
        throw new DescentMapException("Unexpected Powerup type: " + type);
    }
  }

  public static double randomPowerupDirection() {
    return Math.random() * MapUtils.TWO_PI;
  }

  public static double randomPowerupSpeed() {
    return Math.random() * POWERUP_MAX_SPEED;
  }
}
