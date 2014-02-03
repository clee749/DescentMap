package cannon;

import mapobject.MapObject;
import mapobject.ProximityBomb;
import structure.Room;

public class ProximityBombCannon extends Cannon {
  public ProximityBombCannon(int damage) {
    super(damage);
  }

  @Override
  public MapObject fireCannon(MapObject source, Room room, double x_loc, double y_loc, double direction) {
    return new ProximityBomb(source, damage, room, x_loc, y_loc);
  }
}
