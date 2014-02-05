package mapobject.shot;

import mapobject.MapObject;
import mapobject.ephemeral.Explosion;
import mapobject.unit.Unit;
import pilot.Pilot;
import structure.Room;

import component.MapEngine;

public abstract class SplashDamageShot extends Shot {
  public SplashDamageShot(Pilot pilot, MapObject source, int damage, Room room, double x_loc, double y_loc,
          double direction) {
    super(pilot, source, damage, room, x_loc, y_loc, direction);
  }

  public SplashDamageShot(MapObject source, int damage, Room room, double x_loc, double y_loc,
          double direction) {
    super(source, damage, room, x_loc, y_loc, direction);
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    MapObject created_object = super.doNextAction(engine, s_elapsed);
    if (is_detonated) {
      room.doSplashDamage(this, damage, SPLASH_DAMAGE_RADIUS, null);
      return new Explosion(room, x_loc, y_loc, Math.min(damage / EXPLOSION_RADIUS_DIVISOR,
              EXPLOSION_MAX_RADIUS), Math.min(damage / EXPLOSION_TIME_DIVISOR, EXPLOSION_MAX_TIME));
    }
    return created_object;
  }

  @Override
  public MapObject handleUnitCollision(Unit hit_unit) {
    room.doSplashDamage(this, damage, SPLASH_DAMAGE_RADIUS, hit_unit);
    return super.handleUnitCollision(hit_unit);
  }

  @Override
  public MapObject handleWallCollision() {
    super.handleWallCollision();
    room.doSplashDamage(this, damage, SPLASH_DAMAGE_RADIUS, null);
    return new Explosion(room, x_loc, y_loc,
            Math.min(damage / EXPLOSION_RADIUS_DIVISOR, EXPLOSION_MAX_RADIUS), Math.min(damage /
                    EXPLOSION_TIME_DIVISOR, EXPLOSION_MAX_TIME));
  }
}
