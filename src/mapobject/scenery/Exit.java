package mapobject.scenery;

import mapobject.MapObject;
import structure.Room;

import common.ObjectType;
import component.MapEngine;

public class Exit extends Scenery {
  public Exit(Room room, double x_loc, double y_loc) {
    super(room, x_loc, y_loc);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Exit;
  }

  @Override
  public void planNextStep(double s_elapsed) {

  }

  @Override
  public MapObject doNextStep(MapEngine engine, double s_elapsed) {
    return null;
  }
}
