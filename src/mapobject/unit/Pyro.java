package mapobject.unit;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.HashMap;

import mapobject.MapObject;
import mapobject.MultipleObject;
import mapobject.ephemeral.Explosion;
import mapobject.powerup.ConcussionPack;
import mapobject.powerup.Energy;
import mapobject.powerup.Shield;
import mapobject.shot.Shot;
import pilot.Pilot;
import pilot.PilotAction;
import pilot.PyroPilot;
import pilot.TurnDirection;
import pyro.PyroPrimaryCannon;
import pyro.PyroSecondaryCannon;
import structure.Room;
import util.MapUtils;
import util.PowerupFactory;
import cannon.Cannon;
import cannon.ConcussionMissileCannon;
import cannon.LaserCannon;

import common.ObjectType;
import component.MapEngine;

import external.ImageHandler;

class PrimaryCannonInfo {
  public final double reload_time;
  public final double energy_cost;

  public PrimaryCannonInfo(double reload_time, double energy_cost) {
    this.reload_time = reload_time;
    this.energy_cost = energy_cost;
  }
}


public class Pyro extends Unit {
  private static final HashMap<PyroPrimaryCannon, PrimaryCannonInfo> PRIMARY_CANNON_INFOS =
          getPrimaryCannonInfos();
  private static final HashMap<PyroSecondaryCannon, Double> SECONDARY_RELOAD_TIMES =
          getSecondaryReloadTimes();

  private static HashMap<PyroPrimaryCannon, PrimaryCannonInfo> getPrimaryCannonInfos() {
    HashMap<PyroPrimaryCannon, PrimaryCannonInfo> infos = new HashMap<PyroPrimaryCannon, PrimaryCannonInfo>();
    infos.put(PyroPrimaryCannon.LASER, new PrimaryCannonInfo(0.25, 0.5));
    infos.put(PyroPrimaryCannon.PLASMA, new PrimaryCannonInfo(0.15, 0.5));
    return infos;
  }

  private static HashMap<PyroSecondaryCannon, Double> getSecondaryReloadTimes() {
    HashMap<PyroSecondaryCannon, Double> times = new HashMap<PyroSecondaryCannon, Double>();
    times.put(PyroSecondaryCannon.CONCUSSION_MISSILE, 0.5);
    return times;
  }

  public static final double OUTER_CANNON_OFFSET_FRACTION = 0.8;
  public static final double CANNON_FORWARD_OFFSET_FRACTION = 0.2;
  public static final double MISSILE_OFFSET_FRACTION = 0.2;
  public static final int MAX_SHIELDS = 200;
  public static final int MIN_STARTING_ENERGY = 100;
  public static final int MAX_ENERGY = 200;
  public static final double CANNON_SWITCH_TIME = 1.0;
  public static final int MIN_STARTING_CONCUSSION_MISSILES = 3;
  public static final int MAX_CONCUSSION_MISSILES = 20;
  public static final double DEATH_SPIN_TIME = 5.0;
  public static final double DEATH_SPIN_MOVE_SPEED_DIVISOR = 2.0;
  public static final double DEATH_SPIN_TURN_SPEED_MULTIPLIER = 1.5;
  public static final double DEATH_SPIN_EXPLOSION_RADIUS = 0.1;
  public static final double DEATH_SPIN_EXPLOSION_TIME = 1.0;
  public static final double DEATH_SPLASH_DAMAGE_RADIUS = 1.0;
  public static final int MIN_DEATH_SPLASH_DAMAGE = 10;
  public static final int MAX_DEATH_SPLASH_DAMAGE = 50;
  public static final Color SELECTED_WEAPON_COLOR = Color.green;
  public static final Color UNSELECTED_WEAPON_COLOR = Color.gray;
  public static final Color WEAPON_AMOUNT_COLOR = Color.red;

  private final double outer_cannon_offset;
  private final double cannon_forward_offset;
  private final double missile_offset;
  private double energy;
  private boolean has_quad_lasers;
  private final Cannon[] primary_cannons;
  private Cannon selected_primary_cannon;
  private double primary_energy_cost;
  private final double secondary_reload_time;
  private double secondary_reload_time_left;
  private boolean firing_secondary;
  private int missile_side;
  private final Cannon selected_secondary_cannon;
  private int num_concussion_missiles;
  private boolean death_spin_started;
  private double death_spin_time_left;
  private double death_spin_direction;
  private double death_spin_delta_direction;

  public Pyro(Pilot pilot, Room room, double x_loc, double y_loc, double direction) {
    super(Unit.getRadius(ObjectType.Pyro), pilot, room, x_loc, y_loc, direction);
    outer_cannon_offset = OUTER_CANNON_OFFSET_FRACTION * radius;
    cannon_forward_offset = CANNON_FORWARD_OFFSET_FRACTION * radius;
    missile_offset = MISSILE_OFFSET_FRACTION * radius;
    energy = MIN_STARTING_ENERGY;
    selected_primary_cannon = new LaserCannon(Shot.getDamage(ObjectType.LaserShot), 1);
    primary_cannons = new Cannon[PyroPrimaryCannon.values().length];
    primary_cannons[PyroPrimaryCannon.LASER.ordinal()] = selected_primary_cannon;
    setPrimaryCannonInfo(PyroPrimaryCannon.LASER);
    num_concussion_missiles = MIN_STARTING_CONCUSSION_MISSILES;
    missile_side = (int) (Math.random() * 2);
    selected_secondary_cannon = new ConcussionMissileCannon(Shot.getDamage(ObjectType.ConcussionMissile));
    secondary_reload_time = SECONDARY_RELOAD_TIMES.get(PyroSecondaryCannon.CONCUSSION_MISSILE);
    ((PyroPilot) pilot).startPilot();
  }

  public Pyro(Room room, double x_loc, double y_loc, double direction) {
    this(new PyroPilot(), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Pyro;
  }

  public double getEnergy() {
    return energy;
  }

  public boolean hasQuadLasers() {
    return has_quad_lasers;
  }

  public LaserCannon getLaserCannon() {
    return ((LaserCannon) primary_cannons[PyroPrimaryCannon.LASER.ordinal()]);
  }

  public int getLaserLevel() {
    return getLaserCannon().getLevel();
  }

  public boolean hasPrimaryCannon(PyroPrimaryCannon cannon_type) {
    return primary_cannons[cannon_type.ordinal()] != null;
  }

  public void setPrimaryCannonInfo(PyroPrimaryCannon primary_cannon_type) {
    PrimaryCannonInfo cannon_info = PRIMARY_CANNON_INFOS.get(primary_cannon_type);
    reload_time = cannon_info.reload_time;
    primary_energy_cost = cannon_info.energy_cost;
  }

  public int getNumConcussionMissiles() {
    return num_concussion_missiles;
  }

  @Override
  public Image getImage(ImageHandler images) {
    if (shields >= 0) {
      return super.getImage(images);
    }
    return images.getImage(image_name, death_spin_direction);
  }

  @Override
  public void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell) {
    super.paint(g, images, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    Point target_pixel =
            MapUtils.coordsToPixel(pilot.getTargetX(), pilot.getTargetY(), ref_cell, ref_cell_nw_pixel,
                    pixels_per_cell);
    g.setColor(Color.yellow);
    g.drawRect(target_pixel.x - 1, target_pixel.y - 1, 2, 2);
  }

  public void paintInfo(Graphics2D g) {
    g.setColor(Color.yellow);
    g.drawString("Energy: " + (int) energy, 10, 20);
    g.setColor(Color.cyan);
    g.drawString("Shield: " + shields, 10, 40);
    int text_offset =
            paintCannonInfo(g, PyroPrimaryCannon.LASER, 70, "Laser Lvl: " + getLaserLevel() +
                    (has_quad_lasers ? " Quad" : ""));
    text_offset = paintCannonInfo(g, PyroPrimaryCannon.PLASMA, text_offset, "Plasma");
    text_offset += 10;
    if (num_concussion_missiles > 0) {
      paintSecondaryWeaponInfo(g, text_offset, "Concsn Missile: ", num_concussion_missiles);
      text_offset += 20;
    }
  }

  public int paintCannonInfo(Graphics2D g, PyroPrimaryCannon cannon_type, int text_offset, String cannon_text) {
    Cannon cannon = primary_cannons[cannon_type.ordinal()];
    if (cannon != null) {
      if (cannon.equals(selected_primary_cannon)) {
        g.setColor(SELECTED_WEAPON_COLOR);
      }
      else {
        g.setColor(UNSELECTED_WEAPON_COLOR);
      }
      g.drawString(cannon_text, 10, text_offset);
      return text_offset + 20;
    }
    return text_offset;
  }

  public void paintSecondaryWeaponInfo(Graphics2D g, int text_offset, String weapon_text, int num) {
    FontMetrics metrics = g.getFontMetrics();
    g.setColor(SELECTED_WEAPON_COLOR);
    g.drawString(weapon_text, 10, text_offset);
    g.setColor(WEAPON_AMOUNT_COLOR);
    g.drawString(String.format("%03d", num), 10 + metrics.stringWidth(weapon_text), text_offset);
  }

  @Override
  public void planNextAction(double s_elapsed) {
    if (shields < 0) {
      next_action = PilotAction.MOVE_FORWARD;
      return;
    }
    super.planNextAction(s_elapsed);
    if (next_action.fire_cannon && reload_time_left < 0.0) {
      planToFireCannon();
    }
    else {
      handleCannonReload(s_elapsed);
    }
    if (next_action.fire_secondary && secondary_reload_time_left < 0.0) {
      planToFireSecondary();
    }
    else {
      handleSecondaryReload(s_elapsed);
    }
  }

  public void planToFireSecondary() {
    firing_secondary = true;
    secondary_reload_time_left = secondary_reload_time;
  }

  public void handleSecondaryReload(double s_elapsed) {
    secondary_reload_time_left -= s_elapsed;
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    if (shields < 0) {
      return doNextDeathSpinAction(engine, s_elapsed);
    }
    MultipleObject created_objects = new MultipleObject();
    created_objects.addObject(super.doNextAction(engine, s_elapsed));
    if (firing_cannon) {
      firing_cannon = false;
      created_objects.addObject(fireCannon());
    }
    if (firing_secondary) {
      firing_secondary = false;
      created_objects.addObject(fireSecondary());
    }
    return created_objects;
  }

  public MapObject doNextDeathSpinAction(MapEngine engine, double s_elapsed) {
    if (!death_spin_started) {
      death_spin_started = true;
      death_spin_direction = direction;
      death_spin_time_left = DEATH_SPIN_TIME;
      move_speed /= DEATH_SPIN_MOVE_SPEED_DIVISOR;
      death_spin_delta_direction = turn_speed * DEATH_SPIN_TURN_SPEED_MULTIPLIER;
      if (next_action.turn.equals(TurnDirection.CLOCKWISE) ||
              (next_action.turn.equals(TurnDirection.NONE) && Math.random() < 0.5)) {
        death_spin_delta_direction *= -1;
      }
    }
    else {
      death_spin_direction =
              MapUtils.normalizeAngle(death_spin_direction + death_spin_delta_direction * s_elapsed);
      if (death_spin_time_left < 0.0) {
        MapObject created_object = handleDeath(s_elapsed);
        if (!is_in_map) {
          room.doSplashDamage(this, Math.max(MAX_DEATH_SPLASH_DAMAGE + shields, MIN_DEATH_SPLASH_DAMAGE),
                  DEATH_SPLASH_DAMAGE_RADIUS, this);
          ((PyroPilot) pilot).prepareForRespawn();
          engine.respawnPyroAfterDeath(this);
        }
        return created_object;
      }
      if (!doNextMovement(engine, s_elapsed)) {
        death_spin_time_left = 0.0;
      }
      death_spin_time_left -= s_elapsed;
    }
    return new Explosion(room, x_loc + Math.random() * 2 * radius - radius, y_loc + Math.random() * 2 *
            radius - radius, DEATH_SPIN_EXPLOSION_RADIUS, DEATH_SPIN_EXPLOSION_TIME);
  }

  @Override
  public MapObject fireCannon() {
    if (energy < primary_energy_cost) {
      return null;
    }
    MultipleObject shots = new MultipleObject();
    Point2D.Double abs_offset = MapUtils.perpendicularVector(cannon_offset, direction);
    shots.addObject(selected_primary_cannon.fireCannon(this, room, x_loc + abs_offset.x,
            y_loc + abs_offset.y, direction));
    shots.addObject(selected_primary_cannon.fireCannon(this, room, x_loc - abs_offset.x,
            y_loc - abs_offset.y, direction));
    if (has_quad_lasers && selected_primary_cannon instanceof LaserCannon) {
      abs_offset = MapUtils.perpendicularVector(outer_cannon_offset, direction);
      double x_forward_abs_offset = Math.cos(direction) * cannon_forward_offset;
      double y_forward_abs_offset = Math.sin(direction) * cannon_forward_offset;
      shots.addObject(selected_primary_cannon.fireCannon(this, room, x_loc + abs_offset.x +
              x_forward_abs_offset, y_loc + abs_offset.y + y_forward_abs_offset, direction));
      shots.addObject(selected_primary_cannon.fireCannon(this, room, x_loc - abs_offset.x +
              x_forward_abs_offset, y_loc - abs_offset.y + y_forward_abs_offset, direction));
    }
    energy -= primary_energy_cost;
    return shots;
  }

  public MapObject fireSecondary() {
    if (num_concussion_missiles < 1) {
      return null;
    }
    --num_concussion_missiles;
    ++missile_side;
    Point2D.Double abs_offset = MapUtils.perpendicularVector(missile_offset, direction);
    if (missile_side % 2 == 0) {
      return selected_secondary_cannon.fireCannon(this, room, x_loc + abs_offset.x, y_loc + abs_offset.y,
              direction);
    }
    return selected_secondary_cannon.fireCannon(this, room, x_loc - abs_offset.x, y_loc - abs_offset.y,
            direction);
  }

  @Override
  public MapObject releasePowerups() {
    MultipleObject powerups = new MultipleObject();
    powerups.addObject(PowerupFactory.newPowerup(ObjectType.Shield, room, x_loc, y_loc));
    shields -= Shield.SHIELD_AMOUNT;
    if (energy >= Energy.ENERGY_AMOUNT) {
      powerups.addObject(PowerupFactory.newPowerup(ObjectType.Energy, room, x_loc, y_loc));
      energy -= Energy.ENERGY_AMOUNT;
    }
    if (has_quad_lasers) {
      powerups.addObject(PowerupFactory.newPowerup(ObjectType.QuadLasers, room, x_loc, y_loc));
    }
    if (getLaserCannon().getLevel() > 1) {
      powerups.addObject(PowerupFactory.newPowerup(ObjectType.LaserCannonPowerup, room, x_loc, y_loc));
    }
    if (hasPrimaryCannon(PyroPrimaryCannon.PLASMA)) {
      powerups.addObject(PowerupFactory.newPowerup(ObjectType.PlasmaCannonPowerup, room, x_loc, y_loc));
    }
    if (num_concussion_missiles >= ConcussionPack.NUM_MISSILES) {
      powerups.addObject(PowerupFactory.newPowerup(ObjectType.ConcussionPack, room, x_loc, y_loc));
      num_concussion_missiles -= ConcussionPack.NUM_MISSILES;
    }
    if (num_concussion_missiles > 0) {
      powerups.addObject(PowerupFactory.newPowerup(ObjectType.ConcussionMissilePowerup, room, x_loc, y_loc));
      --num_concussion_missiles;
    }
    return powerups;
  }

  public boolean acquireShield(int amount) {
    if (shields < MAX_SHIELDS) {
      shields = Math.min(shields + amount, MAX_SHIELDS);
      return true;
    }
    return false;
  }

  public boolean acquireEnergy(int amount) {
    if (energy < MAX_ENERGY) {
      energy = Math.min(energy + amount, MAX_ENERGY);
      return true;
    }
    return false;
  }

  public boolean acquireQuadLasers() {
    if (!has_quad_lasers) {
      has_quad_lasers = true;
      return true;
    }
    return acquireEnergy(Energy.ENERGY_AMOUNT);
  }

  public boolean acquireCannon(PyroPrimaryCannon cannon_type) {
    if (cannon_type.equals(PyroPrimaryCannon.LASER)) {
      if (getLaserCannon().incrementLevel()) {
        return true;
      }
    }
    else {
      int cannon_index = cannon_type.ordinal();
      if (primary_cannons[cannon_index] == null) {
        primary_cannons[cannon_index] = PyroPrimaryCannon.createCannon(cannon_type);
        switchCannon(cannon_type);
        return true;
      }
    }
    return acquireEnergy(Energy.ENERGY_AMOUNT);
  }

  public boolean switchCannon(PyroPrimaryCannon cannon_type) {
    Cannon new_primary_cannon = primary_cannons[cannon_type.ordinal()];
    if (new_primary_cannon == null) {
      return false;
    }
    if (new_primary_cannon.equals(selected_primary_cannon)) {
      return true;
    }
    selected_primary_cannon = new_primary_cannon;
    setPrimaryCannonInfo(cannon_type);
    reload_time_left = CANNON_SWITCH_TIME;
    return true;
  }

  public boolean acquireConcussionMissiles(int amount) {
    if (num_concussion_missiles < MAX_CONCUSSION_MISSILES) {
      num_concussion_missiles = Math.min(num_concussion_missiles + amount, MAX_CONCUSSION_MISSILES);
      return true;
    }
    return false;
  }

  public boolean acquireHomingMissiles(int amount) {
    return true;
  }
}
