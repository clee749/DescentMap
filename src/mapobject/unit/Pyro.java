package mapobject.unit;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Point2D;

import mapobject.MapObject;
import mapobject.MultipleObject;
import mapobject.powerup.ConcussionPack;
import mapobject.powerup.Energy;
import mapobject.powerup.HomingPack;
import mapobject.powerup.ProximityPack;
import mapobject.unit.robot.Robot;
import pilot.ComputerPyroPilot;
import pilot.Pilot;
import pilot.PilotAction;
import pilot.PyroPilot;
import pilot.TurnDirection;
import pilot.UnitPilot;
import pyro.PyroPrimaryCannon;
import pyro.PyroSecondaryCannon;
import resource.ImageHandler;
import structure.Room;
import structure.RoomConnection;
import util.MapUtils;
import util.PowerupFactory;
import cannon.Cannon;
import cannon.LaserCannon;

import common.DescentMapException;
import common.ObjectType;
import common.RoomSide;
import component.MapEngine;

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
  private static final String[] SECONDARY_AMMO_TEXTS = getSecondaryAmmoTexts();

  private static PrimaryCannonInfo[] getPrimaryCannonInfos() {
    PrimaryCannonInfo[] infos = new PrimaryCannonInfo[PyroPrimaryCannon.values().length];
    infos[PyroPrimaryCannon.LASER.ordinal()] = new PrimaryCannonInfo(0.25, 0.5);
    infos[PyroPrimaryCannon.SPREADFIRE.ordinal()] = new PrimaryCannonInfo(0.2, 0.5);
    infos[PyroPrimaryCannon.PLASMA.ordinal()] = new PrimaryCannonInfo(0.15, 0.5);
    infos[PyroPrimaryCannon.FUSION.ordinal()] = new PrimaryCannonInfo(1.0, 2.0);
    return infos;
  }

  private static double[] getSecondaryReloadTimes() {
    double[] times = new double[PyroSecondaryCannon.values().length];
    times[PyroSecondaryCannon.CONCUSSION_MISSILE.ordinal()] = 0.6;
    times[PyroSecondaryCannon.HOMING_MISSILE.ordinal()] = 0.6;
    times[PyroSecondaryCannon.PROXIMITY_BOMB.ordinal()] = 0.3;
    times[PyroSecondaryCannon.SMART_MISSILE.ordinal()] = 0.6;
    times[PyroSecondaryCannon.MEGA_MISSILE.ordinal()] = 0.6;
    return times;
  }

  private static int[] getMaxSecondaryAmmos() {
    int[] maxes = new int[PyroSecondaryCannon.values().length];
    maxes[PyroSecondaryCannon.CONCUSSION_MISSILE.ordinal()] = 20;
    maxes[PyroSecondaryCannon.HOMING_MISSILE.ordinal()] = 10;
    maxes[PyroSecondaryCannon.PROXIMITY_BOMB.ordinal()] = 10;
    maxes[PyroSecondaryCannon.SMART_MISSILE.ordinal()] = 5;
    maxes[PyroSecondaryCannon.MEGA_MISSILE.ordinal()] = 5;
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

  private static String[] getSecondaryAmmoTexts() {
    String[] texts = new String[PyroSecondaryCannon.values().length];
    texts[PyroSecondaryCannon.CONCUSSION_MISSILE.ordinal()] = "Concsn Missile: ";
    texts[PyroSecondaryCannon.HOMING_MISSILE.ordinal()] = "Homing Missile: ";
    texts[PyroSecondaryCannon.PROXIMITY_BOMB.ordinal()] = "Proxim. Bomb: ";
    texts[PyroSecondaryCannon.SMART_MISSILE.ordinal()] = "Smart Missile: ";
    texts[PyroSecondaryCannon.MEGA_MISSILE.ordinal()] = "Mega Missile: ";
    return texts;
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

  // energy
  public static final double MIN_STARTING_ENERGY = 100.0;
  public static final double MAX_ENERGY = 200.0;
  public static final double ENERGY_RECHARGE_COOLDOWN = 0.04;
  public static final int NUM_ENERGY_RECHARGES_PER_SOUND = 6;

  // armaments
  public static final int MAX_SHIELDS = 200;
  public static final int MIN_STARTING_CONCUSSION_MISSILES = 3;
  public static final double CANNON_SWITCH_TIME = 1.0;
  public static final int PROXIMITY_BOMB_ORDINAL = PyroSecondaryCannon.PROXIMITY_BOMB.ordinal();
  public static final double BOMB_RELOAD_TIME = SECONDARY_RELOAD_TIMES[PROXIMITY_BOMB_ORDINAL];
  public static final double SPREADFIRE_DIRECTION_DELTA = Math.PI / 36;

  // collisions
  public static final double MIN_WALL_COLLISION_SPEED_FOR_DAMAGE = 0.9;
  public static final int MIN_SHIELDS_AFTER_WALL_COLLISION = 10;

  // death spin
  public static final double DEATH_SPIN_TIME = 5.0;
  public static final double DEATH_SPIN_MOVE_SPEED_DIVISOR = 2.0;
  public static final double DEATH_SPIN_TURN_SPEED_MULTIPLIER = 1.5;

  // death
  public static final double DEATH_SPLASH_DAMAGE_RADIUS = 1.0;
  public static final int DEATH_SPLASH_DAMAGE = 50;
  public static final int MAX_DEATH_SPIN_DAMAGE_TAKEN = 50;

  // absolute Cannon offsets
  private final double outer_cannon_offset;
  private final double cannon_forward_offset;
  private final double missile_offset;

  // energy
  private double energy;
  private double energy_recharge_cooldown_left;
  private boolean is_recharging_energy;
  private int energy_recharge_left_until_sound;

  // primary Cannon info
  private boolean has_quad_lasers;
  private Cannon[] primary_cannons;
  private Cannon selected_primary_cannon;
  private PyroPrimaryCannon selected_primary_cannon_type;
  private double primary_energy_cost;

  // secondary Cannon info
  private double secondary_reload_time;
  private double secondary_reload_time_left;
  private boolean firing_secondary;
  private int missile_side;
  private final Cannon[] secondary_cannons;
  private int[] secondary_ammo;
  private Cannon selected_secondary_cannon;
  private PyroSecondaryCannon selected_secondary_cannon_type;

  // bomb info
  private double bomb_reload_time_left;
  private boolean dropping_bomb;

  // death spin
  private TurnDirection previous_turn;
  private boolean death_spin_started;
  private double death_spin_time_left;
  private double death_spin_direction;
  private double death_spin_delta_direction;

  // misc
  private final int starting_shields;
  private final double max_move_speed;
  private double cloak_time_left;
  private boolean play_personal_sounds;
  private MapEngine engine;

  public Pyro(Pilot pilot, Room room, double x_loc, double y_loc, double direction) {
    super(Unit.getRadius(ObjectType.Pyro), pilot, room, x_loc, y_loc, direction);
    outer_cannon_offset = OUTER_CANNON_OFFSET_FRACTION * radius;
    cannon_forward_offset = CANNON_FORWARD_OFFSET_FRACTION * radius;
    missile_offset = MISSILE_OFFSET_FRACTION * radius;
    secondary_cannons = PyroSecondaryCannon.createCannons();
    starting_shields = shields;
    max_move_speed = move_speed;
    spawnNew();
  }

  public Pyro(Room room, double x_loc, double y_loc, double direction) {
    this(new ComputerPyroPilot(), room, x_loc, y_loc, direction);
  }

  public void spawnNew() {
    move_speed = max_move_speed;
    shields = starting_shields;
    energy = MIN_STARTING_ENERGY;
    primary_cannons = new Cannon[PyroPrimaryCannon.values().length];
    selected_primary_cannon = PyroPrimaryCannon.createCannon(PyroPrimaryCannon.LASER);
    primary_cannons[PyroPrimaryCannon.LASER.ordinal()] = selected_primary_cannon;
    setPrimaryCannonInfo(PyroPrimaryCannon.LASER);
    missile_side = (int) (Math.random() * 2);
    secondary_ammo = new int[PyroSecondaryCannon.values().length];
    secondary_ammo[PyroSecondaryCannon.CONCUSSION_MISSILE.ordinal()] = MIN_STARTING_CONCUSSION_MISSILES;
    selected_secondary_cannon = secondary_cannons[PyroSecondaryCannon.CONCUSSION_MISSILE.ordinal()];
    selected_secondary_cannon_type = PyroSecondaryCannon.CONCUSSION_MISSILE;
    secondary_reload_time = SECONDARY_RELOAD_TIMES[PyroSecondaryCannon.CONCUSSION_MISSILE.ordinal()];
    has_quad_lasers = false;
    death_spin_started = false;
    secondary_ammo[PyroSecondaryCannon.SMART_MISSILE.ordinal()] = 1;
    secondary_ammo[PyroSecondaryCannon.MEGA_MISSILE.ordinal()] = 1;
  }

  public void spawn(Room room, double x_loc, double y_loc, double direction) {
    this.room = room;
    this.x_loc = x_loc;
    this.y_loc = y_loc;
    this.direction = direction;
    is_in_map = true;
    is_visible = true;
    reload_time_left = 0.0;
    firing_cannon = false;
    is_cloaked = false;
    is_exploded = false;
    energy_recharge_cooldown_left = 0.0;
    is_recharging_energy = false;
    secondary_reload_time_left = 0.0;
    firing_secondary = false;
    bomb_reload_time_left = 0.0;
    dropping_bomb = false;
    previous_turn = TurnDirection.NONE;
    setZeroVelocity();
    if (death_spin_started) {
      spawnNew();
    }
    else {
      shields = Math.max(shields, starting_shields);
      energy = Math.max(energy, MIN_STARTING_ENERGY);
      secondary_ammo[PyroSecondaryCannon.CONCUSSION_MISSILE.ordinal()] =
              Math.max(secondary_ammo[PyroSecondaryCannon.CONCUSSION_MISSILE.ordinal()],
                      MIN_STARTING_CONCUSSION_MISSILES);
      ((PyroPilot) pilot).newLevel();
      secondary_ammo[PyroSecondaryCannon.SMART_MISSILE.ordinal()] =
              Math.max(secondary_ammo[PyroSecondaryCannon.SMART_MISSILE.ordinal()], 1);
      secondary_ammo[PyroSecondaryCannon.MEGA_MISSILE.ordinal()] =
              Math.max(secondary_ammo[PyroSecondaryCannon.MEGA_MISSILE.ordinal()], 1);
    }
    pilot.updateCurrentRoom(room);
    ((PyroPilot) pilot).startPilot();
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Pyro;
  }

  @Override
  public void setPilot(Pilot pilot) {
    super.setPilot(pilot);
    ((PyroPilot) pilot).startPilot();
  }

  public void setEngine(MapEngine engine) {
    this.engine = engine;
  }

  public void setPlayPersonalSounds(boolean play_personal_sounds) {
    this.play_personal_sounds = play_personal_sounds;
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

  public boolean isReadyToRespawn() {
    return ((PyroPilot) pilot).isReadyToRespawn();
  }

  public void handleRespawnDelay(double s_elapsed) {
    ((PyroPilot) pilot).handleRespawnDelay(s_elapsed);
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
    if (!is_cloaked) {
      Point center_pixel = MapUtils.coordsToPixel(x_loc, y_loc, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
      Image image = getImage(images);
      Point nw_corner_pixel =
              new Point(center_pixel.x - image.getWidth(null) / 2, center_pixel.y - image.getHeight(null) / 2);
      g.drawImage(image, nw_corner_pixel.x, nw_corner_pixel.y, null);
      paintShield(g, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    }
    else {
      g.setColor(Color.magenta);
      g.drawString(String.valueOf((int) cloak_time_left), 100, 20);
    }
    Point target_pixel =
            MapUtils.coordsToPixel(((UnitPilot) pilot).getTargetX(), ((UnitPilot) pilot).getTargetY(),
                    ref_cell, ref_cell_nw_pixel, pixels_per_cell);
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
                    (has_quad_lasers ? " Quad" : ""), 20);
    text_offset = paintCannonInfo(g, PyroPrimaryCannon.SPREADFIRE, "Spread", text_offset);
    text_offset = paintCannonInfo(g, PyroPrimaryCannon.PLASMA, "Plasma", text_offset);
    paintCannonInfo(g, PyroPrimaryCannon.FUSION, "Fusion", text_offset);
    text_offset = 20;
    for (int i = 0; i < secondary_ammo.length; ++i) {
      text_offset =
              paintSecondaryWeaponInfo(g, PyroSecondaryCannon.values()[i], SECONDARY_AMMO_TEXTS[i],
                      text_offset);
    }
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
    g.drawString(cannon_text, 160, text_offset);
    return text_offset + 20;
  }

  public int paintSecondaryWeaponInfo(Graphics2D g, PyroSecondaryCannon cannon_type, String weapon_text,
          int text_offset) {
    int ammo = getSecondaryAmmo(cannon_type);
    if (ammo < 1 && !cannon_type.equals(selected_secondary_cannon_type)) {
      return text_offset;
    }
    FontMetrics metrics = g.getFontMetrics();
    if (cannon_type.equals(selected_secondary_cannon_type)) {
      g.setColor(SELECTED_CANNON_COLOR);
    }
    else {
      g.setColor(UNSELECTED_CANNON_COLOR);
    }
    g.drawString(weapon_text, 310, text_offset);
    g.setColor(AMMO_AMOUNT_COLOR);
    g.drawString(String.format("%03d", ammo), 310 + metrics.stringWidth(weapon_text), text_offset);
    return text_offset + 20;
  }

  @Override
  public void planNextAction(double s_elapsed) {
    handleEnergyRechargeSound();
    if (shields < 0) {
      handleCooldowns(s_elapsed);
      next_action = PilotAction.MOVE_FORWARD;
      return;
    }
    super.planNextAction(s_elapsed);
    if (next_action.fire_cannon && reload_time_left < 0.0 && primary_energy_cost <= energy) {
      planToFireCannon();
    }
    if (next_action.fire_secondary && secondary_reload_time_left < 0.0 &&
            secondary_ammo[selected_secondary_cannon_type.ordinal()] > 0) {
      planToFireSecondary();
    }
    if (next_action.drop_bomb && bomb_reload_time_left < 0.0 && secondary_ammo[PROXIMITY_BOMB_ORDINAL] > 0) {
      planToDropBomb();
    }
  }

  public void handleEnergyRechargeSound() {
    if (!is_recharging_energy) {
      energy_recharge_left_until_sound = 0;
    }
    is_recharging_energy = false;
  }

  public void planToFireSecondary() {
    firing_secondary = true;
    secondary_reload_time_left = secondary_reload_time;
  }

  public void planToDropBomb() {
    dropping_bomb = true;
    bomb_reload_time_left = BOMB_RELOAD_TIME;
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    if (is_cloaked && cloak_time_left < 0.0) {
      is_cloaked = false;
      is_visible = true;
      playPublicSound("effects/cloakoff.wav");
    }
    if (shields < 0) {
      return doNextDeathSpinAction(engine, s_elapsed);
    }
    previous_turn = next_action.turn;
    MultipleObject created_objects = new MultipleObject();
    if (!death_spin_started) {
      if (firing_cannon) {
        firing_cannon = false;
        created_objects.addObject(fireCannon());
      }
      if (firing_secondary) {
        firing_secondary = false;
        created_objects.addObject(fireSecondary());
      }
      if (dropping_bomb) {
        dropping_bomb = false;
        created_objects.addObject(dropBomb());
      }
    }
    created_objects.addObject(super.doNextAction(engine, s_elapsed));
    return created_objects;
  }

  @Override
  public void applyMovementActions(MapEngine engine, double s_elapsed) {
    super.applyMovementActions(engine, s_elapsed);
    for (Robot robot : room.getRobots()) {
      if (robot.isExploded()) {
        continue;
      }
      double dx = robot.getX() - x_loc;
      double dy = robot.getY() - y_loc;
      double distance = Math.hypot(dx, dy);
      if (distance < radius + robot.getRadius()) {
        robot.handlePyroCollision(engine, this, dx, dy, distance);
        break;
      }
    }
  }

  @Override
  public void handleHittingWall(RoomSide wall_side) {
    double impact_speed;
    if (wall_side.equals(RoomSide.WEST) || wall_side.equals(RoomSide.EAST)) {
      impact_speed = Math.abs(move_x_velocity) + Math.abs(strafe_x_velocity);
    }
    else {
      impact_speed = Math.abs(move_y_velocity) + Math.abs(strafe_y_velocity);
    }
    super.handleHittingWall(wall_side);
    handleWallImpactDamage(impact_speed);
  }

  @Override
  public boolean handleHittingNeighborWall(RoomSide wall_side, RoomConnection connection_to_neighbor) {
    double impact_speed;
    if (wall_side.equals(RoomSide.NORTH) || wall_side.equals(RoomSide.SOUTH)) {
      impact_speed = Math.abs(move_x_velocity) + Math.abs(strafe_x_velocity);
    }
    else {
      impact_speed = Math.abs(move_y_velocity) + Math.abs(strafe_y_velocity);
    }
    boolean location_accepted = super.handleHittingNeighborWall(wall_side, connection_to_neighbor);
    if (!location_accepted) {
      handleWallImpactDamage(impact_speed);
    }
    return location_accepted;
  }

  public void handleWallImpactDamage(double impact_speed) {
    if (impact_speed > MIN_WALL_COLLISION_SPEED_FOR_DAMAGE) {
      if (shields > MIN_SHIELDS_AFTER_WALL_COLLISION) {
        beDamaged(engine, (int) (Math.random() * 2), false);
      }
      playPublicSound("effects/ramfast.wav");
    }
  }

  @Override
  public void setZeroVelocity() {
    super.setZeroVelocity();
    if (death_spin_started) {
      death_spin_time_left = 0.0;
    }
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
    if (Math.random() * MIN_TIME_BETWEEN_DAMAGED_EXPLOSIONS < s_elapsed) {
      playPublicSound("effects/explode2.wav");
    }
    return createDamagedExplosion();
  }

  @Override
  public void handleCooldowns(double s_elapsed) {
    super.handleCooldowns(s_elapsed);
    secondary_reload_time_left -= s_elapsed;
    bomb_reload_time_left -= s_elapsed;
    energy_recharge_cooldown_left -= s_elapsed;
    cloak_time_left -= s_elapsed;
  }

  @Override
  public void playWeaponHitSound(MapEngine engine) {
    playPublicSound("effects/shit01.wav");
  }

  public MapObject fireCannon() {
    revealIfCloaked();
    MultipleObject shots = new MultipleObject();
    switch (selected_primary_cannon_type) {
      case LASER:
        if (has_quad_lasers) {
          Point2D.Double abs_offset = MapUtils.perpendicularVector(outer_cannon_offset, direction);
          double x_forward_abs_offset = Math.cos(direction) * cannon_forward_offset;
          double y_forward_abs_offset = Math.sin(direction) * cannon_forward_offset;
          shots.addObject(selected_primary_cannon.fireCannon(this, room, x_loc + abs_offset.x +
                  x_forward_abs_offset, y_loc + abs_offset.y + y_forward_abs_offset, direction));
          shots.addObject(selected_primary_cannon.fireCannon(this, room, x_loc - abs_offset.x +
                  x_forward_abs_offset, y_loc - abs_offset.y + y_forward_abs_offset, direction));
        }
        // fall through
      case PLASMA:
        // fall through
      case FUSION:
        Point2D.Double abs_offset = MapUtils.perpendicularVector(cannon_offset, direction);
        shots.addObject(selected_primary_cannon.fireCannon(this, room, x_loc + abs_offset.x, y_loc +
                abs_offset.y, direction));
        shots.addObject(selected_primary_cannon.fireCannon(this, room, x_loc - abs_offset.x, y_loc -
                abs_offset.y, direction));
        break;
      case SPREADFIRE:
        shots.addObject(selected_primary_cannon.fireCannon(this, room, x_loc, y_loc,
                MapUtils.normalizeAngle(direction + SPREADFIRE_DIRECTION_DELTA)));
        shots.addObject(selected_primary_cannon.fireCannon(this, room, x_loc, y_loc,
                MapUtils.normalizeAngle(direction - SPREADFIRE_DIRECTION_DELTA)));
        // Vulcan here
        shots.addObject(selected_primary_cannon.fireCannon(this, room, x_loc, y_loc, direction));
        break;
      default:
        throw new DescentMapException("Unexpected PyroPrimaryCannon " + selected_primary_cannon_type);
    }
    energy -= primary_energy_cost;
    playPublicSound(selected_primary_cannon.getSoundKey());
    return shots;
  }

  public MapObject fireSecondary() {
    revealIfCloaked();
    MapObject shot;
    if (selected_secondary_cannon_type.equals(PyroSecondaryCannon.CONCUSSION_MISSILE) ||
            selected_secondary_cannon_type.equals(PyroSecondaryCannon.HOMING_MISSILE)) {
      shot = fireSecondaryWithOffset();
    }
    else {
      shot = fireSecondaryWithoutOffset();
    }
    playPublicSound(selected_secondary_cannon.getSoundKey());
    if (--secondary_ammo[selected_secondary_cannon_type.ordinal()] < 1) {
      handleSecondaryAmmoDepleted();
    }
    return shot;
  }

  public MapObject fireSecondaryWithOffset() {
    ++missile_side;
    Point2D.Double abs_offset = MapUtils.perpendicularVector(missile_offset, direction);
    if (missile_side % 2 == 0) {
      return selected_secondary_cannon.fireCannon(this, room, x_loc + abs_offset.x, y_loc + abs_offset.y,
              direction);
    }
    return selected_secondary_cannon.fireCannon(this, room, x_loc - abs_offset.x, y_loc - abs_offset.y,
            direction);
  }

  public MapObject fireSecondaryWithoutOffset() {
    return selected_secondary_cannon.fireCannon(this, room, x_loc, y_loc, direction);
  }

  public MapObject dropBomb() {
    revealIfCloaked();
    Cannon cannon = secondary_cannons[PROXIMITY_BOMB_ORDINAL];
    MapObject shot = cannon.fireCannon(this, room, x_loc, y_loc, direction);
    playPublicSound(cannon.getSoundKey());
    if (--secondary_ammo[PROXIMITY_BOMB_ORDINAL] < 1 &&
            selected_secondary_cannon_type.equals(PyroSecondaryCannon.PROXIMITY_BOMB)) {
      handleSecondaryAmmoDepleted();
    }
    return shot;
  }

  @Override
  public MapObject releasePowerups() {
    MultipleObject powerups = new MultipleObject();
    powerups.addObject(PowerupFactory.newReleasedPowerup(ObjectType.Shield, room, x_loc, y_loc));
    if (energy >= Energy.ENERGY_AMOUNT) {
      powerups.addObject(PowerupFactory.newReleasedPowerup(ObjectType.Energy, room, x_loc, y_loc));
    }
    if (is_cloaked) {
      powerups.addObject(PowerupFactory.newReleasedPowerup(ObjectType.Cloak, room, x_loc, y_loc));
    }
    if (has_quad_lasers) {
      powerups.addObject(PowerupFactory.newReleasedPowerup(ObjectType.QuadLasers, room, x_loc, y_loc));
    }
    if (getLaserCannon().getLevel() > 1) {
      powerups.addObject(PowerupFactory.newReleasedPowerup(ObjectType.LaserCannonPowerup, room, x_loc, y_loc));
    }
    if (hasPrimaryCannon(PyroPrimaryCannon.SPREADFIRE)) {
      powerups.addObject(PowerupFactory.newReleasedPowerup(ObjectType.SpreadfireCannonPowerup, room, x_loc,
              y_loc));
    }
    if (hasPrimaryCannon(PyroPrimaryCannon.PLASMA)) {
      powerups.addObject(PowerupFactory
              .newReleasedPowerup(ObjectType.PlasmaCannonPowerup, room, x_loc, y_loc));
    }
    if (hasPrimaryCannon(PyroPrimaryCannon.FUSION)) {
      powerups.addObject(PowerupFactory
              .newReleasedPowerup(ObjectType.FusionCannonPowerup, room, x_loc, y_loc));
    }
    if (getSecondaryAmmo(PyroSecondaryCannon.CONCUSSION_MISSILE) >= ConcussionPack.NUM_MISSILES) {
      powerups.addObject(PowerupFactory.newReleasedPowerup(ObjectType.ConcussionPack, room, x_loc, y_loc));
      secondary_ammo[PyroSecondaryCannon.CONCUSSION_MISSILE.ordinal()] -= ConcussionPack.NUM_MISSILES;
    }
    if (getSecondaryAmmo(PyroSecondaryCannon.CONCUSSION_MISSILE) > 0) {
      powerups.addObject(PowerupFactory.newReleasedPowerup(ObjectType.ConcussionMissilePowerup, room, x_loc,
              y_loc));
    }
    if (getSecondaryAmmo(PyroSecondaryCannon.HOMING_MISSILE) >= HomingPack.NUM_MISSILES) {
      powerups.addObject(PowerupFactory.newReleasedPowerup(ObjectType.HomingPack, room, x_loc, y_loc));
      secondary_ammo[PyroSecondaryCannon.HOMING_MISSILE.ordinal()] -= HomingPack.NUM_MISSILES;
    }
    if (getSecondaryAmmo(PyroSecondaryCannon.HOMING_MISSILE) > 0) {
      powerups.addObject(PowerupFactory.newReleasedPowerup(ObjectType.HomingMissilePowerup, room, x_loc,
              y_loc));
    }
    if (getSecondaryAmmo(PyroSecondaryCannon.PROXIMITY_BOMB) >= ProximityPack.NUM_BOMBS) {
      powerups.addObject(PowerupFactory.newReleasedPowerup(ObjectType.ProximityPack, room, x_loc, y_loc));
    }
    if (getSecondaryAmmo(PyroSecondaryCannon.SMART_MISSILE) > 0) {
      powerups.addObject(PowerupFactory
              .newReleasedPowerup(ObjectType.SmartMissilePowerup, room, x_loc, y_loc));
    }
    if (getSecondaryAmmo(PyroSecondaryCannon.MEGA_MISSILE) > 0) {
      powerups.addObject(PowerupFactory.newReleasedPowerup(ObjectType.MegaMissilePowerup, room, x_loc, y_loc));
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

  public boolean acquireCloak(double time) {
    if (!is_cloaked) {
      is_cloaked = true;
      is_visible = false;
      cloak_time_left = time;
      visible_time_left = 0.0;
      playPublicSound("effects/cloakon.wav");
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

  public boolean switchPrimaryCannon(PyroPrimaryCannon cannon_type, boolean action_on_error) {
    Cannon new_primary_cannon = primary_cannons[cannon_type.ordinal()];
    if (new_primary_cannon == null) {
      if (action_on_error) {
        playPersonalSound("effects/beep02.wav");
      }
      return false;
    }
    if (new_primary_cannon.equals(selected_primary_cannon)) {
      return true;
    }
    selected_primary_cannon = new_primary_cannon;
    reload_time_left = CANNON_SWITCH_TIME;
    setPrimaryCannonInfo(cannon_type);
    playPersonalSound("effects/change1.wav");
    return true;
  }

  public boolean acquireSecondaryAmmo(PyroSecondaryCannon cannon_type, int amount) {
    boolean has_any_ammo = false;
    for (int i = 0; i < secondary_ammo.length; ++i) {
      if (i != PROXIMITY_BOMB_ORDINAL && secondary_ammo[i] > 0) {
        has_any_ammo = true;
        break;
      }
    }

    int current_ammo = getSecondaryAmmo(cannon_type);
    int max_ammo = getMaxSecondaryAmmo(cannon_type);
    if (current_ammo < max_ammo) {
      secondary_ammo[cannon_type.ordinal()] = Math.min(current_ammo + amount, max_ammo);
      if (!has_any_ammo) {
        switchSecondaryCannon(cannon_type, false);
      }
      return true;
    }
    return false;
  }

  public boolean switchSecondaryCannon(PyroSecondaryCannon cannon_type, boolean action_on_error) {
    if (secondary_ammo[cannon_type.ordinal()] < 1) {
      if (action_on_error) {
        playPersonalSound("effects/beep02.wav");
      }
      return false;
    }
    if (cannon_type.equals(selected_secondary_cannon_type)) {
      return true;
    }
    selected_secondary_cannon = secondary_cannons[cannon_type.ordinal()];
    selected_secondary_cannon_type = cannon_type;
    secondary_reload_time_left = CANNON_SWITCH_TIME;
    playPersonalSound("effects/change2.wav");
    return true;
  }

  public void handleSecondaryAmmoDepleted() {
    for (int i = PyroSecondaryCannon.values().length - 1; i >= 0; --i) {
      if (i != PROXIMITY_BOMB_ORDINAL && secondary_ammo[i] > 0) {
        switchSecondaryCannon(PyroSecondaryCannon.values()[i], false);
        break;
      }
    }
  }

  public void rechargeEnergy() {
    is_recharging_energy = true;
    --energy_recharge_left_until_sound;
    if (energy_recharge_cooldown_left < 0.0 && energy < MIN_STARTING_ENERGY) {
      energy = Math.min(energy + 1.0, MIN_STARTING_ENERGY);
      energy_recharge_cooldown_left = ENERGY_RECHARGE_COOLDOWN;
      if (energy_recharge_left_until_sound < 1) {
        energy_recharge_left_until_sound = NUM_ENERGY_RECHARGES_PER_SOUND;
        playPublicSound("effects/power04.wav");
      }
    }
  }

  public boolean playPersonalSound(String name) {
    if (play_personal_sounds) {
      engine.playSound(name);
      return true;
    }
    return false;
  }

  public void playPublicSound(String name) {
    if (!playPersonalSound(name)) {
      playSound(engine, name);
    }
  }
}
