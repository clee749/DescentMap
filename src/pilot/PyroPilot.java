package pilot;

import mapobject.MovableObject;
import mapobject.unit.Pyro;

public abstract class PyroPilot extends UnitPilot {
  protected Pyro bound_pyro;

  @Override
  public void bindToObject(MovableObject object) {
    super.bindToObject(object);
    bound_pyro = (Pyro) object;
  }

  public abstract void newLevel();

  public abstract void startPilot();

  public abstract boolean isReadyToRespawn();

  public abstract void handleRespawnDelay(double s_elapsed);

  public abstract void prepareForRespawn();
}
