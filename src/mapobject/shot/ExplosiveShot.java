package mapobject.shot;

import mapobject.MapObject;
import mapobject.MultipleObject;
import mapobject.unit.Unit;
import pilot.Pilot;
import structure.Room;

import component.MapEngine;

public abstract class ExplosiveShot extends Shot {
  public ExplosiveShot(Pilot pilot, MapObject source, int damage, Room room, double x_loc, double y_loc,
          double direction) {
    super(pilot, source, damage, room, x_loc, y_loc, direction);
  }

  public ExplosiveShot(MapObject source, int damage, Room room, double x_loc, double y_loc, double direction) {
    super(source, damage, room, x_loc, y_loc, direction);
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    MapObject created_object = super.doNextAction(engine, s_elapsed);
    if (is_detonated) {
      MultipleObject created_objects = new MultipleObject();
      created_objects.addObject(created_object);
      created_objects.addObject(createExplosion());
      return created_objects;
    }
    return created_object;
  }

  @Override
  public MapObject handleUnitCollision(MapEngine engine, Unit hit_unit) {
    playSound(engine, "weapons/explode1.wav");
    return super.handleUnitCollision(engine, hit_unit);
  }

  @Override
  public MapObject handleWallCollision(MapEngine engine) {
    super.handleWallCollision(engine);
    playSound(engine, "weapons/explode1.wav");
    return createExplosion();
  }
}
