package mapobject.shot;

import mapobject.MapObject;
import mapobject.MultipleObject;
import mapobject.ephemeral.Explosion;
import mapobject.unit.Unit;
import pilot.Pilot;
import structure.Room;

import component.MapEngine;

public abstract class Missile extends ExplosiveShot {
  public static final double SPLASH_DAMAGE_RADIUS = 1.0;
  public static final double ROCKET_EXPLOSION_RADIUS = 0.03;
  public static final double ROCKET_EXPLOSION_TIME = 0.25;

  protected double splash_damage_radius;

  public Missile(Pilot pilot, MapObject source, int damage, Room room, double x_loc, double y_loc,
          double direction) {
    super(pilot, source, damage, room, x_loc, y_loc, direction);
    splash_damage_radius = SPLASH_DAMAGE_RADIUS;
  }

  public Missile(MapObject source, int damage, Room room, double x_loc, double y_loc, double direction) {
    super(source, damage, room, x_loc, y_loc, direction);
    splash_damage_radius = SPLASH_DAMAGE_RADIUS;
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    MultipleObject created_objects = new MultipleObject();
    created_objects.addObject(new Explosion(room, x_loc, y_loc, ROCKET_EXPLOSION_RADIUS,
            ROCKET_EXPLOSION_TIME));
    created_objects.addObject(super.doNextAction(engine, s_elapsed));
    if (is_detonated) {
      room.doSplashDamage(this, damage, splash_damage_radius, null);
      created_objects.addObject(createExplosion());
    }
    return created_objects;
  }

  @Override
  public MapObject handleUnitCollision(MapEngine engine, Unit hit_unit) {
    room.doSplashDamage(this, damage, splash_damage_radius, hit_unit);
    return super.handleUnitCollision(engine, hit_unit);
  }

  @Override
  public MapObject handleWallCollision(MapEngine engine) {
    room.doSplashDamage(this, damage, splash_damage_radius, null);
    return super.handleWallCollision(engine);
  }
}
