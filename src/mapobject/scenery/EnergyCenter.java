package mapobject.scenery;

import mapobject.MapObject;
import mapobject.unit.Pyro;
import structure.Room;

import common.ObjectType;
import component.MapEngine;

public class EnergyCenter extends Scenery {
  public EnergyCenter(Room room, double x_loc, double y_loc) {
    super(room, x_loc, y_loc);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.EnergyCenter;
  }

  @Override
  public void planNextAction(double s_elapsed) {

  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    for (Pyro pyro : room.getPyros()) {
      if (pyro.getShields() >= 0 && Math.abs(x_loc - pyro.getX()) < radius &&
              Math.abs(y_loc - pyro.getY()) < radius) {
        pyro.rechargeEnergy();
      }
    }
    return null;
  }
}
