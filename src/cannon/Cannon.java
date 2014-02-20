package cannon;

import mapobject.MapObject;
import structure.Room;

public abstract class Cannon {
  protected final int damage;
  protected final boolean on_pyro;
  protected String sound_key;

  public Cannon(int damage, boolean on_pyro) {
    this.damage = damage;
    this.on_pyro = on_pyro;
    setSoundKey();
  }

  public String getSoundKey() {
    return sound_key;
  }

  public abstract void setSoundKey();

  public abstract MapObject fireCannon(MapObject source, Room room, double x_loc, double y_loc,
          double direction);
}
