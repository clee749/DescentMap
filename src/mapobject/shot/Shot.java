package mapobject.shot;

import java.util.HashMap;
import java.util.HashSet;

import mapobject.MapObject;
import mapobject.MovableObject;
import mapobject.ephemeral.Explosion;
import mapobject.unit.Pyro;
import mapobject.unit.Unit;
import pilot.Pilot;
import pilot.ShotPilot;
import structure.Room;

import common.ObjectType;
import component.MapEngine;

public abstract class Shot extends MovableObject {
  public static Integer getDamage(ObjectType type) {
    return DAMAGES.get(type);
  }

  private static final HashMap<ObjectType, Integer> DAMAGES = getDamages();
  private static final HashSet<ObjectType> SPLASH_DAMAGERS = getSplashDamagers();

  private static HashMap<ObjectType, Integer> getDamages() {
    HashMap<ObjectType, Integer> damages = new HashMap<ObjectType, Integer>();
    damages.put(ObjectType.LaserShot, 3);
    damages.put(ObjectType.FireballShot, 6);
    damages.put(ObjectType.PlasmaShot, 7);
    damages.put(ObjectType.ConcussionMissile, 16);
    damages.put(ObjectType.HomingMissile, 16);
    return damages;
  }

  private static HashSet<ObjectType> getSplashDamagers() {
    HashSet<ObjectType> damagers = new HashSet<ObjectType>();
    damagers.add(ObjectType.ConcussionMissile);
    damagers.add(ObjectType.HomingMissile);
    return damagers;
  }

  public static final double EXPLOSION_RADIUS_DIVISOR = 30.0;
  public static final double EXPLOSION_TIME_DIVISOR = 3.0;
  public static final double EXPLOSION_MAX_RADIUS = 0.2;
  public static final double EXPLOSION_MAX_TIME = 0.5;
  public static final double SPLASH_DAMAGE_RADIUS = 1.0;

  protected final int damage;
  protected final MapObject source;
  protected final boolean does_splash_damage;

  public Shot(Pilot pilot, MapObject source, int damage, Room room, double x_loc, double y_loc,
          double direction) {
    super(0.0, pilot, room, x_loc, y_loc, direction);
    this.source = source;
    this.damage = damage;
    does_splash_damage = SPLASH_DAMAGERS.contains(type);
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
    for (Unit unit : room.getRobots()) {
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
    if (does_splash_damage) {
      room.doSplashDamage(this, damage, SPLASH_DAMAGE_RADIUS, hit_unit);
    }
    return new Explosion(room, x_loc, y_loc,
            Math.min(damage / EXPLOSION_RADIUS_DIVISOR, EXPLOSION_MAX_RADIUS), Math.min(damage /
                    EXPLOSION_TIME_DIVISOR, EXPLOSION_MAX_TIME));
  }

  public MapObject handleWallCollision() {
    is_in_map = false;
    if (does_splash_damage) {
      room.doSplashDamage(this, damage, SPLASH_DAMAGE_RADIUS, null);
      return new Explosion(room, x_loc, y_loc, Math.min(damage / EXPLOSION_RADIUS_DIVISOR,
              EXPLOSION_MAX_RADIUS), Math.min(damage / EXPLOSION_TIME_DIVISOR, EXPLOSION_MAX_TIME));
    }
    return null;
  }
}
