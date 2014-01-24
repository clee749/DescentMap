package mapobject.shot;

import mapobject.MapObject;
import mapobject.ephemeral.Explosion;
import mapobject.unit.Unit;
import pilot.Pilot;
import structure.Room;

public abstract class Missile extends Shot {
  public static final double SPLASH_DAMAGE_RADIUS = 1.0;

  public Missile(Pilot pilot, MapObject source, int damage, Room room, double x_loc, double y_loc,
          double direction) {
    super(pilot, source, damage, room, x_loc, y_loc, direction);
  }

  public Missile(MapObject source, int damage, Room room, double x_loc, double y_loc, double direction) {
    super(source, damage, room, x_loc, y_loc, direction);
  }

  @Override
  public MapObject handleUnitCollision(Unit hit_unit) {
    is_in_map = false;
    hit_unit.beDamaged(damage);
    room.doSplashDamage(hit_unit, x_loc, y_loc, damage, SPLASH_DAMAGE_RADIUS);
    return new Explosion(room, x_loc, y_loc,
            Math.min(damage / EXPLOSION_RADIUS_DIVISOR, EXPLOSION_MAX_RADIUS), Math.min(damage /
                    EXPLOSION_TIME_DIVISOR, EXPLOSION_MAX_TIME));
  }

  @Override
  public MapObject handleWallCollision() {
    is_in_map = false;
    room.doSplashDamage(null, x_loc, y_loc, damage, SPLASH_DAMAGE_RADIUS);
    return new Explosion(room, x_loc, y_loc,
            Math.min(damage / EXPLOSION_RADIUS_DIVISOR, EXPLOSION_MAX_RADIUS), Math.min(damage /
                    EXPLOSION_TIME_DIVISOR, EXPLOSION_MAX_TIME));
  }
}
