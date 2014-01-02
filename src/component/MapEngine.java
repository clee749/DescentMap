package component;

import structure.DescentMap;
import mapobject.unit.pyro.Pyro;

public class MapEngine {
  private final DescentMap map;
  private Pyro center_ship;

  public MapEngine(DescentMap map) {
    this.map = map;
  }

  public void setCenterShip(Pyro ship) {
    center_ship = ship;
  }

  public void computeNextStep() {
    center_ship.computeNextStep();
  }

  public void doNextStep(long ms_elapsed) {
    center_ship.doNextStep(ms_elapsed);
  }

  public boolean levelComplete() {
    return center_ship.getRoom().equals(map.getExitRoom());
  }
}
