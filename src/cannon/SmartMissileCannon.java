package cannon;

import mapobject.MapObject;
import mapobject.shot.SmartMissile;
import structure.Room;

public class SmartMissileCannon extends Cannon {
  private final int smart_plasma_damage;

  public SmartMissileCannon(int damage, int smart_plasma_damage, boolean on_pyro) {
    super(damage, on_pyro);
    this.smart_plasma_damage = smart_plasma_damage;
  }

  @Override
  public void setSoundKey() {
    sound_key = "weapons/missile1.wav";
  }

  @Override
  public MapObject fireCannon(MapObject source, Room room, double x_loc, double y_loc, double direction) {
    return new SmartMissile(source, damage, smart_plasma_damage, room, x_loc, y_loc, direction);
  }
}
