package mapobject.shot;

import mapobject.MapObject;
import structure.Room;

import common.ObjectType;

public class LaserShot extends Shot {
  public static final double MOVE_SPEED_INCREASE_PER_LEVEL = 0.1;

  public LaserShot(MapObject source, int damage, Room room, double x_loc, double y_loc, double direction,
          int level) {
    super(source, damage, room, x_loc, y_loc, direction);
    image_name += level;
    move_speed += MOVE_SPEED_INCREASE_PER_LEVEL * (level - 1);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.LaserShot;
  }
}
