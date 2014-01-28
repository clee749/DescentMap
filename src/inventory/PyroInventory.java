package inventory;

import mapobject.unit.pyro.Pyro;

public class PyroInventory {
  public final int shields;
  public final double energy;
  public final int num_concussion_missiles;

  public PyroInventory(Pyro pyro) {
    shields = pyro.getShields();
    energy = pyro.getEnergy();
    num_concussion_missiles = 3;
  }
}
