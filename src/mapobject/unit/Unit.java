package mapobject.unit;

import java.awt.geom.Point2D;
import java.util.HashMap;

import mapobject.MapObject;
import mapobject.MovableObject;
import mapobject.MultipleObject;
import mapobject.ephemeral.Explosion;
import pilot.Pilot;
import pilot.PilotAction;
import structure.Room;
import structure.RoomConnection;
import util.MapUtils;

import common.DescentMapException;
import common.ObjectType;
import common.RoomSide;
import component.MapEngine;

public abstract class Unit extends MovableObject {
  public static Double getRadius(ObjectType type) {
    return RADII.get(type);
  }

  public static Integer getStartingShields(ObjectType type) {
    return STARTING_SHIELDS.get(type);
  }

  private static final HashMap<ObjectType, Double> RADII = getRadii();
  private static final HashMap<ObjectType, Double> CANNON_OFFSETS = getCannonOffsets();
  private static final HashMap<ObjectType, Integer> STARTING_SHIELDS = getStartingShields();

  private static HashMap<ObjectType, Double> getRadii() {
    HashMap<ObjectType, Double> radii = new HashMap<ObjectType, Double>();
    // 1m == 0.05
    radii.put(ObjectType.BabySpider, 0.12);
    radii.put(ObjectType.Bomber, 0.15);
    radii.put(ObjectType.Class1Drone, 0.15);
    radii.put(ObjectType.Class2Drone, 0.2);
    radii.put(ObjectType.SecondaryLifter, 0.2);
    radii.put(ObjectType.Pyro, 0.25);
    radii.put(ObjectType.LightHulk, 0.25);
    radii.put(ObjectType.PlatformLaser, 0.25);
    radii.put(ObjectType.PlatformMissile, 0.25);
    radii.put(ObjectType.DefenseRobot, 0.3);
    radii.put(ObjectType.HeavyHulk, 0.35);
    radii.put(ObjectType.MediumHulk, 0.35);
    radii.put(ObjectType.MediumHulkCloaked, 0.35);
    radii.put(ObjectType.HeavyDriller, 0.4);
    radii.put(ObjectType.Spider, 0.4);
    return radii;
  }

  private static HashMap<ObjectType, Double> getCannonOffsets() {
    HashMap<ObjectType, Double> offsets = new HashMap<ObjectType, Double>();
    offsets.put(ObjectType.BabySpider, 0.0);
    offsets.put(ObjectType.Bomber, 0.0);
    offsets.put(ObjectType.Class2Drone, 0.0);
    offsets.put(ObjectType.PlatformLaser, 0.0);
    offsets.put(ObjectType.PlatformMissile, 0.0);
    offsets.put(ObjectType.Spider, 0.13);
    offsets.put(ObjectType.SecondaryLifter, 0.4);
    offsets.put(ObjectType.Pyro, 0.525);
    offsets.put(ObjectType.LightHulk, 0.79);
    offsets.put(ObjectType.HeavyDriller, 0.81);
    offsets.put(ObjectType.HeavyHulk, 0.83);
    offsets.put(ObjectType.MediumHulk, 0.83);
    offsets.put(ObjectType.MediumHulkCloaked, 0.83);
    offsets.put(ObjectType.DefenseRobot, 0.84);
    offsets.put(ObjectType.Class1Drone, 0.86);
    return offsets;
  }

  private static HashMap<ObjectType, Integer> getStartingShields() {
    HashMap<ObjectType, Integer> shields = new HashMap<ObjectType, Integer>();
    shields.put(ObjectType.BabySpider, 8);
    shields.put(ObjectType.Bomber, 8);
    shields.put(ObjectType.Class1Drone, 8);
    shields.put(ObjectType.Class2Drone, 11);
    shields.put(ObjectType.SecondaryLifter, 20);
    shields.put(ObjectType.DefenseRobot, 23);
    shields.put(ObjectType.LightHulk, 23);
    shields.put(ObjectType.PlatformLaser, 23);
    shields.put(ObjectType.MediumHulk, 32);
    shields.put(ObjectType.MediumHulkCloaked, 32);
    shields.put(ObjectType.Spider, 35);
    shields.put(ObjectType.HeavyDriller, 47);
    shields.put(ObjectType.PlatformMissile, 47);
    shields.put(ObjectType.HeavyHulk, 98);
    shields.put(ObjectType.Pyro, 100);
    return shields;
  }

  public static final double DAMAGED_EXPLOSION_RADIUS = 0.1;
  public static final double DAMAGED_EXPLOSION_TIME = 1.0;
  public static final double MIN_TIME_BETWEEN_DAMAGED_EXPLOSIONS = 0.5;
  public static final double VISIBLE_TIME_AFTER_REVEAL = 1.0;
  public static final double TIME_FOR_ZERO_TO_MAX_SPEED = 0.5;
  public static final double TIME_FOR_MAX_TO_ZERO_SPEED = 1.0;
  public static final double PUSH_FRACTION_DIVIDEND = 0.02;
  public static final double EXPLOSION_RADIUS_MULTIPLIER = 1.1;
  public static final double EXPLOSION_MIN_TIME = 0.5;
  public static final double EXPLOSION_MAX_TIME = EXPLOSION_MIN_TIME * 2;
  public static final double EXPLOSION_TIME_DIVISOR = 3.0;

  protected final double cannon_offset;
  protected final int half_shields;
  protected double move_x_fraction;
  protected double move_y_fraction;
  protected double move_positive_direction;
  protected double strafe_x_fraction;
  protected double strafe_y_fraction;
  protected double strafe_positive_direction;
  protected double reload_time;
  protected double reload_time_left;
  protected boolean firing_cannon;
  protected int shields;
  protected boolean is_cloaked;
  protected boolean is_visible;
  protected double visible_time_left;
  protected boolean is_exploded;
  protected double exploding_time_left;

  public Unit(double radius, Pilot pilot, Room room, double x_loc, double y_loc, double direction) {
    super(radius, pilot, room, x_loc, y_loc, direction);
    cannon_offset = CANNON_OFFSETS.get(type) * radius;
    shields = STARTING_SHIELDS.get(type);
    half_shields = shields / 2;
    is_visible = true;
  }

  public int getShields() {
    return shields;
  }

  public boolean isCloaked() {
    return is_cloaked;
  }

  public boolean isVisible() {
    return is_visible;
  }

  public boolean isExploded() {
    return is_exploded;
  }

  @Override
  public void planNextAction(double s_elapsed) {
    if (!is_exploded) {
      handleCooldowns(s_elapsed);
      super.planNextAction(s_elapsed);
    }
    else {
      next_action = PilotAction.NO_ACTION;
    }
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    if (is_cloaked && is_visible && visible_time_left < 0.0) {
      is_visible = false;
    }
    if (shields < 0) {
      return handleDeath(engine, s_elapsed);
    }
    MultipleObject created_objects = new MultipleObject();
    int less_half_shields = half_shields - shields;
    if (less_half_shields > 0 &&
            Math.random() * MIN_TIME_BETWEEN_DAMAGED_EXPLOSIONS < less_half_shields / (half_shields + 1.0) *
                    s_elapsed) {
      created_objects.addObject(createDamagedExplosion());
    }
    created_objects.addObject(super.doNextAction(engine, s_elapsed));
    return created_objects;
  }

  @Override
  public void applyMoveAction(double s_elapsed) {
    switch (next_action.move) {
      case NONE:
        boolean do_move = false;
        if (move_x_fraction != 0.0) {
          move_x_fraction =
                  (move_x_fraction > 0.0 ? Math.max(move_x_fraction - s_elapsed / TIME_FOR_MAX_TO_ZERO_SPEED,
                          0.0) : Math.min(move_x_fraction + s_elapsed / TIME_FOR_MAX_TO_ZERO_SPEED, 0.0));
          do_move = true;
        }
        if (move_y_fraction != 0.0) {
          move_y_fraction =
                  (move_y_fraction > 0.0 ? Math.max(move_y_fraction - s_elapsed / TIME_FOR_MAX_TO_ZERO_SPEED,
                          0.0) : Math.min(move_y_fraction + s_elapsed / TIME_FOR_MAX_TO_ZERO_SPEED, 0.0));
          do_move = true;
        }
        if (!do_move) {
          return;
        }
        break;
      case FORWARD:
        move_positive_direction = direction;
        if (move_x_fraction < 1.0) {
          move_x_fraction = Math.min(move_x_fraction + s_elapsed / TIME_FOR_ZERO_TO_MAX_SPEED, 1.0);
        }
        if (move_y_fraction < 1.0) {
          move_y_fraction = Math.min(move_y_fraction + s_elapsed / TIME_FOR_ZERO_TO_MAX_SPEED, 1.0);
        }
        break;
      case BACKWARD:
        move_positive_direction = direction;
        if (move_x_fraction > -1.0) {
          move_x_fraction = Math.max(move_x_fraction - s_elapsed / TIME_FOR_ZERO_TO_MAX_SPEED, -1.0);
        }
        if (move_y_fraction > -1.0) {
          move_y_fraction = Math.max(move_y_fraction - s_elapsed / TIME_FOR_ZERO_TO_MAX_SPEED, -1.0);
        }
        break;
      default:
        throw new DescentMapException("Unexpected MoveDirection: " + next_action.move);
    }

    x_loc += move_x_fraction * move_speed * Math.cos(move_positive_direction) * s_elapsed;
    y_loc += move_y_fraction * move_speed * Math.sin(move_positive_direction) * s_elapsed;
  }

  @Override
  public void applyStrafeAction(double s_elapsed) {
    switch (next_action.strafe) {
      case NONE:
        boolean do_strafe = false;
        if (strafe_x_fraction != 0.0) {
          strafe_x_fraction =
                  (strafe_x_fraction > 0.0 ? Math.max(strafe_x_fraction - s_elapsed /
                          TIME_FOR_MAX_TO_ZERO_SPEED, 0.0) : Math.min(strafe_x_fraction + s_elapsed /
                          TIME_FOR_MAX_TO_ZERO_SPEED, 0.0));
          do_strafe = true;
        }
        if (strafe_y_fraction != 0.0) {
          strafe_y_fraction =
                  (strafe_y_fraction > 0.0 ? Math.max(strafe_y_fraction - s_elapsed /
                          TIME_FOR_MAX_TO_ZERO_SPEED, 0.0) : Math.min(strafe_y_fraction + s_elapsed /
                          TIME_FOR_MAX_TO_ZERO_SPEED, 0.0));
          do_strafe = true;
        }
        if (!do_strafe) {
          return;
        }
        break;
      case LEFT:
        strafe_positive_direction = direction;
        if (strafe_x_fraction > -1.0) {
          strafe_x_fraction = Math.max(strafe_x_fraction - s_elapsed / TIME_FOR_ZERO_TO_MAX_SPEED, -1.0);
        }
        if (strafe_y_fraction > -1.0) {
          strafe_y_fraction = Math.max(strafe_y_fraction - s_elapsed / TIME_FOR_ZERO_TO_MAX_SPEED, -1.0);
        }
        break;
      case RIGHT:
        strafe_positive_direction = direction;
        if (strafe_x_fraction < 1.0) {
          strafe_x_fraction = Math.min(strafe_x_fraction + s_elapsed / TIME_FOR_ZERO_TO_MAX_SPEED, 1.0);
        }
        if (strafe_y_fraction < 1.0) {
          strafe_y_fraction = Math.min(strafe_y_fraction + s_elapsed / TIME_FOR_ZERO_TO_MAX_SPEED, 1.0);
        }
        break;
      default:
        throw new DescentMapException("Unexpected StrafeDirection: " + next_action.strafe);
    }

    Point2D.Double strafe_dxdy =
            MapUtils.perpendicularVector(move_speed * s_elapsed, strafe_positive_direction);
    x_loc += strafe_x_fraction * strafe_dxdy.x;
    y_loc += strafe_y_fraction * strafe_dxdy.y;
  }

  @Override
  public void handleHittingWall(RoomSide wall_side) {
    super.handleHittingWall(wall_side);
    if (wall_side.equals(RoomSide.WEST) || wall_side.equals(RoomSide.EAST)) {
      move_x_fraction = 0.0;
      strafe_x_fraction = 0.0;
    }
    else {
      move_y_fraction = 0.0;
      strafe_y_fraction = 0.0;
    }
  }

  @Override
  public boolean handleHittingNeighborWall(RoomSide wall_side, RoomConnection connection_to_neighbor) {
    boolean location_accepted = super.handleHittingNeighborWall(wall_side, connection_to_neighbor);
    if (location_accepted) {
      return true;
    }
    if (wall_side.equals(RoomSide.NORTH) || wall_side.equals(RoomSide.SOUTH)) {
      move_x_fraction = 0.0;
      strafe_x_fraction = 0.0;
    }
    else {
      move_y_fraction = 0.0;
      strafe_y_fraction = 0.0;
    }
    return false;
  }

  public void setZeroVelocity() {
    move_x_fraction = 0.0;
    move_y_fraction = 0.0;
    strafe_x_fraction = 0.0;
    strafe_y_fraction = 0.0;
  }

  public boolean isCannonReloaded() {
    return reload_time_left < 0.0;
  }

  public void handleCooldowns(double s_elapsed) {
    reload_time_left -= s_elapsed;
    visible_time_left -= s_elapsed;
  }

  public void planToFireCannon() {
    firing_cannon = true;
    reload_time_left = reload_time;
  }

  public void beDamaged(MapEngine engine, int amount, boolean is_direct_weapon_hit) {
    shields -= amount;
    revealIfCloaked();
  }

  public Explosion createDamagedExplosion() {
    return new Explosion(room, x_loc + Math.random() * 2 * radius - radius, y_loc + Math.random() * 2 *
            radius - radius, DAMAGED_EXPLOSION_RADIUS, DAMAGED_EXPLOSION_TIME);
  }

  public void revealIfCloaked() {
    if (is_cloaked) {
      is_visible = true;
      visible_time_left = VISIBLE_TIME_AFTER_REVEAL;
    }
  }

  public void bePushed(MapEngine engine, double dx, double dy, double fraction) {
    double previous_x_loc = x_loc;
    double previous_y_loc = y_loc;
    x_loc += dx * fraction;
    y_loc += dy * fraction;
    setZeroVelocity();
    boundInsideAndUpdateRoom(engine, previous_x_loc, previous_y_loc);
  }

  public void bePushed(MapEngine engine, double dx, double dy) {
    bePushed(engine, dx, dy, PUSH_FRACTION_DIVIDEND / radius);
  }

  public MapObject handleDeath(MapEngine engine, double s_elapsed) {
    if (!is_exploded) {
      is_exploded = true;
      double explosion_time = Math.random() * (EXPLOSION_MAX_TIME - EXPLOSION_MIN_TIME) + EXPLOSION_MIN_TIME;
      exploding_time_left = explosion_time / EXPLOSION_TIME_DIVISOR;
      playSound(engine, "weapons/explode1.wav");
      return new Explosion(room, x_loc, y_loc, radius * EXPLOSION_RADIUS_MULTIPLIER, explosion_time);
    }
    if (exploding_time_left < 0.0) {
      is_in_map = false;
      return releasePowerups();
    }
    exploding_time_left -= s_elapsed;
    return null;
  }

  public abstract MapObject releasePowerups();
}
