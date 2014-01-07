package mapobject.shot;

import mapobject.MapObject;
import structure.Room;

import common.ObjectType;

public class LaserShot extends Shot {
  private final int level;

  public LaserShot(MapObject source, Room room, double x_loc, double y_loc, double direction, int level) {
    super(source, room, x_loc, y_loc, direction);
    image_name += level;
    this.level = level;
  }

  @Override
  public ObjectType getType() {
    return ObjectType.LaserShot;
  }
}
