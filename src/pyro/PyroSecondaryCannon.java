package pyro;

import mapobject.ProximityBomb;
import mapobject.shot.Shot;
import cannon.Cannon;
import cannon.ConcussionMissileCannon;
import cannon.HomingMissileCannon;
import cannon.ProximityBombCannon;

import common.DescentMapException;
import common.ObjectType;

public enum PyroSecondaryCannon {
  CONCUSSION_MISSILE,
  HOMING_MISSILE,
  PROXIMITY_BOMB;

  public static Cannon createCannon(PyroSecondaryCannon cannon_type) {
    switch (cannon_type) {
      case CONCUSSION_MISSILE:
        return new ConcussionMissileCannon(Shot.getDamage(ObjectType.ConcussionMissile));
      case HOMING_MISSILE:
        return new HomingMissileCannon(Shot.getDamage(ObjectType.HomingMissile));
      case PROXIMITY_BOMB:
        return new ProximityBombCannon(ProximityBomb.DAMAGE);
      default:
        throw new DescentMapException("Unexpected PyroSecondaryCannon: " + cannon_type);
    }
  }

  public static Cannon[] createCannons() {
    PyroSecondaryCannon[] values = values();
    Cannon[] cannons = new Cannon[values.length];
    for (int i = 0; i < values.length; ++i) {
      cannons[i] = createCannon(values[i]);
    }
    return cannons;
  }
}
