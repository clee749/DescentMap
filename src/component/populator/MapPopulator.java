package component.populator;

import structure.DescentMap;

public abstract class MapPopulator {
  protected final DescentMap map;

  public MapPopulator(DescentMap map) {
    this.map = map;
  }

  public abstract void populateMap();
}
