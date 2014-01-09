package mapobject.powerup;

import mapobject.unit.pyro.Pyro;
import structure.Room;

import common.Constants;
import common.ObjectType;

public class Shield extends Powerup {
  public Shield(Room room, double x_loc, double y_loc, double direction, double speed) {
    super(room, x_loc, y_loc, direction, speed);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Shield;
  }

  @Override
  public boolean beAcquired(Pyro pyro) {
    return pyro.acquireShield(Constants.POWERUP_SHIELD_AMOUNT);
  }
}
