package mapobject.powerup;

import mapobject.unit.Pyro;
import structure.Room;

import common.ObjectType;

public class QuadLasers extends Powerup {
  public QuadLasers(Room room, double x_loc, double y_loc, double direction, double speed) {
    super(room, x_loc, y_loc, direction, speed);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.QuadLasers;
  }

  @Override
  public boolean beAcquired(Pyro pyro) {
    return pyro.acquireQuadLasers();
  }
}
