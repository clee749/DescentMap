package cannon;

import mapobject.MapObject;
import mapobject.shot.ConcussionMissile;
import structure.Room;

public class ConcussionMissileCannon extends Cannon {
  public ConcussionMissileCannon(int damage, boolean on_pyro) {
    super(damage, on_pyro);
  }

  @Override
  public void setSoundKey() {
    sound_key = "weapons/missile1.wav";
  }

  @Override
  public MapObject fireCannon(MapObject source, Room room, double x_loc, double y_loc, double direction) {
    return new ConcussionMissile(source, damage, room, x_loc, y_loc, direction);
  }
}
