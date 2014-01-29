package mapobject.shot;

import mapobject.MapObject;
import structure.Room;

import common.ObjectType;

public class FireballShot extends Shot {
  public FireballShot(MapObject source, int damage, Room room, double x_loc, double y_loc, double direction) {
    super(source, damage, room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.FireballShot;
  }
}
