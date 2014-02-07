package mapobject.unit;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.util.HashMap;

import mapobject.MapObject;
import mapobject.MovableObject;
import mapobject.MultipleObject;
import mapobject.ephemeral.Explosion;
import pilot.Pilot;
import pilot.PilotAction;
import structure.Room;
import util.MapUtils;

import common.ObjectType;
import component.MapEngine;

import external.ImageHandler;

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
  public static final double EXPLOSION_RADIUS_MULTIPLIER = 1.1;
  public static final double EXPLOSION_MIN_TIME = 0.5;
  public static final double EXPLOSION_MAX_TIME = EXPLOSION_MIN_TIME * 2;
  public static final double EXPLOSION_TIME_DIVISOR = 3.0;

  protected final double cannon_offset;
  protected final int half_shields;
  protected double reload_time;
  protected double reload_time_left;
  protected boolean firing_cannon;
  protected int shields;
  protected boolean is_exploded;
  protected double exploding_time_left;

  public Unit(double radius, Pilot pilot, Room room, double x_loc, double y_loc, double direction) {
    super(radius, pilot, room, x_loc, y_loc, direction);
    cannon_offset = CANNON_OFFSETS.get(type) * radius;
    shields = STARTING_SHIELDS.get(type);
    half_shields = shields / 2;
  }

  public int getShields() {
    return shields;
  }

  @Override
  public void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell) {
    Point center_pixel = MapUtils.coordsToPixel(x_loc, y_loc, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    Image image = getImage(images);
    Point nw_corner_pixel =
            new Point(center_pixel.x - image.getWidth(null) / 2, center_pixel.y - image.getHeight(null) / 2);
    g.drawImage(image, nw_corner_pixel.x, nw_corner_pixel.y, null);
    g.setColor(Color.cyan);
    g.drawString(String.valueOf(shields), nw_corner_pixel.x, nw_corner_pixel.y);
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
    if (shields < 0) {
      return handleDeath(s_elapsed);
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

  public boolean isCannonReloaded() {
    return reload_time_left < 0.0;
  }

  public void handleCooldowns(double s_elapsed) {
    reload_time_left -= s_elapsed;
  }

  public void planToFireCannon() {
    firing_cannon = true;
    reload_time_left = reload_time;
  }

  public void beDamaged(int amount) {
    shields -= amount;
  }

  public Explosion createDamagedExplosion() {
    return new Explosion(room, x_loc + Math.random() * 2 * radius - radius, y_loc + Math.random() * 2 *
            radius - radius, DAMAGED_EXPLOSION_RADIUS, DAMAGED_EXPLOSION_TIME);
  }

  public MapObject handleDeath(double s_elapsed) {
    if (!is_exploded) {
      is_exploded = true;
      double explosion_time = Math.random() * (EXPLOSION_MAX_TIME - EXPLOSION_MIN_TIME) + EXPLOSION_MIN_TIME;
      exploding_time_left = explosion_time / EXPLOSION_TIME_DIVISOR;
      return new Explosion(room, x_loc, y_loc, radius * EXPLOSION_RADIUS_MULTIPLIER, explosion_time);
    }
    if (exploding_time_left < 0.0) {
      is_in_map = false;
      return releasePowerups();
    }
    exploding_time_left -= s_elapsed;
    return null;
  }

  public abstract MapObject fireCannon();

  public abstract MapObject releasePowerups();
}
