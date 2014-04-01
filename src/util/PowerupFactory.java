package util;

import mapobject.powerup.Cloak;
import mapobject.powerup.ConcussionMissilePowerup;
import mapobject.powerup.ConcussionPack;
import mapobject.powerup.Energy;
import mapobject.powerup.FusionCannonPowerup;
import mapobject.powerup.HomingMissilePowerup;
import mapobject.powerup.HomingPack;
import mapobject.powerup.LaserCannonPowerup;
import mapobject.powerup.PlasmaCannonPowerup;
import mapobject.powerup.Powerup;
import mapobject.powerup.ProximityPack;
import mapobject.powerup.QuadLasers;
import mapobject.powerup.Shield;
import mapobject.powerup.SpreadfireCannonPowerup;
import structure.Room;

import common.DescentMapException;
import common.ObjectType;

public class PowerupFactory {
  public static final double POWERUP_MAX_SPEED = 2.0;

  protected PowerupFactory() {

  }

  public static Powerup newPowerup(ObjectType type, Room room, double x_loc, double y_loc, double direction,
          double speed) {
    switch (type) {
      case Cloak:
        return new Cloak(room, x_loc, y_loc, direction, speed);
      case ConcussionMissilePowerup:
        return new ConcussionMissilePowerup(room, x_loc, y_loc, direction, speed);
      case ConcussionPack:
        return new ConcussionPack(room, x_loc, y_loc, direction, speed);
      case Energy:
        return new Energy(room, x_loc, y_loc, direction, speed);
      case FusionCannonPowerup:
        return new FusionCannonPowerup(room, x_loc, y_loc, direction, speed);
      case HomingMissilePowerup:
        return new HomingMissilePowerup(room, x_loc, y_loc, direction, speed);
      case HomingPack:
        return new HomingPack(room, x_loc, y_loc, direction, speed);
      case ProximityPack:
        return new ProximityPack(room, x_loc, y_loc, direction, speed);
      case QuadLasers:
        return new QuadLasers(room, x_loc, y_loc, direction, speed);
      case LaserCannonPowerup:
        return new LaserCannonPowerup(room, x_loc, y_loc, direction, speed);
      case PlasmaCannonPowerup:
        return new PlasmaCannonPowerup(room, x_loc, y_loc, direction, speed);
      case Shield:
        return new Shield(room, x_loc, y_loc, direction, speed);
      case SpreadfireCannonPowerup:
        return new SpreadfireCannonPowerup(room, x_loc, y_loc, direction, speed);
      default:
        throw new DescentMapException("Unexpected Powerup type: " + type);
    }
  }

  public static Powerup newReleasedPowerup(ObjectType type, Room room, double x_loc, double y_loc) {
    return newPowerup(type, room, x_loc, y_loc, randomPowerupDirection(), randomPowerupSpeed());
  }

  public static Powerup newStationaryPowerup(ObjectType type, Room room, double x_loc, double y_loc) {
    return newPowerup(type, room, x_loc, y_loc, 0.0, 0.0);
  }

  public static double randomPowerupDirection() {
    return Math.random() * MapUtils.TWO_PI;
  }

  public static double randomPowerupSpeed() {
    return Math.random() * POWERUP_MAX_SPEED;
  }
}
