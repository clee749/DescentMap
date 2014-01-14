package mapobject.unit.pyro;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

import mapobject.MapObject;
import mapobject.MultipleObject;
import mapobject.shot.LaserShot;
import mapobject.unit.Unit;
import pilot.Pilot;
import pilot.PyroPilot;
import structure.Room;
import util.MapUtils;

import common.Constants;
import common.ObjectType;
import component.MapEngine;

import external.ImageHandler;

public class Pyro extends Unit {
  private final double outer_cannon_offset;
  private final double cannon_forward_offset;
  private final boolean has_quad_lasers;

  public Pyro(Pilot pilot, Room room, double x_loc, double y_loc, double direction) {
    super(pilot, room, x_loc, y_loc, direction);
    outer_cannon_offset = Constants.PYRO_OUTER_CANNON_OFFSET * radius;
    cannon_forward_offset = Constants.PYRO_CANNON_FORWARD_OFFSET * radius;
    ((PyroPilot) pilot).startPilot();
    has_quad_lasers = true;
    reload_time = 0.25;
  }

  public Pyro(Room room, double x_loc, double y_loc, double direction) {
    this(new PyroPilot(), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Pyro;
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
    MapObject object_created = super.doNextAction(engine, s_elapsed);
    if (firing_cannon) {
      firing_cannon = false;
      return fireCannon();
    }
    return object_created;
  }

  @Override
  public MapObject fireCannon() {
    MultipleObject shots = new MultipleObject();
    Point2D.Double abs_offset = MapUtils.perpendicularVector(cannon_offset, direction);
    shots.addObject(new LaserShot(this, room, x_loc + abs_offset.x, y_loc + abs_offset.y, direction, 1));
    shots.addObject(new LaserShot(this, room, x_loc - abs_offset.x, y_loc - abs_offset.y, direction, 1));
    if (has_quad_lasers) {
      abs_offset = MapUtils.perpendicularVector(outer_cannon_offset, direction);
      double x_forward_abs_offset = Math.cos(direction) * cannon_forward_offset;
      double y_forward_abs_offset = Math.sin(direction) * cannon_forward_offset;
      shots.addObject(new LaserShot(this, room, x_loc + abs_offset.x + x_forward_abs_offset, y_loc +
              abs_offset.y + y_forward_abs_offset, direction, 1));
      shots.addObject(new LaserShot(this, room, x_loc - abs_offset.x + x_forward_abs_offset, y_loc -
              abs_offset.y + y_forward_abs_offset, direction, 1));
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
