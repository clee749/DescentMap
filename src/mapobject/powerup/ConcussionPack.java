package mapobject.powerup;

import mapobject.unit.pyro.Pyro;
import structure.Room;

import common.ObjectType;

public class ConcussionPack extends Powerup {
  public static final int NUM_MISSILES = 4;

  public ConcussionPack(Room room, double x_loc, double y_loc, double direction, double speed) {
    super(room, x_loc, y_loc, direction, speed);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.ConcussionPack;
  }

  @Override
  public boolean beAcquired(Pyro pyro) {
    return pyro.acquireConcussionMissiles(NUM_MISSILES);
  }
}
