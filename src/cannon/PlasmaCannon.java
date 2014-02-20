package cannon;

import mapobject.MapObject;
import mapobject.shot.PlasmaShot;
import structure.Room;

public class PlasmaCannon extends Cannon {
  public PlasmaCannon(int damage, boolean on_pyro) {
    super(damage, on_pyro);
  }

  @Override
  public void setSoundKey() {
    sound_key = (on_pyro ? "weapons/plasma.wav" : "weapons/laser06.wav");
  }

  @Override
  public MapObject fireCannon(MapObject source, Room room, double x_loc, double y_loc, double direction) {
    return new PlasmaShot(source, damage, room, x_loc, y_loc, direction);
  }
}
