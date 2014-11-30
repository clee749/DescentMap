package mapobject.shot;

import java.util.HashMap;

import mapobject.MapObject;
import mapobject.MovableObject;
import mapobject.ephemeral.Explosion;
import mapobject.unit.Pyro;
import mapobject.unit.Unit;
import mapobject.unit.robot.Robot;
import pilot.Pilot;
import pilot.ShotPilot;
import structure.Room;
import util.MapUtils;

import common.ObjectType;
import common.RoomSide;
import component.MapEngine;

public abstract class Shot extends MovableObject {
  public static Integer getDamage(ObjectType type) {
    return DAMAGES.get(type);
  }

  private static final HashMap<ObjectType, Integer> DAMAGES = getDamages();

  private static HashMap<ObjectType, Integer> getDamages() {
    HashMap<ObjectType, Integer> damages = new HashMap<ObjectType, Integer>();
    damages.put(ObjectType.LaserShot, 3);
    damages.put(ObjectType.FireballShot, 6);
    damages.put(ObjectType.PlasmaShot, 7);
    damages.put(ObjectType.ConcussionMissile, 16);
    damages.put(ObjectType.HomingMissile, 16);
    damages.put(ObjectType.SmartMissile, 5);
    damages.put(ObjectType.SmartPlasma, 35);
    damages.put(ObjectType.FusionShot, 60);
    damages.put(ObjectType.MegaMissile, 201);
    return damages;
  }

  public static final double EXPLOSION_RADIUS_DIVISOR = 30.0;
  public static final double EXPLOSION_TIME_DIVISOR = 3.0;
  public static final double EXPLOSION_MAX_RADIUS = 0.2;
  public static final double EXPLOSION_MAX_TIME = 0.5;

  protected final int damage;
  protected final MapObject source;
  protected boolean is_detonated;

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

  public boolean isDetonated() {
    return is_detonated;
  }

  public void detonate() {
    is_detonated = true;
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    if (is_detonated) {
      is_in_map = false;
      return null;
    }

    boolean location_accepted = doNextMovement(engine, s_elapsed);
    if (!location_accepted) {
      return handleWallCollision(engine);
    }

    Unit hit_unit = checkForUnitCollisions();
    if (hit_unit != null) {
      return handleUnitCollision(engine, hit_unit);
    }

    return null;
  }

  public Unit checkForUnitCollisions() {
    Unit hit_unit = checkForUnitCollisions(room);
    if (hit_unit != null) {
      return hit_unit;
    }

    RoomSide close_neighbor_side = MapUtils.findRoomBorderSide(this, Unit.LARGEST_UNIT_RADIUS);
    if (close_neighbor_side != null) {
      Room neighbor = room.getNeighborInDirection(close_neighbor_side);
      if (neighbor != null) {
        hit_unit = checkForUnitCollisions(neighbor);
      }
    }

    return hit_unit;
  }

  public Unit checkForUnitCollisions(Room room) {
    for (Pyro pyro : room.getPyros()) {
      if (hitsUnit(pyro)) {
        return pyro;
      }
    }
    for (Robot robot : room.getRobots()) {
      if (hitsUnit(robot)) {
        return robot;
      }
    }
    return null;
  }

  public boolean hitsUnit(Unit unit) {
    return !unit.equals(source) && MapUtils.objectsIntersect(this, unit, unit.getRadius());
  }

  public MapObject handleUnitCollision(MapEngine engine, Unit hit_unit) {
    is_in_map = false;
    hit_unit.beDamaged(engine, damage, true);
    return createExplosion();
  }

  public Explosion createExplosion() {
    return new Explosion(room, x_loc, y_loc,
            Math.min(damage / EXPLOSION_RADIUS_DIVISOR, EXPLOSION_MAX_RADIUS), Math.min(damage /
                    EXPLOSION_TIME_DIVISOR, EXPLOSION_MAX_TIME));
  }

  public MapObject handleWallCollision(MapEngine engine) {
    is_in_map = false;
    return null;
  }
}
