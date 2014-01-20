package mapobject.powerup;

import mapobject.unit.pyro.Pyro;
import structure.Room;

import common.ObjectType;

public class Shield extends Powerup {
  public static final int SHIELD_AMOUNT = 18;

  public Shield(Room room, double x_loc, double y_loc, double direction, double speed) {
    super(room, x_loc, y_loc, direction, speed);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Shield;
  }

  @Override
  public boolean beAcquired(Pyro pyro) {
    return pyro.acquireShield(SHIELD_AMOUNT);
  }
}
