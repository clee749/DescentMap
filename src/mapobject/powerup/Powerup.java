package mapobject.powerup;

import java.awt.Image;

import mapobject.MapObject;
import mapobject.MovableObject;
import mapobject.unit.Pyro;
import pilot.PowerupPilot;
import resource.ImageHandler;
import structure.Room;
import util.MapUtils;

import common.RoomSide;
import component.MapEngine;

public abstract class Powerup extends MovableObject {
  public static final double RADIUS = 0.12;
  public static final double SECONDS_PER_FRAME = 0.03;
  public static final double MOVE_SPEED_DECELERATION = 0.5;

  protected int frame_num;
  protected double frame_time_left;

  public Powerup(Room room, double x_loc, double y_loc, double direction, double speed) {
    super(RADIUS, new PowerupPilot(), room, x_loc, y_loc, direction, speed, 0.0);
    frame_num = (int) (Math.random() * 9);
    frame_time_left = SECONDS_PER_FRAME;
  }

  @Override
  public Image getImage(ImageHandler images) {
    return images.getImage(image_name, frame_num);
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    MapObject created_object = super.doNextAction(engine, s_elapsed);

    if (frame_time_left < 0.0) {
      ++frame_num;
      frame_time_left = SECONDS_PER_FRAME;
    }
    else {
      frame_time_left -= s_elapsed;
    }

    if (move_speed > 0.0) {
      move_speed = Math.max(move_speed - MOVE_SPEED_DECELERATION * s_elapsed, 0.0);
    }

    for (Pyro pyro : room.getPyros()) {
      double pyro_radius = pyro.getRadius();
      if (pyro.getShields() >= 0 && Math.abs(x_loc - pyro.getX()) < pyro_radius &&
              Math.abs(y_loc - pyro.getY()) < pyro_radius && beAcquired(pyro)) {
        is_in_map = false;
        break;
      }
    }
    return created_object;
  }

  @Override
  public void handleHittingWall(RoomSide wall_side) {
    super.handleHittingWall(wall_side);
    if (wall_side.equals(RoomSide.NORTH) || wall_side.equals(RoomSide.SOUTH)) {
      direction += 2 * (Math.PI - direction);
    }
    else {
      direction = MapUtils.normalizeAngle(Math.PI - direction);
    }
  }

  public abstract boolean beAcquired(Pyro pyro);
}
