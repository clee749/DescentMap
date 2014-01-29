package cannon;

import mapobject.MapObject;
import mapobject.shot.LaserShot;
import structure.Room;

public class LaserCannon extends Cannon {
  public static final int MAX_LEVEL = 4;

  private int level;

  public LaserCannon(int damage, int level) {
    super(damage);
    this.level = level;
  }

  @Override
  public MapObject fireCannon(MapObject source, Room room, double x_loc, double y_loc, double direction) {
    return new LaserShot(source, damage, room, x_loc, y_loc, direction, level);
  }

  public int getLevel() {
    return level;
  }

  public boolean incrementLevel() {
    if (level < MAX_LEVEL) {
      ++level;
      return true;
    }
    return false;
  }
}
