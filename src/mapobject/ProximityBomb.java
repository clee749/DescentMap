package mapobject;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;

import mapobject.ephemeral.Explosion;
import mapobject.powerup.Powerup;
import mapobject.shot.Shot;
import mapobject.unit.Pyro;
import mapobject.unit.Unit;
import mapobject.unit.robot.Robot;
import resource.ImageHandler;
import structure.Room;
import util.MapUtils;

import common.ObjectType;
import common.RoomSide;
import component.MapEngine;

public class ProximityBomb extends MapObject {
  public static final double RADIUS = 0.12;
  public static final double SECONDS_PER_FRAME = Powerup.SECONDS_PER_FRAME;
  public static final int DAMAGE = 27;
  public static final double SPLASH_DAMAGE_RADIUS = 1.0;
  public static final double EXPLOSION_RADIUS = RADIUS * 2;
  public static final double EXPLOSION_TIME = 0.75;
  public static final double TIME_TO_FULLY_ARM = 5.0;

  private final MapObject source;
  private final int damage;
  private final double source_combined_radius;
  private int frame_num;
  private double frame_time_left;
  private boolean is_fully_armed;
  private double time_until_fully_armed;

  public ProximityBomb(MapObject source, int damage, Room room, double x_loc, double y_loc) {
    super(RADIUS, room, x_loc, y_loc);
    this.source = source;
    this.damage = damage;
    source_combined_radius = source.getRadius() + radius;
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

    boolean is_outside_source =
            Math.abs(source.getX() - x_loc) > source_combined_radius ||
                    Math.abs(source.getY() - y_loc) > source_combined_radius;
    MapObject created_object = handleAnyCollisions(engine, room, is_outside_source);
    if (created_object != null) {
      return created_object;
    }

    RoomSide close_neighbor_side = MapUtils.findRoomBorderSide(this, Unit.LARGEST_UNIT_RADIUS);
    if (close_neighbor_side != null) {
      Room neighbor = room.getNeighborInDirection(close_neighbor_side);
      if (neighbor != null) {
        return handleAnyCollisions(engine, neighbor, is_outside_source);
      }
    }

    return null;
  }

  public MapObject handleAnyCollisions(MapEngine engine, Room room, boolean is_outside_source) {
    for (Pyro pyro : room.getPyros()) {
      if ((is_fully_armed || !pyro.equals(source)) && MapUtils.objectsIntersect(this, pyro)) {
        return handleDetonation(engine, pyro);
      }
    }
    for (Robot robot : room.getRobots()) {
      if (!robot.equals(source) && MapUtils.objectsIntersect(this, robot)) {
        return handleDetonation(engine, robot);
      }
    }
    for (Shot shot : room.getShots()) {
      if ((is_outside_source || !shot.getSource().equals(source)) && MapUtils.objectsIntersect(this, shot)) {
        shot.detonate();
        return handleDetonation(engine, null);
      }
    }
    return null;
  }

  public MapObject handleDetonation(MapEngine engine, Unit hit_unit) {
    if (hit_unit != null) {
      hit_unit.beDamaged(engine, damage, true);
    }
    is_in_map = false;
    room.doSplashDamage(this, damage, SPLASH_DAMAGE_RADIUS, hit_unit);
    playSound(engine, "weapons/explode1.wav");
    return new Explosion(room, x_loc, y_loc, EXPLOSION_RADIUS, EXPLOSION_TIME);
  }
}
