package cannon;

import mapobject.MapObject;
import mapobject.shot.LaserShot;
import structure.Room;

public class LaserCannon extends Cannon {
  private final int level;

  public LaserCannon(int damage, int level) {
    super(damage);
    this.level = level;
  }

  @Override
  public MapObject fireCannon(MapObject source, Room room, double x_loc, double y_loc, double direction) {
    return new LaserShot(source, damage, room, x_loc, y_loc, direction, level);
  }

}
