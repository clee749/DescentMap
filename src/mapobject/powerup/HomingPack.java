package mapobject.powerup;

import mapobject.unit.Pyro;
import pyro.PyroSecondaryCannon;
import structure.Room;

import common.ObjectType;

public class HomingPack extends Powerup {
  public static final int NUM_MISSILES = 4;

  public HomingPack(Room room, double x_loc, double y_loc, double direction, double speed) {
    super(room, x_loc, y_loc, direction, speed);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.HomingPack;
  }

  @Override
  public boolean beAcquired(Pyro pyro) {
    return pyro.acquireSecondaryAmmo(PyroSecondaryCannon.HOMING_MISSILE, NUM_MISSILES);
  }
}
