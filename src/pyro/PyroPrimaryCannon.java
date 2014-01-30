package pyro;

import cannon.Cannon;
import cannon.LaserCannon;
import cannon.PlasmaCannon;

import common.Constants;
import common.DescentMapException;
import common.ObjectType;

public enum PyroPrimaryCannon {
  LASER,
  PLASMA;

  public static Cannon createCannon(PyroPrimaryCannon cannon_type) {
    switch (cannon_type) {
      case LASER:
        return new LaserCannon(Constants.getDamage(ObjectType.LaserShot), 1);
      case PLASMA:
        return new PlasmaCannon(Constants.getDamage(ObjectType.PlasmaShot));
      default:
        throw new DescentMapException("Unexpected PyroPrimaryCannon: " + cannon_type);
    }
  }
}
