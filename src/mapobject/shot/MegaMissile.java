package mapobject.shot;

import mapobject.MapObject;
import mapobject.ephemeral.Explosion;
import pilot.HomingPilot;
import structure.Room;
import util.MapUtils;

import common.ObjectType;

public class MegaMissile extends Missile {
  public static final double MAX_ANGLE_TO_TARGET = MapUtils.PI_OVER_FOUR;
  public static final double SPLASH_DAMAGE_RADIUS = 3.0;
  public static final double EXPLOSION_TIME = 0.5;

  public MegaMissile(MapObject source, int damage, Room room, double x_loc, double y_loc, double direction) {
    super(new HomingPilot(source, MAX_ANGLE_TO_TARGET), source, damage, room, x_loc, y_loc, direction);
    splash_damage_radius = SPLASH_DAMAGE_RADIUS;
  }

  @Override
  public ObjectType getType() {
    return ObjectType.MegaMissile;
  }

  @Override
  public Explosion createExplosion() {
    return new Explosion(room, x_loc, y_loc, SPLASH_DAMAGE_RADIUS, EXPLOSION_TIME);
  }
}
