package pyro;

import java.util.HashMap;

import cannon.Cannon;
import cannon.FusionCannon;
import cannon.LaserCannon;
import cannon.PlasmaCannon;
import cannon.SpreadfireCannon;

import common.DescentMapException;

public enum PyroPrimaryCannon {
  LASER,
  SPREADFIRE,
  PLASMA,
  FUSION;

  private static final HashMap<PyroPrimaryCannon, Integer> PYRO_DAMAGES = getPyroDamages();

  private static HashMap<PyroPrimaryCannon, Integer> getPyroDamages() {
    HashMap<PyroPrimaryCannon, Integer> damages = new HashMap<PyroPrimaryCannon, Integer>();
    damages.put(LASER, 3);
    damages.put(SPREADFIRE, 3);
    damages.put(PLASMA, 3);
    damages.put(FUSION, 18);
    return damages;
  }

  public static Cannon createCannon(PyroPrimaryCannon cannon_type) {
    switch (cannon_type) {
      case LASER:
        return new LaserCannon(PYRO_DAMAGES.get(cannon_type), true);
      case SPREADFIRE:
        return new SpreadfireCannon(PYRO_DAMAGES.get(cannon_type), true);
      case PLASMA:
        return new PlasmaCannon(PYRO_DAMAGES.get(cannon_type), true);
      case FUSION:
        return new FusionCannon(PYRO_DAMAGES.get(cannon_type), true);
      default:
        throw new DescentMapException("Unexpected PyroPrimaryCannon: " + cannon_type);
    }
  }
}
