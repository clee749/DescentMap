package mapobject.unit;

import java.awt.Image;
import java.awt.geom.Point2D;
import java.util.HashMap;

import mapobject.MapObject;
import mapobject.MovableObject;
import mapobject.MultipleObject;
import mapobject.ephemeral.Explosion;
import pilot.Pilot;
import pilot.PilotAction;
import pilot.TurnDirection;
import resource.ImageHandler;
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
    radii.put(ObjectType.Gopher, 0.15);
    radii.put(ObjectType.Class2Drone, 0.2);
    radii.put(ObjectType.SecondaryLifter, 0.2);
    radii.put(ObjectType.Pyro, 0.25);
    radii.put(ObjectType.LightHulk, 0.25);
    radii.put(ObjectType.MediumLifter, 0.25);
    radii.put(ObjectType.PlatformLaser, 0.25);
    radii.put(ObjectType.PlatformMissile, 0.25);
    radii.put(ObjectType.DefenseRobot, 0.3);
    radii.put(ObjectType.AdvancedLifter, 0.35);
    radii.put(ObjectType.HeavyHulk, 0.35);
    radii.put(ObjectType.MediumHulk, 0.35);
    radii.put(ObjectType.MediumHulkCloaked, 0.35);
    radii.put(ObjectType.HeavyDriller, 0.4);
    radii.put(ObjectType.Spider, 0.4);
    radii.put(ObjectType.MiniBoss, 0.55);
    radii.put(ObjectType.BigGuy, 1.4);
    radii.put(ObjectType.FinalBoss, 1.6);
    return radii;
  }

  private static HashMap<ObjectType, Double> getCannonOffsets() {
    HashMap<ObjectType, Double> offsets = new HashMap<ObjectType, Double>();
    offsets.put(ObjectType.BabySpider, 0.0);
    offsets.put(ObjectType.Bomber, 0.0);
    offsets.put(ObjectType.Class2Drone, 0.0);
    offsets.put(ObjectType.Gopher, 0.0);
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
    offsets.put(ObjectType.MiniBoss, 0.83);
    offsets.put(ObjectType.BigGuy, 0.83);
    offsets.put(ObjectType.FinalBoss, 0.83);
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
    shields.put(ObjectType.MediumLifter, 26);
    shields.put(ObjectType.AdvancedLifter, 32);
    shields.put(ObjectType.MediumHulk, 32);
    shields.put(ObjectType.MediumHulkCloaked, 32);
    shields.put(ObjectType.Spider, 35);
    shields.put(ObjectType.Gopher, 44);
    shields.put(ObjectType.HeavyDriller, 47);
    shields.put(ObjectType.PlatformMissile, 47);
    shields.put(ObjectType.HeavyHulk, 98);
    shields.put(ObjectType.Pyro, 100);
    shields.put(ObjectType.MiniBoss, 122);
    shields.put(ObjectType.BigGuy, 999);
    shields.put(ObjectType.FinalBoss, 999);
    return shields;
  }

  public static final double LARGEST_UNIT_RADIUS = RADII.get(ObjectType.MiniBoss);
  public static final double DAMAGED_EXPLOSION_RADIUS = 0.1;
  public static final double DAMAGED_EXPLOSION_TIME = 1.0;
  public static final double MIN_TIME_BETWEEN_DAMAGED_EXPLOSIONS = 0.5;
  public static final double VISIBLE_TIME_AFTER_REVEAL = 1.0;
  public static final double SPEED_INCREASE_PER_SECOND = 2.0;
  public static final double SPEED_DECREASE_PER_SECOND = 1.0;
  public static final int HOSTILE_COLLISION_BASE_DAMAGE = 1;
  public static final double PUSH_FRACTION_DIVIDEND = 0.02;
  public static final double EXPLOSION_RADIUS_MULTIPLIER = 1.1;
  public static final double EXPLOSION_MIN_TIME = 0.5;
  public static final double EXPLOSION_MAX_TIME = EXPLOSION_MIN_TIME * 2;
  public static final double EXPLOSION_TIME_DIVISOR = 3.0;
  public static final double DEATH_SPIN_TIME = 5.0;
  public static final double DEATH_SPIN_MOVE_SPEED_DIVISOR = 2.0;
  public static final double DEATH_SPIN_TURN_SPEED_MULTIPLIER = 1.5;
  public static final double MAX_DEATH_SPIN_DAMAGE_PERCENT = 0.5;

  protected final double cannon_offset;
  protected final int half_shields;
  protected double move_x_velocity;
  protected double move_y_velocity;
  protected double strafe_x_velocity;
  protected double strafe_y_velocity;
  protected double reload_time;
  protected double reload_time_left;
  protected boolean firing_cannon;
  protected int shields;
  protected boolean is_cloaked;
  protected boolean is_visible;
  protected double visible_time_left;
  protected boolean is_exploded;
  protected double exploding_time_left;
  protected boolean has_death_spin;
  protected TurnDirection previous_turn;
  protected boolean death_spin_started;
  protected double death_spin_time_left;
  protected double death_spin_direction;
  protected double death_spin_delta_direction;
  protected double max_death_spin_damage_taken;

  public Unit(double radius, Pilot pilot, Room room, double x_loc, double y_loc, double direction) {
    super(radius, pilot, room, x_loc, y_loc, direction);
    Double raw_cannon_offset = CANNON_OFFSETS.get(type);
    cannon_offset = (raw_cannon_offset != null ? CANNON_OFFSETS.get(type) * radius : 0.0);
    shields = STARTING_SHIELDS.get(type);
    half_shields = shields / 2;
    is_visible = true;
    previous_turn = TurnDirection.NONE;
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

  public boolean isHomable() {
    return !is_cloaked;
  }

  public boolean isExploded() {
    return is_exploded;
  }

  public void enableDeathSpin() {
    has_death_spin = true;
    max_death_spin_damage_taken = shields * MAX_DEATH_SPIN_DAMAGE_PERCENT;
  }

  @Override
  public Image getImage(ImageHandler images) {
    if (!has_death_spin || shields >= 0) {
      return super.getImage(images);
    }
    return images.getImage(image_name, death_spin_direction);
  }

  @Override
  public void planNextAction(double s_elapsed) {
    if (shields < 0) {
      if (has_death_spin) {
        handleCooldowns(s_elapsed);
        next_action = PilotAction.MOVE_FORWARD;
      }
      else {
        next_action = PilotAction.NO_ACTION;
      }
    }
    else {
      handleCooldowns(s_elapsed);
      super.planNextAction(s_elapsed);
    }
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    if (is_cloaked && is_visible && visible_time_left < 0.0) {
      is_visible = false;
    }
    if (shields < 0) {
      return (has_death_spin ? doNextDeathSpinAction(engine, s_elapsed) : handleDeath(engine, s_elapsed));
    }
    previous_turn = (next_action != null ? next_action.turn : TurnDirection.NONE);
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

  public MapObject doNextDeathSpinAction(MapEngine engine, double s_elapsed) {
    if (!death_spin_started) {
      death_spin_started = true;
      death_spin_direction = direction;
      death_spin_time_left = DEATH_SPIN_TIME;
      move_speed /= DEATH_SPIN_MOVE_SPEED_DIVISOR;
      death_spin_delta_direction = turn_speed * DEATH_SPIN_TURN_SPEED_MULTIPLIER;
      if (previous_turn.equals(TurnDirection.CLOCKWISE) ||
              (previous_turn.equals(TurnDirection.NONE) && Math.random() < 0.5)) {
        death_spin_delta_direction *= -1;
      }
    }
    else {
      death_spin_direction =
              MapUtils.normalizeAngle(death_spin_direction + death_spin_delta_direction * s_elapsed);
      if (death_spin_time_left < 0.0) {
        MapObject created_object = handleDeath(engine, s_elapsed);
        return created_object;
      }
      if (!doNextMovement(engine, s_elapsed)) {
        death_spin_time_left = 0.0;
      }
      death_spin_time_left -= s_elapsed;
    }
    if (shields < -max_death_spin_damage_taken) {
      death_spin_time_left = -1.0;
    }
    if (Math.random() * MIN_TIME_BETWEEN_DAMAGED_EXPLOSIONS < s_elapsed) {
      playSound(engine, "effects/explode2.wav");
    }
    return createDamagedExplosion();
  }

  @Override
  public void applyMovementActions(MapEngine engine, double s_elapsed) {
    super.applyMovementActions(engine, s_elapsed);
    x_loc += (move_x_velocity + strafe_x_velocity) * s_elapsed;
    y_loc += (move_y_velocity + strafe_y_velocity) * s_elapsed;
  }

  @Override
  public void applyMoveAction(double s_elapsed) {
    switch (next_action.move) {
      case NONE:
        double move_velocity = Math.hypot(move_x_velocity, move_y_velocity);
        double delta = s_elapsed * SPEED_DECREASE_PER_SECOND * move_x_velocity / move_velocity;
        if (Math.abs(move_x_velocity) > Math.abs(delta)) {
          move_x_velocity -= delta;
        }
        else {
          move_x_velocity = 0.0;
        }
        delta = s_elapsed * SPEED_DECREASE_PER_SECOND * move_y_velocity / move_velocity;
        if (Math.abs(move_y_velocity) > Math.abs(delta)) {
          move_y_velocity -= delta;
        }
        else {
          move_y_velocity = 0.0;
        }
        break;
      case FORWARD:
        double max = move_speed * Math.cos(direction);
        move_x_velocity += SPEED_INCREASE_PER_SECOND * Math.cos(direction) * s_elapsed;
        if (Math.abs(move_x_velocity) > Math.abs(max)) {
          move_x_velocity = max;
        }
        max = move_speed * Math.sin(direction);
        move_y_velocity += SPEED_INCREASE_PER_SECOND * Math.sin(direction) * s_elapsed;
        if (Math.abs(move_y_velocity) > Math.abs(max)) {
          move_y_velocity = max;
        }
        break;
      case BACKWARD:
        max = move_speed * Math.cos(direction);
        move_x_velocity -= SPEED_INCREASE_PER_SECOND * Math.cos(direction) * s_elapsed;
        if (Math.abs(move_x_velocity) > Math.abs(max)) {
          move_x_velocity = -max;
        }
        max = move_speed * Math.sin(direction);
        move_y_velocity -= SPEED_INCREASE_PER_SECOND * Math.sin(direction) * s_elapsed;
        if (Math.abs(move_y_velocity) > Math.abs(max)) {
          move_y_velocity = -max;
        }
        break;
      default:
        throw new DescentMapException("Unexpected MoveDirection: " + next_action.move);
    }
  }

  @Override
  public void applyStrafeAction(double s_elapsed) {
    switch (next_action.strafe) {
      case NONE:
        double strafe_velocity = Math.hypot(strafe_x_velocity, strafe_y_velocity);
        double delta = s_elapsed * SPEED_DECREASE_PER_SECOND * strafe_x_velocity / strafe_velocity;
        if (Math.abs(strafe_x_velocity) > Math.abs(delta)) {
          strafe_x_velocity -= delta;
        }
        else {
          strafe_x_velocity = 0.0;
        }
        delta = s_elapsed * SPEED_DECREASE_PER_SECOND * strafe_y_velocity / strafe_velocity;
        if (Math.abs(strafe_y_velocity) > Math.abs(delta)) {
          strafe_y_velocity -= delta;
        }
        else {
          strafe_y_velocity = 0.0;
        }
        break;
      case LEFT:
        Point2D.Double strafe_dxdy = MapUtils.perpendicularVector(1.0, direction);
        double max = move_speed * strafe_dxdy.x;
        strafe_x_velocity -= SPEED_INCREASE_PER_SECOND * strafe_dxdy.x * s_elapsed;
        if (Math.abs(strafe_x_velocity) > Math.abs(max)) {
          strafe_x_velocity = -max;
        }
        max = move_speed * strafe_dxdy.y;
        strafe_y_velocity -= SPEED_INCREASE_PER_SECOND * strafe_dxdy.y * s_elapsed;
        if (Math.abs(strafe_y_velocity) > Math.abs(max)) {
          strafe_y_velocity = -max;
        }
        break;
      case RIGHT:
        strafe_dxdy = MapUtils.perpendicularVector(1.0, direction);
        max = move_speed * strafe_dxdy.x;
        strafe_x_velocity += SPEED_INCREASE_PER_SECOND * strafe_dxdy.x * s_elapsed;
        if (Math.abs(strafe_x_velocity) > Math.abs(max)) {
          strafe_x_velocity = max;
        }
        max = move_speed * strafe_dxdy.y;
        strafe_y_velocity += SPEED_INCREASE_PER_SECOND * strafe_dxdy.y * s_elapsed;
        if (Math.abs(strafe_y_velocity) > Math.abs(max)) {
          strafe_y_velocity = max;
        }
        break;
      default:
        throw new DescentMapException("Unexpected StrafeDirection: " + next_action.strafe);
    }
  }

  @Override
  public void handleHittingWall(RoomSide wall_side) {
    super.handleHittingWall(wall_side);
    if (wall_side.equals(RoomSide.WEST) || wall_side.equals(RoomSide.EAST)) {
      move_x_velocity = 0.0;
      strafe_x_velocity = 0.0;
    }
    else {
      move_y_velocity = 0.0;
      strafe_y_velocity = 0.0;
    }
  }

  @Override
  public boolean handleHittingNeighborWall(RoomSide wall_side, RoomConnection connection_to_neighbor) {
    boolean location_accepted = super.handleHittingNeighborWall(wall_side, connection_to_neighbor);
    if (location_accepted) {
      return true;
    }
    if (wall_side.equals(RoomSide.NORTH) || wall_side.equals(RoomSide.SOUTH)) {
      move_x_velocity = 0.0;
      strafe_x_velocity = 0.0;
    }
    else {
      move_y_velocity = 0.0;
      strafe_y_velocity = 0.0;
    }
    return false;
  }

  public void setZeroVelocity() {
    move_x_velocity = 0.0;
    move_y_velocity = 0.0;
    strafe_x_velocity = 0.0;
    strafe_y_velocity = 0.0;
  }

  public boolean isCannonReloaded() {
    return reload_time_left < 0.0;
  }

  public void handleCooldowns(double s_elapsed) {
    reload_time_left -= s_elapsed;
    if (shields >= 0) {
      visible_time_left -= s_elapsed;
    }
  }

  public void planToFireCannon() {
    firing_cannon = true;
    reload_time_left = reload_time;
  }

  public void beDamaged(MapEngine engine, int amount, boolean play_weapon_hit_sound) {
    shields -= amount;
    revealIfCloaked();
    if (play_weapon_hit_sound) {
      playWeaponHitSound(engine);
    }
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

  public abstract void playWeaponHitSound(MapEngine engine);
}
