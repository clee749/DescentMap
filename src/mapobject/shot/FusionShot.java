package mapobject.shot;

import java.util.HashSet;

import mapobject.MapObject;
import mapobject.unit.Unit;
import structure.Room;

import common.ObjectType;
import component.MapEngine;

public class FusionShot extends Shot {
  private final HashSet<Unit> damaged_units;

  public FusionShot(MapObject source, int damage, Room room, double x_loc, double y_loc, double direction) {
    super(source, damage, room, x_loc, y_loc, direction);
    damaged_units = new HashSet<Unit>();
  }

  @Override
  public ObjectType getType() {
    return ObjectType.FusionShot;
  }

  @Override
  public MapObject handleUnitCollision(MapEngine engine, Unit hit_unit) {
    if (damaged_units.add(hit_unit)) {
      hit_unit.beDamaged(engine, damage, true);
      return createExplosion();
    }
    return null;
  }

  @Override
  public void detonate() {

  }
}
