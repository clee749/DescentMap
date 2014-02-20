package mapobject;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;

import mapobject.ephemeral.Explosion;
import mapobject.shot.Shot;
import mapobject.unit.Pyro;
import mapobject.unit.Unit;
import resource.ImageHandler;
import structure.Room;
import util.MapUtils;

import common.ObjectType;
import component.MapEngine;

public class ProximityBomb extends MapObject {
  public static final double RADIUS = 0.12;
  public static final double SECONDS_PER_FRAME = 0.03;
  public static final int DAMAGE = 27;
  public static final double SPLASH_DAMAGE_RADIUS = 1.0;
  public static final double EXPLOSION_RADIUS = RADIUS * 2;
  public static final double EXPLOSION_TIME = 0.75;
  public static final double TIME_TO_FULLY_ARM = 5.0;

  private final MapObject source;
  private final int damage;
  private int frame_num;
  private double frame_time_left;
  private boolean is_fully_armed;
  private double time_until_fully_armed;

  public ProximityBomb(MapObject source, int damage, Room room, double x_loc, double y_loc) {
    super(RADIUS, room, x_loc, y_loc);
    this.source = source;
    this.damage = damage;
    frame_num = (int) (Math.random() * 9);
    frame_time_left = SECONDS_PER_FRAME;
    if (source.getType().equals(ObjectType.Pyro)) {
      time_until_fully_armed = TIME_TO_FULLY_ARM;
    }
    else {
      is_fully_armed = true;
    }
  }

  @Override
  public ObjectType getType() {
    return ObjectType.ProximityBomb;
  }

  @Override
  public void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell) {
    Point center_pixel = MapUtils.coordsToPixel(x_loc, y_loc, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    Image image = images.getImage(image_name, frame_num);
    g.drawImage(image, center_pixel.x - image.getWidth(null) / 2, center_pixel.y - image.getHeight(null) / 2,
            null);
  }

  @Override
  public void planNextAction(double s_elapsed) {

  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    frame_time_left -= s_elapsed;
    if (frame_time_left < 0.0) {
      ++frame_num;
      frame_time_left = SECONDS_PER_FRAME;
    }

    if (!is_fully_armed) {
      time_until_fully_armed -= s_elapsed;
      if (time_until_fully_armed < 0.0) {
        is_fully_armed = true;
      }
    }

    for (Pyro pyro : room.getPyros()) {
      if ((is_fully_armed || !pyro.equals(source)) && isUnitInRange(pyro)) {
        return handleDetonation(engine, pyro);
      }
    }
    for (Unit unit : room.getRobots()) {
      if (isUnitInRange(unit)) {
        return handleDetonation(engine, unit);
      }
    }

    for (Shot shot : room.getShots()) {
      if (Math.hypot(shot.getX() - x_loc, shot.getY() - y_loc) < radius) {
        shot.detonate();
        return handleDetonation(engine, null);
      }
    }

    return null;
  }

  public boolean isUnitInRange(Unit unit) {
    double combined_radius = unit.getRadius() + radius;
    return !unit.equals(source) && Math.abs(unit.getX() - x_loc) < combined_radius &&
            Math.abs(unit.getY() - y_loc) < combined_radius;
  }

  public MapObject handleDetonation(MapEngine engine, Unit hit_unit) {
    if (hit_unit != null) {
      hit_unit.beDamaged(engine, damage, false);
    }
    is_in_map = false;
    room.doSplashDamage(this, damage, SPLASH_DAMAGE_RADIUS, hit_unit);
    playSound(engine, "weapons/explode1.wav");
    return new Explosion(room, x_loc, y_loc, EXPLOSION_RADIUS, EXPLOSION_TIME);
  }
}
