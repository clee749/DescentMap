package cannon;

import mapobject.MapObject;
import mapobject.shot.FusionShot;
import structure.Room;

public class FusionCannon extends Cannon {
  public FusionCannon(int damage, boolean on_pyro) {
    super(damage, on_pyro);
  }

  @Override
  public void setSoundKey() {
    sound_key = "weapons/fusion.wav";
  }

  @Override
  public MapObject fireCannon(MapObject source, Room room, double x_loc, double y_loc, double direction) {
    return new FusionShot(source, damage, room, x_loc, y_loc, direction);
  }
}
