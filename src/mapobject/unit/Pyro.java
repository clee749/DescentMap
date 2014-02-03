package mapobject.unit;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Point2D;

import mapobject.MapObject;
import mapobject.MultipleObject;
import mapobject.ephemeral.Explosion;
import mapobject.powerup.ConcussionPack;
import mapobject.powerup.Energy;
import mapobject.powerup.HomingPack;
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
  public static final Color BASE_SHIELD_COLOR = Color.cyan;

  public static int getMaxSecondaryAmmo(PyroSecondaryCannon cannon_type) {
    return MAX_SECONDARY_AMMOS[cannon_type.ordinal()];
  }

  private static final PrimaryCannonInfo[] PRIMARY_CANNON_INFOS = getPrimaryCannonInfos();
  private static final double[] SECONDARY_RELOAD_TIMES = getSecondaryReloadTimes();
  private static final int[] MAX_SECONDARY_AMMOS = getMaxSecondaryAmmos();
  private static final Color[] SHIELD_COLORS = getShieldColors();

  private static PrimaryCannonInfo[] getPrimaryCannonInfos() {
    PrimaryCannonInfo[] infos = new PrimaryCannonInfo[PyroPrimaryCannon.values().length];
    infos[PyroPrimaryCannon.LASER.ordinal()] = new PrimaryCannonInfo(0.25, 0.5);
    infos[PyroPrimaryCannon.PLASMA.ordinal()] = new PrimaryCannonInfo(0.15, 0.5);
    return infos;
  }

  private static double[] getSecondaryReloadTimes() {
    double[] times = new double[PyroSecondaryCannon.values().length];
    times[PyroSecondaryCannon.CONCUSSION_MISSILE.ordinal()] = 0.5;
    times[PyroSecondaryCannon.HOMING_MISSILE.ordinal()] = 0.5;
    return times;
  }

  private static int[] getMaxSecondaryAmmos() {
    int[] maxes = new int[PyroSecondaryCannon.values().length];
    maxes[PyroSecondaryCannon.CONCUSSION_MISSILE.ordinal()] = 20;
    maxes[PyroSecondaryCannon.HOMING_MISSILE.ordinal()] = 10;
    return maxes;
  }

  private static Color[] getShieldColors() {
    Color[] colors = new Color[NUM_SHIELD_COLORS];
    float dalpha = MAX_SHIELD_ALPHA / NUM_SHIELD_COLORS;
    for (int i = 0; i < colors.length; ++i) {
      colors[i] =
              new Color(BASE_SHIELD_COLOR.getColorSpace(), BASE_SHIELD_COLOR.getColorComponents(null),
                      dalpha * (i + 1));
    }
    return colors;
  }

  // relative Cannon offsets
  public static final double OUTER_CANNON_OFFSET_FRACTION = 0.8;
  public static final double CANNON_FORWARD_OFFSET_FRACTION = 0.2;
  public static final double MISSILE_OFFSET_FRACTION = 0.2;

  // shield painting
  public static final int NUM_SHIELD_COLORS = 9;
  public static final float MAX_SHIELD_ALPHA = 0.5f;
  public static final int MIN_SHIELDS_FOR_PAINT = 10;
  public static final int SHIELD_COLORS_DIVISOR = 10;
  public static final double SHIELD_RADIUS = Unit.getRadius(ObjectType.Pyro) * 1.3;
  public static final double SHIELD_DIAMETER = SHIELD_RADIUS * 2;

  // Cannon info painting
  public static final Color SELECTED_CANNON_COLOR = Color.green;
  public static final Color UNSELECTED_CANNON_COLOR = Color.gray;
  public static final Color AMMO_AMOUNT_COLOR = Color.red;

  // armaments
  public static final int MAX_SHIELDS = 200;
  public static final int MIN_STARTING_ENERGY = 100;
  public static final int MAX_ENERGY = 200;
  public static final int MIN_STARTING_CONCUSSION_MISSILES = 3;
  public static final double CANNON_SWITCH_TIME = 1.0;

  // death spin
  public static final double DEATH_SPIN_TIME = 5.0;
  public static final double DEATH_SPIN_MOVE_SPEED_DIVISOR = 2.0;
  public static final double DEATH_SPIN_TURN_SPEED_MULTIPLIER = 1.5;
  public static final double DEATH_SPIN_EXPLOSION_RADIUS = 0.1;
  public static final double DEATH_SPIN_EXPLOSION_TIME = 1.0;

  // death
  public static final double DEATH_SPLASH_DAMAGE_RADIUS = 1.0;
  public static final int DEATH_SPLASH_DAMAGE = 50;
  public static final int MAX_DEATH_SPIN_DAMAGE_TAKEN = 50;

  // absolute Cannon offsets
  private final double outer_cannon_offset;
  private final double cannon_forward_offset;
  private final double missile_offset;

  // primary Cannon info
  private double energy;
  private boolean has_quad_lasers;
  private final Cannon[] primary_cannons;
  private Cannon selected_primary_cannon;
  private PyroPrimaryCannon selected_primary_cannon_type;
  private double primary_energy_cost;

  // secondary Cannon info
  private double secondary_reload_time;
  private double secondary_reload_time_left;
  private boolean firing_secondary;
  private int missile_side;
  private final Cannon[] secondary_cannons;
  private final int[] secondary_ammo;
  private Cannon selected_secondary_cannon;
  private PyroSecondaryCannon selected_secondary_cannon_type;

  // death spin
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
    missile_side = (int) (Math.random() * 2);
    secondary_cannons = PyroSecondaryCannon.createCannons();
    secondary_ammo = new int[PyroSecondaryCannon.values().length];
    secondary_ammo[PyroSecondaryCannon.CONCUSSION_MISSILE.ordinal()] = MIN_STARTING_CONCUSSION_MISSILES;
    selected_secondary_cannon = new ConcussionMissileCannon(Shot.getDamage(ObjectType.ConcussionMissile));
    selected_secondary_cannon_type = PyroSecondaryCannon.CONCUSSION_MISSILE;
    secondary_reload_time = SECONDARY_RELOAD_TIMES[PyroSecondaryCannon.CONCUSSION_MISSILE.ordinal()];
    ((PyroPilot) pilot).startPilot();
    secondary_ammo[PyroSecondaryCannon.HOMING_MISSILE.ordinal()] = MIN_STARTING_CONCUSSION_MISSILES;
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

  public PyroPrimaryCannon getSelectedPrimaryCannonType() {
    return selected_primary_cannon_type;
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

  public void setPrimaryCannonInfo(PyroPrimaryCannon cannon_type) {
    PrimaryCannonInfo cannon_info = PRIMARY_CANNON_INFOS[cannon_type.ordinal()];
    reload_time = cannon_info.reload_time;
    primary_energy_cost = cannon_info.energy_cost;
    selected_primary_cannon_type = cannon_type;
  }

  public boolean isSecondaryCannonReloaded() {
    return secondary_reload_time_left < 0.0;
  }

  public int getSecondaryAmmo(PyroSecondaryCannon cannon_type) {
    return secondary_ammo[cannon_type.ordinal()];
  }

  public PyroSecondaryCannon getSelectedSecondaryCannonType() {
    return selected_secondary_cannon_type;
  }

  public void setSecondaryCannonInfo(PyroSecondaryCannon cannon_type) {
    secondary_reload_time = SECONDARY_RELOAD_TIMES[cannon_type.ordinal()];
    selected_secondary_cannon_type = cannon_type;
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
    paintShield(g, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    super.paint(g, images, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    Point target_pixel =
            MapUtils.coordsToPixel(pilot.getTargetX(), pilot.getTargetY(), ref_cell, ref_cell_nw_pixel,
                    pixels_per_cell);
    g.setColor(Color.yellow);
    g.drawRect(target_pixel.x - 1, target_pixel.y - 1, 2, 2);
  }

  public void paintShield(Graphics2D g, Point ref_cell, Point ref_cell_nw_pixel, int pixels_per_cell) {
    if (shields < MIN_SHIELDS_FOR_PAINT) {
      return;
    }
    g.setColor(SHIELD_COLORS[Math.min(shields / SHIELD_COLORS_DIVISOR, SHIELD_COLORS.length) - 1]);
    Point nw_corner_pixel =
            MapUtils.coordsToPixelRounded(x_loc - SHIELD_RADIUS, y_loc - SHIELD_RADIUS, ref_cell,
                    ref_cell_nw_pixel, pixels_per_cell);
    int shield_pixel_diameter = (int) (SHIELD_DIAMETER * pixels_per_cell);
    g.fillOval(nw_corner_pixel.x, nw_corner_pixel.y, shield_pixel_diameter, shield_pixel_diameter);
  }

  public void paintInfo(Graphics2D g) {
    g.setColor(Color.yellow);
    g.drawString("Energy: " + (int) Math.ceil(energy), 10, 20);
    g.setColor(Color.cyan);
    g.drawString("Shield: " + shields, 10, 40);
    int text_offset =
            paintCannonInfo(g, PyroPrimaryCannon.LASER, "Laser Lvl: " + getLaserLevel() +
                    (has_quad_lasers ? " Quad" : ""), 70);
    text_offset = paintCannonInfo(g, PyroPrimaryCannon.PLASMA, "Plasma", text_offset);
    text_offset =
            paintSecondaryWeaponInfo(g, PyroSecondaryCannon.CONCUSSION_MISSILE, "Concsn Missile: ",
                    text_offset + 10);
    text_offset =
            paintSecondaryWeaponInfo(g, PyroSecondaryCannon.HOMING_MISSILE, "Homing Missile: ", text_offset);
  }

  public int paintCannonInfo(Graphics2D g, PyroPrimaryCannon cannon_type, String cannon_text, int text_offset) {
    Cannon cannon = primary_cannons[cannon_type.ordinal()];
    if (cannon == null) {
      return text_offset;
    }
    if (cannon.equals(selected_primary_cannon)) {
      g.setColor(SELECTED_CANNON_COLOR);
    }
    else {
      g.setColor(UNSELECTED_CANNON_COLOR);
    }
    g.drawString(cannon_text, 10, text_offset);
    return text_offset + 20;
  }

  public int paintSecondaryWeaponInfo(Graphics2D g, PyroSecondaryCannon cannon_type, String weapon_text,
          int text_offset) {
    int ammo = getSecondaryAmmo(cannon_type);
    if (ammo < 1) {
      return text_offset;
    }
    FontMetrics metrics = g.getFontMetrics();
    if (cannon_type.equals(selected_secondary_cannon_type)) {
      g.setColor(SELECTED_CANNON_COLOR);
    }
    else {
      g.setColor(UNSELECTED_CANNON_COLOR);
    }
    g.drawString(weapon_text, 10, text_offset);
    g.setColor(AMMO_AMOUNT_COLOR);
    g.drawString(String.format("%03d", ammo), 10 + metrics.stringWidth(weapon_text), text_offset);
    return text_offset + 20;
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
          room.doSplashDamage(this, DEATH_SPLASH_DAMAGE, DEATH_SPLASH_DAMAGE_RADIUS, this);
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
    if (shields < -MAX_DEATH_SPIN_DAMAGE_TAKEN) {
      death_spin_time_left = -1.0;
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
    if (has_quad_lasers && selected_primary_cannon_type.equals(PyroPrimaryCannon.LASER)) {
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
    int ammo = secondary_ammo[selected_secondary_cannon_type.ordinal()];
    if (ammo < 1) {
      return null;
    }
    --secondary_ammo[selected_secondary_cannon_type.ordinal()];
    ++missile_side;
    Point2D.Double abs_offset = MapUtils.perpendicularVector(missile_offset, direction);
    MapObject shot;
    if (missile_side % 2 == 0) {
      shot =
              selected_secondary_cannon.fireCannon(this, room, x_loc + abs_offset.x, y_loc + abs_offset.y,
                      direction);
    }
    shot =
            selected_secondary_cannon.fireCannon(this, room, x_loc - abs_offset.x, y_loc - abs_offset.y,
                    direction);
    if (ammo == 1) {
      handleSecondaryAmmoDepleted();
    }
    return shot;
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
    if (getSecondaryAmmo(PyroSecondaryCannon.CONCUSSION_MISSILE) >= ConcussionPack.NUM_MISSILES) {
      powerups.addObject(PowerupFactory.newPowerup(ObjectType.ConcussionPack, room, x_loc, y_loc));
      secondary_ammo[PyroSecondaryCannon.CONCUSSION_MISSILE.ordinal()] -= ConcussionPack.NUM_MISSILES;
    }
    if (getSecondaryAmmo(PyroSecondaryCannon.CONCUSSION_MISSILE) > 0) {
      powerups.addObject(PowerupFactory.newPowerup(ObjectType.ConcussionMissilePowerup, room, x_loc, y_loc));
    }
    if (getSecondaryAmmo(PyroSecondaryCannon.HOMING_MISSILE) >= HomingPack.NUM_MISSILES) {
      powerups.addObject(PowerupFactory.newPowerup(ObjectType.HomingPack, room, x_loc, y_loc));
      secondary_ammo[PyroSecondaryCannon.HOMING_MISSILE.ordinal()] -= HomingPack.NUM_MISSILES;
    }
    if (getSecondaryAmmo(PyroSecondaryCannon.HOMING_MISSILE) > 0) {
      powerups.addObject(PowerupFactory.newPowerup(ObjectType.HomingMissilePowerup, room, x_loc, y_loc));
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
        return true;
      }
    }
    return acquireEnergy(Energy.ENERGY_AMOUNT);
  }

  public boolean switchPrimaryCannon(PyroPrimaryCannon cannon_type) {
    Cannon new_primary_cannon = primary_cannons[cannon_type.ordinal()];
    if (new_primary_cannon == null) {
      return false;
    }
    if (new_primary_cannon.equals(selected_primary_cannon)) {
      return true;
    }
    selected_primary_cannon = new_primary_cannon;
    reload_time_left = CANNON_SWITCH_TIME;
    setPrimaryCannonInfo(cannon_type);
    return true;
  }

  public boolean acquireSecondaryAmmo(PyroSecondaryCannon cannon_type, int amount) {
    int current_ammo = getSecondaryAmmo(cannon_type);
    int max_ammo = getMaxSecondaryAmmo(cannon_type);
    if (current_ammo < max_ammo) {
      secondary_ammo[cannon_type.ordinal()] = Math.min(current_ammo + amount, max_ammo);
      return true;
    }
    return false;
  }

  public boolean switchSecondaryCannon(PyroSecondaryCannon cannon_type) {
    if (secondary_ammo[cannon_type.ordinal()] < 1) {
      return false;
    }
    if (cannon_type.equals(selected_secondary_cannon_type)) {
      return true;
    }
    selected_secondary_cannon = secondary_cannons[cannon_type.ordinal()];
    selected_secondary_cannon_type = cannon_type;
    secondary_reload_time_left = CANNON_SWITCH_TIME;
    return true;
  }

  public void handleSecondaryAmmoDepleted() {
    for (int i = PyroSecondaryCannon.values().length - 1; i >= 0; --i) {
      if (secondary_ammo[i] > 0) {
        switchSecondaryCannon(PyroSecondaryCannon.values()[i]);
        break;
      }
    }
  }
}
