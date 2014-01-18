package mapobject.unit.pyro;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Point2D;

import mapobject.MapObject;
import mapobject.MultipleObject;
import mapobject.ephemeral.Explosion;
import mapobject.unit.Unit;
import pilot.Pilot;
import pilot.PilotAction;
import pilot.PyroPilot;
import pilot.TurnDirection;
import structure.Room;
import util.MapUtils;
import cannon.Cannon;
import cannon.LaserCannon;

import common.Constants;
import common.ObjectType;
import component.MapEngine;

import external.ImageHandler;

public class Pyro extends Unit {
  private final double outer_cannon_offset;
  private final double cannon_forward_offset;
  private final boolean has_quad_lasers;
  private boolean death_spin_started;
  private double death_spin_time_left;
  private double death_spin_direction;
  private double death_spin_delta_direction;
  private final Cannon selected_primary_cannon;

  public Pyro(Pilot pilot, Room room, double x_loc, double y_loc, double direction) {
    super(pilot, room, x_loc, y_loc, direction);
    outer_cannon_offset = Constants.PYRO_OUTER_CANNON_OFFSET * radius;
    cannon_forward_offset = Constants.PYRO_CANNON_FORWARD_OFFSET * radius;
    ((PyroPilot) pilot).startPilot();
    has_quad_lasers = true;
    reload_time = 0.25;
    selected_primary_cannon = new LaserCannon(Constants.getDamage(ObjectType.LaserShot), 1);
  }

  public Pyro(Room room, double x_loc, double y_loc, double direction) {
    this(new PyroPilot(), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Pyro;
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
    super.paint(g, images, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    Point target_pixel =
            MapUtils.coordsToPixel(pilot.getTargetX(), pilot.getTargetY(), ref_cell, ref_cell_nw_pixel,
                    pixels_per_cell);
    g.setColor(Color.yellow);
    g.drawRect(target_pixel.x - 1, target_pixel.y - 1, 2, 2);
  }

  @Override
  public void planNextAction(double s_elapsed) {
    if (shields < 0) {
      next_action = PilotAction.MOVE_FORWARD;
      return;
    }
    super.planNextAction(s_elapsed);
    if (next_action.fire_cannon && reload_time_left < 0.0) {
      planToFireCannon();
    }
    else {
      handleCannonReload(s_elapsed);
    }
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    if (shields < 0) {
      return doNextDeathSpinAction(engine, s_elapsed);
    }
    MapObject object_created = super.doNextAction(engine, s_elapsed);
    if (firing_cannon) {
      firing_cannon = false;
      return fireCannon();
    }
    return object_created;
  }

  public MapObject doNextDeathSpinAction(MapEngine engine, double s_elapsed) {
    if (!death_spin_started) {
      death_spin_started = true;
      death_spin_direction = direction;
      death_spin_time_left = Constants.PYRO_DEATH_SPIN_TIME;
      move_speed /= Constants.PYRO_DEATH_SPIN_MOVE_SPEED_DIVISOR;
      death_spin_delta_direction = turn_speed * Constants.PYRO_DEATH_SPIN_TURN_SPEED_MULTIPLIER;
      if (next_action.turn.equals(TurnDirection.CLOCKWISE) ||
              (next_action.turn.equals(TurnDirection.NONE) && Math.random() < 0.5)) {
        death_spin_delta_direction *= -1;
      }
    }
    else {
      death_spin_direction =
              MapUtils.normalizeAngle(death_spin_direction + death_spin_delta_direction * s_elapsed);
      if (death_spin_time_left < 0.0) {
        return handleDeath(s_elapsed);
      }
      if (!doNextMovement(engine, s_elapsed)) {
        death_spin_time_left = 0.0;
      }
      death_spin_time_left -= s_elapsed;
    }
    return new Explosion(room, x_loc + Math.random() * 2 * radius - radius, y_loc + Math.random() * 2 *
            radius - radius, Constants.PYRO_DEATH_SPIN_EXPLOSION_RADIUS,
            Constants.PYRO_DEATH_SPIN_EXPLOSION_TIME);
  }

  @Override
  public MapObject fireCannon() {
    MultipleObject shots = new MultipleObject();
    Point2D.Double abs_offset = MapUtils.perpendicularVector(cannon_offset, direction);
    shots.addObject(selected_primary_cannon.fireCannon(this, room, x_loc + abs_offset.x,
            y_loc + abs_offset.y, direction));
    shots.addObject(selected_primary_cannon.fireCannon(this, room, x_loc - abs_offset.x,
            y_loc - abs_offset.y, direction));
    if (has_quad_lasers && selected_primary_cannon instanceof LaserCannon) {
      abs_offset = MapUtils.perpendicularVector(outer_cannon_offset, direction);
      double x_forward_abs_offset = Math.cos(direction) * cannon_forward_offset;
      double y_forward_abs_offset = Math.sin(direction) * cannon_forward_offset;
      shots.addObject(selected_primary_cannon.fireCannon(this, room, x_loc + abs_offset.x +
              x_forward_abs_offset, y_loc + abs_offset.y + y_forward_abs_offset, direction));
      shots.addObject(selected_primary_cannon.fireCannon(this, room, x_loc - abs_offset.x +
              x_forward_abs_offset, y_loc - abs_offset.y + y_forward_abs_offset, direction));
    }
    return shots;
  }

  @Override
  public MapObject releasePowerups() {
    return null;
  }

  public boolean acquireShield(int amount) {
    if (shields < Constants.PYRO_MAX_SHIELDS) {
      shields = Math.min(shields + amount, Constants.PYRO_MAX_SHIELDS);
      return true;
    }
    return false;
  }
}
