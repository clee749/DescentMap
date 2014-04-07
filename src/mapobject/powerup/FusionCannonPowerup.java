package mapobject.powerup;

import pyro.PyroPrimaryCannon;
import mapobject.unit.Pyro;
import structure.Room;

import common.ObjectType;

public class FusionCannonPowerup extends Powerup {
  public FusionCannonPowerup(Room room, double x_loc, double y_loc, double direction, double speed) {
    super(room, x_loc, y_loc, direction, speed);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.FusionCannonPowerup;
  }

  @Override
  public boolean beAcquired(Pyro pyro) {
    return pyro.acquireCannon(PyroPrimaryCannon.FUSION);
  }
}
