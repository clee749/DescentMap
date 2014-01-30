package mapobject.powerup;

import mapobject.unit.Pyro;
import structure.Room;

import common.ObjectType;

public class Energy extends Powerup {
  public static final int ENERGY_AMOUNT = 18;

  public Energy(Room room, double x_loc, double y_loc, double direction, double speed) {
    super(room, x_loc, y_loc, direction, speed);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Energy;
  }

  @Override
  public boolean beAcquired(Pyro pyro) {
    return pyro.acquireEnergy(ENERGY_AMOUNT);
  }
}
