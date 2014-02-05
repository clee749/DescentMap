package mapobject.unit.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.HashMap;

import mapobject.MapObject;
import mapobject.unit.Unit;
import pilot.Pilot;
import pilot.RobotPilot;
import structure.Room;
import util.MapUtils;
import cannon.Cannon;

import common.ObjectType;
import component.MapEngine;

import external.ImageHandler;

public abstract class Robot extends Unit {
  private static final HashMap<ObjectType, Double> RELOAD_TIMES = getReloadTimes();
  private static final HashMap<ObjectType, Integer> SHOTS_PER_VOLLEYS = getShotsPerVolleys();

  private static HashMap<ObjectType, Double> getReloadTimes() {
    HashMap<ObjectType, Double> times = new HashMap<ObjectType, Double>();
    times.put(ObjectType.HeavyHulk, 1.0);
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
    times.put(ObjectType.PlatformMissile, 4.0);
    times.put(ObjectType.Bomber, 5.0);
    return times;
  }

  private static HashMap<ObjectType, Integer> getShotsPerVolleys() {
    HashMap<ObjectType, Integer> shots = new HashMap<ObjectType, Integer>();
    shots.put(ObjectType.Bomber, 1);
    shots.put(ObjectType.HeavyHulk, 1);
    shots.put(ObjectType.BabySpider, 2);
    shots.put(ObjectType.Class1Drone, 2);
    shots.put(ObjectType.Class2Drone, 2);
    shots.put(ObjectType.LightHulk, 2);
    shots.put(ObjectType.MediumHulk, 2);
    shots.put(ObjectType.SecondaryLifter, 2);
    shots.put(ObjectType.HeavyDriller, 3);
    shots.put(ObjectType.PlatformMissile, 3);
    shots.put(ObjectType.Spider, 3);
    shots.put(ObjectType.DefenseRobot, 4);
    shots.put(ObjectType.PlatformLaser, 4);
    return shots;
  }

  public static final double VOLLEY_RELOAD_TIME = 0.1;

  protected final Cannon cannon;
  protected final int shots_per_volley;
  protected double shots_left_in_volley;
  protected double volley_reload_time_left;
  protected int cannon_side;

  public Robot(double radius, Pilot pilot, Cannon cannon, Room room, double x_loc, double y_loc,
          double direction) {
    super(radius, pilot, room, x_loc, y_loc, direction);
    this.cannon = cannon;
    reload_time = RELOAD_TIMES.get(type);
    shots_per_volley = SHOTS_PER_VOLLEYS.get(type);
    cannon_side = (int) (Math.random() * 2);
  }

  public Robot(double radius, Cannon cannon, Room room, double x_loc, double y_loc, double direction) {
    this(radius, new RobotPilot(), cannon, room, x_loc, y_loc, direction);
  }

  @Override
  public void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell) {
    super.paint(g, images, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    Point target_pixel =
            MapUtils.coordsToPixel(pilot.getTargetX(), pilot.getTargetY(), ref_cell, ref_cell_nw_pixel,
                    pixels_per_cell);
    g.setColor(Color.orange);
    g.drawRect(target_pixel.x - 1, target_pixel.y - 1, 2, 2);
  }

  @Override
  public void planNextAction(double s_elapsed) {
    super.planNextAction(s_elapsed);
    if (next_action.fire_cannon && !firing_cannon && reload_time_left < 0.0) {
      planToFireCannon();
    }
    else {
      handleCannonReload(s_elapsed);
    }
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    MapObject object_created = super.doNextAction(engine, s_elapsed);
    if (firing_cannon && shields >= 0) {
      if (volley_reload_time_left < 0.0) {
        --shots_left_in_volley;
        if (shots_left_in_volley < 1) {
          firing_cannon = false;
        }
        else {
          volley_reload_time_left = VOLLEY_RELOAD_TIME;
        }
        return fireCannon();
      }
      else {
        handleCannonVolleyReload(s_elapsed);
      }
    }
    return object_created;
  }

  @Override
  public void planToFireCannon() {
    super.planToFireCannon();
    shots_left_in_volley = shots_per_volley;
    volley_reload_time_left = -1.0;
  }

  @Override
  public MapObject fireCannon() {
    ++cannon_side;
    Point2D.Double abs_offset = MapUtils.perpendicularVector(cannon_offset, direction);
    if (cannon_side % 2 == 0) {
      return cannon.fireCannon(this, room, x_loc + abs_offset.x, y_loc + abs_offset.y, direction);
    }
    return cannon.fireCannon(this, room, x_loc - abs_offset.x, y_loc - abs_offset.y, direction);
  }

  public void handleCannonVolleyReload(double s_elapsed) {
    volley_reload_time_left -= s_elapsed;
  }
}
