package mapobject.unit.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

import mapobject.MapObject;
import mapobject.shot.LaserShot;
import mapobject.unit.Unit;
import pilot.Pilot;
import pilot.RobotPilot;
import structure.Room;
import util.MapUtils;

import common.Constants;
import component.MapEngine;

import external.ImageHandler;

public abstract class Robot extends Unit {
  protected final int shots_per_volley;
  protected final double volley_reload_time;
  protected double shots_left_in_volley;
  protected double volley_reload_time_left;
  protected int cannon_side;

  public Robot(Pilot pilot, Room room, double x_loc, double y_loc, double direction) {
    super(pilot, room, x_loc, y_loc, direction);
    shots_per_volley = Constants.getShotsPerVolley(type);
    reload_time = Constants.getReloadTime(type);
    volley_reload_time = Constants.getVolleyReloadTime(type);
    cannon_side = (int) (Math.random() * 2);
  }

  public Robot(Room room, double x_loc, double y_loc, double direction) {
    this(new RobotPilot(), room, x_loc, y_loc, direction);
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
    if (firing_cannon) {
      if (volley_reload_time_left < 0.0) {
        --shots_left_in_volley;
        if (shots_left_in_volley < 1) {
          firing_cannon = false;
        }
        else {
          volley_reload_time_left = volley_reload_time;
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
    Point2D.Double abs_offset = findRightShotAbsOffset(cannon_offset);
    if (cannon_side % 2 == 0) {
      return new LaserShot(this, room, x_loc + abs_offset.x, y_loc + abs_offset.y, direction, 2);
    }
    return new LaserShot(this, room, x_loc - abs_offset.x, y_loc - abs_offset.y, direction, 2);
  }

  public void handleCannonVolleyReload(double s_elapsed) {
    volley_reload_time_left -= s_elapsed;
  }
}
