package mapobject.scenery;

import mapobject.MapObject;
import structure.Room;

import common.ObjectType;
import component.MapEngine;

public class Entrance extends Scenery {
  public Entrance(Room room, double x_loc, double y_loc) {
    super(room, x_loc, y_loc);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Entrance;
  }

  @Override
  public void planNextAction(double s_elapsed) {

  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    return null;
  }
}
