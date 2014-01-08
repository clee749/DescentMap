package mapobject.unit.pyro;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

import mapobject.MapObject;
import mapobject.MultipleObject;
import mapobject.shot.LaserShot;
import mapobject.unit.Unit;
import pilot.PyroPilot;
import structure.Room;
import util.MapUtils;

import common.Constants;
import common.ObjectType;

import external.ImageHandler;

public class Pyro extends Unit {
  private final double outer_cannon_offset;
  private final double cannon_forward_offset;
  private final boolean has_quad_lasers;

  public Pyro(Room room, double x_loc, double y_loc, double direction) {
    super(new PyroPilot(), room, x_loc, y_loc, direction);
    outer_cannon_offset = Constants.PYRO_OUTER_CANNON_OFFSET * radius;
    cannon_forward_offset = Constants.PYRO_CANNON_FORWARD_OFFSET * radius;
    ((PyroPilot) pilot).startPilot();
    has_quad_lasers = true;
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
  public MapObject fireCannon() {
    MultipleObject shots = new MultipleObject();
    Point2D.Double abs_offset = findRightShotAbsOffset(cannon_offset);
    shots.addObject(new LaserShot(this, room, x_loc + abs_offset.x, y_loc + abs_offset.y, direction, 1));
    shots.addObject(new LaserShot(this, room, x_loc - abs_offset.x, y_loc - abs_offset.y, direction, 1));
    if (has_quad_lasers) {
      abs_offset = findRightShotAbsOffset(outer_cannon_offset);
      double x_forward_abs_offset = Math.cos(direction) * cannon_forward_offset;
      double y_forward_abs_offset = Math.sin(direction) * cannon_forward_offset;
      shots.addObject(new LaserShot(this, room, x_loc + abs_offset.x + x_forward_abs_offset, y_loc +
              abs_offset.y + y_forward_abs_offset, direction, 1));
      shots.addObject(new LaserShot(this, room, x_loc - abs_offset.x + x_forward_abs_offset, y_loc -
              abs_offset.y + y_forward_abs_offset, direction, 1));
    }
    return shots;
  }
}
