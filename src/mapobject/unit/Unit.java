package mapobject.unit;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;

import mapobject.MapObject;
import mapobject.MovableObject;
import mapobject.ephemeral.Explosion;
import mapobject.shot.Shot;
import pilot.Pilot;
import pilot.PilotAction;
import structure.Room;
import util.MapUtils;

import common.Constants;
import component.MapEngine;

import external.ImageHandler;

public abstract class Unit extends MovableObject {
  protected final double cannon_offset;
  protected double reload_time;
  protected double reload_time_left;
  protected boolean firing_cannon;
  protected int shields;
  protected boolean is_exploded;
  protected double exploding_time_left;

  public Unit(Pilot pilot, Room room, double x_loc, double y_loc, double direction) {
    super(pilot, room, x_loc, y_loc, direction);
    cannon_offset = Constants.getCannonOffset(type) * radius;
    shields = Constants.getStartingShields(type);
  }

  public int getShields() {
    return shields;
  }

  @Override
  public void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell) {
    Point center_pixel = MapUtils.coordsToPixel(x_loc, y_loc, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    Image image = images.getImage(image_name, direction);
    Point nw_corner_pixel =
            new Point(center_pixel.x - image.getWidth(null) / 2, center_pixel.y - image.getHeight(null) / 2);
    g.drawImage(image, nw_corner_pixel.x, nw_corner_pixel.y, null);
    g.setColor(Color.cyan);
    g.drawString(String.valueOf(shields), nw_corner_pixel.x, nw_corner_pixel.y);
  }

  @Override
  public void planNextAction(double s_elapsed) {
    if (!is_exploded) {
      super.planNextAction(s_elapsed);
    }
    else {
      next_action = PilotAction.NO_ACTION;
    }
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    if (shields < 0) {
      if (!is_exploded) {
        is_exploded = true;
        double explosion_time =
                Math.random() * (Constants.UNIT_EXPLOSION_MAX_TIME - Constants.UNIT_EXPLOSION_MIN_TIME) +
                        Constants.UNIT_EXPLOSION_MIN_TIME;
        exploding_time_left = explosion_time / Constants.UNIT_EXPLOSION_TIME_DIVISOR;
        return new Explosion(room, x_loc, y_loc, radius * Constants.UNIT_EXPLOSION_RADIUS_MULTIPLIER,
                explosion_time);
      }
      if (exploding_time_left < 0.0) {
        is_in_map = false;
        return releasePowerups();
      }
      exploding_time_left -= s_elapsed;
    }
    return super.doNextAction(engine, s_elapsed);
  }

  public void handleCannonReload(double s_elapsed) {
    reload_time_left -= s_elapsed;
  }

  public void planToFireCannon() {
    firing_cannon = true;
    reload_time_left = reload_time;
  }

  public void hitByShot(Shot shot) {
    shields -= shot.getDamage();
  }

  public abstract MapObject fireCannon();

  public abstract MapObject releasePowerups();
}
