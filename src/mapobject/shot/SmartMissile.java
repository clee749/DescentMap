package mapobject.shot;

import mapobject.MapObject;
import mapobject.MultipleObject;
import structure.Room;
import util.MapUtils;

import common.ObjectType;
import component.MapEngine;

public class SmartMissile extends Missile {
  public static final int NUM_SMART_PLASMAS = 6;

  private final int smart_plasma_damage;

  public SmartMissile(MapObject source, int damage, int smart_plasma_damage, Room room, double x_loc,
          double y_loc, double direction) {
    super(source, damage, room, x_loc, y_loc, direction);
    this.smart_plasma_damage = smart_plasma_damage;
  }

  @Override
  public ObjectType getType() {
    return ObjectType.SmartMissile;
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    double previous_x_loc = x_loc;
    double previous_y_loc = y_loc;
    MapObject created_object = super.doNextAction(engine, s_elapsed);
    if (!is_in_map || is_detonated) {
      MultipleObject created_objects = new MultipleObject();
      created_objects.addObject(created_object);
      double placement_x_range = previous_x_loc - x_loc;
      double placement_y_range = previous_y_loc - y_loc;
      for (int count = 0; count < NUM_SMART_PLASMAS; ++count) {
        created_objects
                .addObject(new SmartPlasma(source, smart_plasma_damage, room, x_loc + Math.random() *
                        placement_x_range, y_loc + Math.random() * placement_y_range, Math.random() *
                        MapUtils.TWO_PI));
      }
      playSound(engine, "weapons/laser06.wav");
      return created_objects;
    }
    return created_object;
  }
}
