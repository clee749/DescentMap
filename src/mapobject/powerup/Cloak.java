package mapobject.powerup;

import mapobject.unit.Pyro;
import structure.Room;

import common.ObjectType;

public class Cloak extends Powerup {
  public static final int CLOAK_TIME = 30;

  public Cloak(Room room, double x_loc, double y_loc, double direction, double speed) {
    super(room, x_loc, y_loc, direction, speed);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Cloak;
  }

  @Override
  public boolean beAcquired(Pyro pyro) {
    return pyro.acquireCloak(CLOAK_TIME);
  }
}
