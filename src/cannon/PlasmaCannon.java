package cannon;

import mapobject.MapObject;
import mapobject.shot.PlasmaShot;
import structure.Room;

public class PlasmaCannon extends Cannon {
  public PlasmaCannon(int damage) {
    super(damage);
  }

  @Override
  public MapObject fireCannon(MapObject source, Room room, double x_loc, double y_loc, double direction) {
    return new PlasmaShot(source, damage, room, x_loc, y_loc, direction);
  }
}
