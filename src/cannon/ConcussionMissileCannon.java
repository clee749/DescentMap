package cannon;

import mapobject.MapObject;
import mapobject.shot.ConcussionMissile;
import structure.Room;

public class ConcussionMissileCannon extends Cannon {
  public ConcussionMissileCannon(int damage) {
    super(damage);
  }

  @Override
  public MapObject fireCannon(MapObject source, Room room, double x_loc, double y_loc, double direction) {
    return new ConcussionMissile(source, damage, room, x_loc, y_loc, direction);
  }
}
