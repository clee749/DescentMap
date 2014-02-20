package cannon;

import mapobject.MapObject;
import mapobject.shot.LaserShot;
import structure.Room;

public class LaserCannon extends Cannon {
  public static final int MAX_LEVEL = 4;

  private int level;

  public LaserCannon(int damage, boolean on_pyro, int level) {
    super(damage, on_pyro);
    this.level = level;
    setSoundKey();
  }

  @Override
  public void setSoundKey() {
    if (level < 1) {
      return;
    }
    if (on_pyro) {
      switch (level) {
        case 1:
          sound_key = "weapons/laser03.wav";
          break;
        case 2:
          sound_key = "weapons/laser04.wav";
          break;
        case 3:
          sound_key = "weapons/laser02.wav";
          break;
        case 4:
          sound_key = "weapons/laser01.wav";
          break;
        default:
          sound_key = "weapons/laser07.wav";
      }
    }
    else {
      sound_key = "weapons/laser02.wav";
    }
  }

  @Override
  public MapObject fireCannon(MapObject source, Room room, double x_loc, double y_loc, double direction) {
    return new LaserShot(source, damage, room, x_loc, y_loc, direction, level);
  }

  public int getLevel() {
    return level;
  }

  public boolean incrementLevel() {
    if (level < MAX_LEVEL) {
      ++level;
      setSoundKey();
      return true;
    }
    return false;
  }
}
