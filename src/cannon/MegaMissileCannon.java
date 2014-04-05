package cannon;

import mapobject.MapObject;
import mapobject.shot.MegaMissile;
import structure.Room;

public class MegaMissileCannon extends Cannon {
  public MegaMissileCannon(int damage, boolean on_pyro) {
    super(damage, on_pyro);
  }

  @Override
  public void setSoundKey() {
    sound_key = "weapons/missile3.wav";
  }

  @Override
  public MapObject fireCannon(MapObject source, Room room, double x_loc, double y_loc, double direction) {
    return new MegaMissile(source, damage, room, x_loc, y_loc, direction);
  }
}
