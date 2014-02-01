package mapobject.powerup;

import mapobject.unit.Pyro;
import pyro.PyroSecondaryCannon;
import structure.Room;

import common.ObjectType;

public class HomingMissilePowerup extends Powerup {
  public HomingMissilePowerup(Room room, double x_loc, double y_loc, double direction, double speed) {
    super(room, x_loc, y_loc, direction, speed);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.HomingMissilePowerup;
  }

  @Override
  public boolean beAcquired(Pyro pyro) {
    return pyro.acquireSecondaryAmmo(PyroSecondaryCannon.HOMING_MISSILE, 1);
  }
}
