package mapobject.shot;

import mapobject.MapObject;
import mapobject.MovableObject;
import pilot.ShotPilot;
import structure.Room;

import component.MapEngine;

public abstract class Shot extends MovableObject {

  public Shot(MapObject source, Room room, double x_loc, double y_loc, double direction) {
    super(new ShotPilot(), room, x_loc, y_loc, direction);
  }

  @Override
  public MapObject doNextStep(MapEngine engine, double s_elapsed) {
    boolean location_accepted = doNextMovement(engine, s_elapsed);
    if (!location_accepted) {
      is_in_map = false;
    }
    return null;
  }
}
