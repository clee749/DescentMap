package mapobject.shot;

import mapobject.MapObject;
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
      return createExplosion();
    }
    return created_object;
  }

  @Override
  public MapObject handleUnitCollision(MapEngine engine, Unit hit_unit) {
    room.doSplashDamage(this, damage, SPLASH_DAMAGE_RADIUS, hit_unit);
    playSound(engine, "weapons/explode1.wav");
    return super.handleUnitCollision(engine, hit_unit);
  }

  @Override
  public MapObject handleWallCollision(MapEngine engine) {
    super.handleWallCollision(engine);
    room.doSplashDamage(this, damage, SPLASH_DAMAGE_RADIUS, null);
    playSound(engine, "weapons/explode1.wav");
    return createExplosion();
  }
}
