package mapobject.shot;

import mapobject.MapObject;
import mapobject.MultipleObject;
import mapobject.ephemeral.Explosion;
import pilot.Pilot;
import structure.Room;

import component.MapEngine;

public abstract class Missile extends SplashDamageShot {
  public static final double ROCKET_EXPLOSION_RADIUS = 0.03;
  public static final double ROCKET_EXPLOSION_TIME = 0.25;

  public Missile(Pilot pilot, MapObject source, int damage, Room room, double x_loc, double y_loc,
          double direction) {
    super(pilot, source, damage, room, x_loc, y_loc, direction);
  }

  public Missile(MapObject source, int damage, Room room, double x_loc, double y_loc, double direction) {
    super(source, damage, room, x_loc, y_loc, direction);
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    MultipleObject created_objects = new MultipleObject();
    created_objects.addObject(new Explosion(room, x_loc, y_loc, ROCKET_EXPLOSION_RADIUS,
            ROCKET_EXPLOSION_TIME));
    created_objects.addObject(super.doNextAction(engine, s_elapsed));
    return created_objects;
  }
}
