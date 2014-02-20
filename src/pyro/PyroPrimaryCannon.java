package pyro;

import mapobject.shot.Shot;
import cannon.Cannon;
import cannon.LaserCannon;
import cannon.PlasmaCannon;

import common.DescentMapException;
import common.ObjectType;

public enum PyroPrimaryCannon {
  LASER,
  PLASMA;

  public static Cannon createCannon(PyroPrimaryCannon cannon_type) {
    switch (cannon_type) {
      case LASER:
        return new LaserCannon(Shot.getDamage(ObjectType.LaserShot), true, 1);
      case PLASMA:
        return new PlasmaCannon(Shot.getDamage(ObjectType.PlasmaShot), true);
      default:
        throw new DescentMapException("Unexpected PyroPrimaryCannon: " + cannon_type);
    }
  }
}
