package mapobject.shot;

import mapobject.MapObject;
import mapobject.MovableObject;
import mapobject.ephemeral.Explosion;
import mapobject.unit.Unit;
import pilot.ShotPilot;
import structure.Room;

import component.MapEngine;

public abstract class Shot extends MovableObject {
  protected MapObject source;

  public Shot(MapObject source, Room room, double x_loc, double y_loc, double direction) {
    super(new ShotPilot(), room, x_loc, y_loc, direction);
    this.source = source;
  }

  @Override
  public MapObject doNextStep(MapEngine engine, double s_elapsed) {
    // check for collision with a Unit
    for (Unit unit : room.getUnits()) {
      if (unit.equals(source)) {
        continue;
      }
      if (Math.abs(x_loc - unit.getX()) < unit.getRadius() &&
              Math.abs(y_loc - unit.getY()) < unit.getRadius()) {
        is_in_map = false;
        return new Explosion(room, x_loc, y_loc, 0.1, 1.0);
      }
    }

    // check for collision with a wall
    boolean location_accepted = doNextMovement(engine, s_elapsed);
    if (!location_accepted) {
      is_in_map = false;
    }
    return null;
  }
}
