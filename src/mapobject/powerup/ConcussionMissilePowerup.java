package mapobject.powerup;

import mapobject.unit.Pyro;
import structure.Room;

import common.ObjectType;

public class ConcussionMissilePowerup extends Powerup {
  public ConcussionMissilePowerup(Room room, double x_loc, double y_loc, double direction, double speed) {
    super(room, x_loc, y_loc, direction, speed);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.ConcussionMissilePowerup;
  }

  @Override
  public boolean beAcquired(Pyro pyro) {
    return pyro.acquireConcussionMissiles(1);
  }
}
