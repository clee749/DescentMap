package mapobject.shot;

import mapobject.MapObject;
import mapobject.MovableObject;
import mapobject.ephemeral.Explosion;
import mapobject.unit.Pyro;
import mapobject.unit.Unit;
import pilot.Pilot;
import pilot.ShotPilot;
import structure.Room;

import component.MapEngine;

public abstract class Shot extends MovableObject {
  public static final double EXPLOSION_RADIUS_DIVISOR = 30.0;
  public static final double EXPLOSION_TIME_DIVISOR = 3.0;
  public static final double EXPLOSION_MAX_RADIUS = 0.2;
  public static final double EXPLOSION_MAX_TIME = 0.5;

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
    Unit hit_unit = checkForUnitCollisions();
    if (hit_unit != null) {
      return handleUnitCollision(hit_unit);
    }

    boolean location_accepted = doNextMovement(engine, s_elapsed);
    if (!location_accepted) {
      return handleWallCollision();
    }
    return null;
  }

  public Unit checkForUnitCollisions() {
    for (Pyro pyro : room.getPyros()) {
      if (hitsUnit(pyro)) {
        return pyro;
      }
    }
    for (Unit unit : room.getMechs()) {
      if (hitsUnit(unit)) {
        return unit;
      }
    }
    return null;
  }

  public boolean hitsUnit(Unit unit) {
    return !unit.equals(source) && Math.abs(x_loc - unit.getX()) < unit.getRadius() &&
            Math.abs(y_loc - unit.getY()) < unit.getRadius();
  }

  public MapObject handleUnitCollision(Unit hit_unit) {
    is_in_map = false;
    hit_unit.beDamaged(damage);
    return new Explosion(room, x_loc, y_loc,
            Math.min(damage / EXPLOSION_RADIUS_DIVISOR, EXPLOSION_MAX_RADIUS), Math.min(damage /
                    EXPLOSION_TIME_DIVISOR, EXPLOSION_MAX_TIME));
  }

  public MapObject handleWallCollision() {
    is_in_map = false;
    return null;
  }
}
