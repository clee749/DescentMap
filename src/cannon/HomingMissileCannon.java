package cannon;

import mapobject.MapObject;
import mapobject.shot.HomingMissile;
import structure.Room;

public class HomingMissileCannon extends Cannon {
  public HomingMissileCannon(int damage, boolean on_pyro) {
    super(damage, on_pyro);
  }

  @Override
  public void setSoundKey() {
    sound_key = "weapons/missile1.wav";
  }

  @Override
  public MapObject fireCannon(MapObject source, Room room, double x_loc, double y_loc, double direction) {
    return new HomingMissile(source, damage, room, x_loc, y_loc, direction);
  }
}
