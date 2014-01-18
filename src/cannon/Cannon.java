package cannon;

import mapobject.MapObject;
import structure.Room;

public abstract class Cannon {
  protected final int damage;

  public Cannon(int damage) {
    this.damage = damage;
  }

  public abstract MapObject fireCannon(MapObject source, Room room, double x_loc, double y_loc,
          double direction);
}
