package mapobject.powerup;

import pyro.PyroSecondaryCannon;
import mapobject.unit.Pyro;
import structure.Room;

import common.ObjectType;

public class MegaMissilePowerup extends Powerup {
  public MegaMissilePowerup(Room room, double x_loc, double y_loc, double direction, double speed) {
    super(room, x_loc, y_loc, direction, speed);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.MegaMissilePowerup;
  }

  @Override
  public boolean beAcquired(Pyro pyro) {
    return pyro.acquireSecondaryAmmo(PyroSecondaryCannon.MEGA_MISSILE, 1);
  }
}
