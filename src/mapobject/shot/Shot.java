package mapobject.shot;

import mapobject.MapObject;
import mapobject.MovableObject;
import mapobject.ephemeral.Explosion;
import mapobject.unit.Unit;
import pilot.Pilot;
import pilot.ShotPilot;
import structure.Room;

import component.MapEngine;

public abstract class Shot extends MovableObject {
  public static final double EXPLOSION_RADIUS_DIVISOR = 30.0;
  public static final double EXPLOSION_TIME_DIVISOR = 3.0;

  protected final int damage;
  protected final MapObject source;

  public Shot(Pilot pilot, MapObject source, int damage, Room room, double x_loc, double y_loc,
          double direction) {
    super(0.0, pilot, room, x_loc, y_loc, direction);
    this.source = source;
    this.damage = damage;
  }

  public Shot(MapObject source, int damage, Room room, double x_loc, double y_loc, double direction) {
    this(new ShotPilot(), source, damage, room, x_loc, y_loc, direction);
  }

  public int getDamage() {
    return damage;
  }

  public MapObject getSource() {
    return source;
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    // check for collision with a Unit
    for (Unit unit : room.getUnits()) {
      if (unit.equals(source)) {
        continue;
      }
      if (Math.abs(x_loc - unit.getX()) < unit.getRadius() &&
              Math.abs(y_loc - unit.getY()) < unit.getRadius()) {
        unit.hitByShot(this);
        is_in_map = false;
        return new Explosion(room, x_loc, y_loc, damage / EXPLOSION_RADIUS_DIVISOR, damage /
                EXPLOSION_TIME_DIVISOR);
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
