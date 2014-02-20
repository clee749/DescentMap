package cannon;

import mapobject.MapObject;
import mapobject.ProximityBomb;
import structure.Room;

public class ProximityBombCannon extends Cannon {
  public ProximityBombCannon(int damage, boolean on_pyro) {
    super(damage, on_pyro);
  }

  @Override
  public void setSoundKey() {
    sound_key = "weapons/dropbomb.wav";
  }

  @Override
  public MapObject fireCannon(MapObject source, Room room, double x_loc, double y_loc, double direction) {
    return new ProximityBomb(source, damage, room, x_loc, y_loc);
  }
}
