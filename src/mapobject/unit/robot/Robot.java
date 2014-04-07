package mapobject.unit.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.HashMap;

import mapobject.MapObject;
import mapobject.MultipleObject;
import mapobject.unit.Pyro;
import mapobject.unit.Unit;
import pilot.Pilot;
import pilot.RobotPilot;
import pilot.UnitPilot;
import resource.ImageHandler;
import structure.Room;
import util.MapUtils;
import cannon.Cannon;

import common.ObjectType;
import component.MapEngine;

public abstract class Robot extends Unit {
  private static final HashMap<ObjectType, Double> RELOAD_TIMES = getReloadTimes();
  private static final HashMap<ObjectType, Integer> SHOTS_PER_VOLLEYS = getShotsPerVolleys();
  private static final HashMap<ObjectType, String> GROWL_SOUND_KEYS = getGrowlSoundKeys();

  private static HashMap<ObjectType, Double> getReloadTimes() {
    HashMap<ObjectType, Double> times = new HashMap<ObjectType, Double>();
    times.put(ObjectType.AdvancedLifter, 1.0);
    times.put(ObjectType.HeavyHulk, 1.0);
    times.put(ObjectType.MediumLifter, 1.0);
    times.put(ObjectType.BabySpider, 2.0);
    times.put(ObjectType.Class1Drone, 2.0);
    times.put(ObjectType.Class2Drone, 2.0);
    times.put(ObjectType.DefenseRobot, 2.0);
    times.put(ObjectType.HeavyDriller, 2.0);
    times.put(ObjectType.LightHulk, 2.0);
    times.put(ObjectType.PlatformLaser, 2.0);
    times.put(ObjectType.SecondaryLifter, 2.0);
    times.put(ObjectType.Spider, 2.0);
    times.put(ObjectType.MediumHulk, 3.0);
    times.put(ObjectType.MediumHulkCloaked, 3.0);
    times.put(ObjectType.PlatformMissile, 4.0);
    times.put(ObjectType.Bomber, 5.0);
    times.put(ObjectType.Gopher, 5.0);
    times.put(ObjectType.MiniBoss, 5.0);
    return times;
  }

  private static HashMap<ObjectType, Integer> getShotsPerVolleys() {
    HashMap<ObjectType, Integer> shots = new HashMap<ObjectType, Integer>();
    shots.put(ObjectType.Bomber, 1);
    shots.put(ObjectType.Gopher, 1);
    shots.put(ObjectType.HeavyHulk, 1);
    shots.put(ObjectType.BabySpider, 2);
    shots.put(ObjectType.Class1Drone, 2);
    shots.put(ObjectType.Class2Drone, 2);
    shots.put(ObjectType.LightHulk, 2);
    shots.put(ObjectType.MediumHulk, 2);
    shots.put(ObjectType.MediumHulkCloaked, 2);
    shots.put(ObjectType.MiniBoss, 2);
    shots.put(ObjectType.SecondaryLifter, 2);
    shots.put(ObjectType.HeavyDriller, 3);
    shots.put(ObjectType.PlatformMissile, 3);
    shots.put(ObjectType.Spider, 3);
    shots.put(ObjectType.DefenseRobot, 4);
    shots.put(ObjectType.PlatformLaser, 4);
    return shots;
  }

  private static HashMap<ObjectType, String> getGrowlSoundKeys() {
    HashMap<ObjectType, String> keys = new HashMap<ObjectType, String>();
    keys.put(ObjectType.AdvancedLifter, "enemies/robot09.wav");
    keys.put(ObjectType.BabySpider, "enemies/robot12.wav");
    keys.put(ObjectType.Bomber, "enemies/robot01.wav");
    keys.put(ObjectType.Class1Drone, "enemies/robot01.wav");
    keys.put(ObjectType.Class2Drone, "enemies/robot11.wav");
    keys.put(ObjectType.DefenseRobot, "enemies/robot21.wav");
    keys.put(ObjectType.Gopher, "enemies/robot34.wav");
    keys.put(ObjectType.HeavyDriller, "enemies/robot27.wav");
    keys.put(ObjectType.HeavyHulk, "enemies/robot07.wav");
    keys.put(ObjectType.LightHulk, "enemies/robot04.wav");
    keys.put(ObjectType.MediumHulk, "enemies/robot03.wav");
    keys.put(ObjectType.MediumHulkCloaked, "enemies/robot03.wav");
    keys.put(ObjectType.MediumLifter, "enemies/robot02.wav");
    keys.put(ObjectType.MiniBoss, "enemies/robot36.wav");
    keys.put(ObjectType.PlatformLaser, "enemies/robot20.wav");
    keys.put(ObjectType.PlatformMissile, "enemies/robot16.wav");
    keys.put(ObjectType.SecondaryLifter, "enemies/robot25.wav");
    keys.put(ObjectType.Spider, "enemies/robot14.wav");
    return keys;
  }

  public static final double VOLLEY_RELOAD_TIME = 0.1;
  public static final double DISABLE_TIME_PER_DAMAGE = 0.03;
  public static final double GROWL_COOLDOWN = 2.0;

  protected final Cannon cannon;
  protected final int shots_per_volley;
  protected final String growl_sound_key;
  protected double shots_left_in_volley;
  protected double volley_reload_time_left;
  protected int cannon_side;
  protected double inactive_time_left;
  protected boolean can_growl;
  protected double growl_cooldown_left;

  public Robot(double radius, Pilot pilot, Cannon cannon, Room room, double x_loc, double y_loc,
          double direction) {
    super(radius, pilot, room, x_loc, y_loc, direction);
    this.cannon = cannon;
    reload_time = RELOAD_TIMES.get(type);
    shots_per_volley = (cannon != null ? SHOTS_PER_VOLLEYS.get(type) : 0);
    growl_sound_key = GROWL_SOUND_KEYS.get(type);
    cannon_side = (int) (Math.random() * 2);
  }

  public Robot(double radius, Cannon cannon, Room room, double x_loc, double y_loc, double direction) {
    this(radius, new RobotPilot(), cannon, room, x_loc, y_loc, direction);
  }

  public String getGrowlSoundKey() {
    return growl_sound_key;
  }

  public boolean canGrowl() {
    return can_growl;
  }

  public void allowGrowl() {
    can_growl = true;
  }

  public void confirmGrowl() {
    growl_cooldown_left = GROWL_COOLDOWN;
  }

  @Override
  public void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell) {
    Point center_pixel = MapUtils.coordsToPixel(x_loc, y_loc, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    Image image = getImage(images);
    Point nw_corner_pixel =
            new Point(center_pixel.x - image.getWidth(null) / 2, center_pixel.y - image.getHeight(null) / 2);
    if (is_visible) {
      g.drawImage(image, nw_corner_pixel.x, nw_corner_pixel.y, null);
      g.setColor(Color.cyan);
      g.drawString(String.valueOf(shields), nw_corner_pixel.x, nw_corner_pixel.y);
    }
    Point target_pixel =
            MapUtils.coordsToPixel(((UnitPilot) pilot).getTargetX(), ((UnitPilot) pilot).getTargetY(),
                    ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    g.setColor(Color.orange);
    g.drawRect(target_pixel.x - 1, target_pixel.y - 1, 2, 2);
  }

  @Override
  public void planNextAction(double s_elapsed) {
    can_growl = false;
    inactive_time_left -= s_elapsed;
    if (inactive_time_left < 0.0) {
      super.planNextAction(s_elapsed);
      if (next_action.fire_cannon && !firing_cannon && reload_time_left < 0.0) {
        planToFireCannon();
      }
    }
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    if (can_growl && growl_cooldown_left < 0.0) {
      engine.registerGrowler(this);
    }
    MultipleObject created_objects = new MultipleObject();
    if (!is_exploded) {
      if (firing_cannon && shields >= 0) {
        if (volley_reload_time_left < 0.0) {
          --shots_left_in_volley;
          if (shots_left_in_volley < 1) {
            firing_cannon = false;
          }
          else {
            volley_reload_time_left = VOLLEY_RELOAD_TIME;
          }
          created_objects.addObject(handleFiringCannon(engine));
        }
      }
    }
    created_objects.addObject(super.doNextAction(engine, s_elapsed));
    return created_objects;
  }

  @Override
  public void handleCooldowns(double s_elapsed) {
    super.handleCooldowns(s_elapsed);
    volley_reload_time_left -= s_elapsed;
    growl_cooldown_left -= s_elapsed;
  }

  @Override
  public void planToFireCannon() {
    super.planToFireCannon();
    shots_left_in_volley = shots_per_volley;
    volley_reload_time_left = -1.0;
  }

  @Override
  public void beDamaged(MapEngine engine, int amount, boolean play_weapon_hit_sound) {
    super.beDamaged(engine, amount, play_weapon_hit_sound);
    tempDisable(amount * DISABLE_TIME_PER_DAMAGE);
  }

  @Override
  public void playWeaponHitSound(MapEngine engine) {
    playSound(engine, "weapons/explode1.wav");
  }

  public void tempDisable(double inactive_time) {
    inactive_time_left = Math.max(inactive_time_left, inactive_time);
    setZeroVelocity();
  }

  public MapObject handleFiringCannon(MapEngine engine) {
    revealIfCloaked();
    playSound(engine, cannon.getSoundKey());
    return fireCannon();
  }

  public MapObject fireCannon() {
    ++cannon_side;
    Point2D.Double abs_offset = MapUtils.perpendicularVector(cannon_offset, direction);
    if (cannon_side % 2 == 0) {
      return cannon.fireCannon(this, room, x_loc + abs_offset.x, y_loc + abs_offset.y, direction);
    }
    return cannon.fireCannon(this, room, x_loc - abs_offset.x, y_loc - abs_offset.y, direction);
  }

  public void handlePyroCollision(MapEngine engine, Pyro pyro, double dx, double dy, double distance) {
    bePushed(engine, dx / distance, dy / distance);
    setZeroVelocity();
    beDamaged(engine, HOSTILE_COLLISION_BASE_DAMAGE + (int) (Math.random() * 2), false);
    pyro.setZeroVelocity();
    pyro.beDamaged(engine, HOSTILE_COLLISION_BASE_DAMAGE + (int) (Math.random() * 2), false);
    pyro.playPublicSound("effects/ramfast.wav");
  }
}
