package mapobject;

import java.awt.Image;

import mapobject.ephemeral.Explosion;
import mapobject.powerup.Powerup;
import mapobject.shot.Shot;
import mapobject.unit.Pyro;
import mapobject.unit.Unit;
import mapobject.unit.robot.Robot;
import pilot.PowerupPilot;
import resource.ImageHandler;
import structure.Room;
import util.MapUtils;

import common.ObjectType;
import common.RoomSide;
import component.MapEngine;

public class ProximityBomb extends MovableObject {
  public static final double RADIUS = 0.12;
  public static final double SECONDS_PER_FRAME = Powerup.SECONDS_PER_FRAME;
  public static final int DAMAGE = 27;
  public static final double SPLASH_DAMAGE_RADIUS = 1.0;
  public static final double EXPLOSION_RADIUS = RADIUS * 2;
  public static final double EXPLOSION_TIME = 0.75;
  public static final double TIME_TO_FULLY_ARM = 5.0;
  public static final double MOVE_SPEED_INCREASE_PER_DAMAGE = 0.1;
  public static final double MOVE_SPEED_DECELERATION = Powerup.MOVE_SPEED_DECELERATION;

  private final MapObject source;
  private final int damage;
  private final double source_combined_radius;
  private int frame_num;
  private double frame_time_left;
  private boolean is_fully_armed;
  private double time_until_fully_armed;

  public ProximityBomb(MapObject source, int damage, Room room, double x_loc, double y_loc) {
    super(RADIUS, new PowerupPilot(), room, x_loc, y_loc, 0.0, 0.0, 0.0);
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
  public Image getImage(ImageHandler images) {
    return images.getImage(image_name, frame_num);
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    MultipleObject created_objects = new MultipleObject();
    created_objects.addObject(super.doNextAction(engine, s_elapsed));

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

    if (move_speed > 0.0) {
      move_speed = Math.max(move_speed - MOVE_SPEED_DECELERATION * s_elapsed, 0.0);
    }

    boolean is_outside_source =
            Math.abs(source.getX() - x_loc) > source_combined_radius ||
                    Math.abs(source.getY() - y_loc) > source_combined_radius;
    created_objects.addObject(handleAnyCollisions(engine, room, is_outside_source));
    if (!is_in_map) {
      return created_objects;
    }

    RoomSide close_neighbor_side = MapUtils.findRoomBorderSide(this, Unit.LARGEST_UNIT_RADIUS);
    if (close_neighbor_side != null) {
      Room neighbor = room.getNeighborInDirection(close_neighbor_side);
      if (neighbor != null) {
        created_objects.addObject(handleAnyCollisions(engine, neighbor, is_outside_source));
      }
    }

    return created_objects;
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

  @Override
  public void handleHittingWall(RoomSide wall_side) {
    super.handleHittingWall(wall_side);
    bounceOffWall(wall_side);
  }

  public void handleSplashDamage(int amount, double direction) {
    double new_move_speed = amount * MOVE_SPEED_INCREASE_PER_DAMAGE;
    if (new_move_speed > move_speed) {
      move_speed = new_move_speed;
      this.direction = direction;
      pilot.startPilot();
    }
  }
}
