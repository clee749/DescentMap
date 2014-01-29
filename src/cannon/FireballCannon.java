package cannon;

import mapobject.MapObject;
import mapobject.shot.FireballShot;
import structure.Room;

public class FireballCannon extends Cannon {
  public FireballCannon(int damage) {
    super(damage);
  }

  @Override
  public MapObject fireCannon(MapObject source, Room room, double x_loc, double y_loc, double direction) {
    return new FireballShot(source, damage, room, x_loc, y_loc, direction);
  }
}
