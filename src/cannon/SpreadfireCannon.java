package cannon;

import mapobject.MapObject;
import mapobject.shot.SpreadfireShot;
import structure.Room;

public class SpreadfireCannon extends Cannon {
  public SpreadfireCannon(int damage, boolean on_pyro) {
    super(damage, on_pyro);
  }

  @Override
  public void setSoundKey() {
    sound_key = "weapons/laser06.wav";
  }

  @Override
  public MapObject fireCannon(MapObject source, Room room, double x_loc, double y_loc, double direction) {
    return new SpreadfireShot(source, damage, room, x_loc, y_loc, direction);
  }
}
