package mapobject.powerup;

import mapobject.unit.pyro.Pyro;
import structure.Room;

import common.ObjectType;

public class LaserCannonPowerup extends Powerup {
  public LaserCannonPowerup(Room room, double x_loc, double y_loc, double direction, double speed) {
    super(room, x_loc, y_loc, direction, speed);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.LaserCannonPowerup;
  }

  @Override
  public boolean beAcquired(Pyro pyro) {
    return pyro.acquireLaserCannon();
  }
}
