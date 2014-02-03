package mapobject.powerup;

import mapobject.unit.Pyro;
import pyro.PyroSecondaryCannon;
import structure.Room;

import common.ObjectType;

public class ProximityPack extends Powerup {
  public static final int NUM_BOMBS = 4;

  public ProximityPack(Room room, double x_loc, double y_loc, double direction, double speed) {
    super(room, x_loc, y_loc, direction, speed);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.ProximityPack;
  }

  @Override
  public boolean beAcquired(Pyro pyro) {
    return pyro.acquireSecondaryAmmo(PyroSecondaryCannon.PROXIMITY_BOMB, NUM_BOMBS);
  }
}
