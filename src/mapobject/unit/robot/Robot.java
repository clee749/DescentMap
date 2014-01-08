package mapobject.unit.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

import mapobject.MapObject;
import mapobject.shot.LaserShot;
import mapobject.unit.Unit;
import pilot.Pilot;
import structure.Room;
import util.MapUtils;
import external.ImageHandler;

public abstract class Robot extends Unit {
  protected int cannon_side;

  public Robot(Pilot pilot, Room room, double x_loc, double y_loc, double direction) {
    super(pilot, room, x_loc, y_loc, direction);
    cannon_side = (int) (Math.random() * 2);
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
  public MapObject fireCannon() {
    ++cannon_side;
    Point2D.Double abs_offset = findRightShotAbsOffset(cannon_offset);
    if (cannon_side % 2 == 0) {
      return new LaserShot(this, room, x_loc + abs_offset.x, y_loc + abs_offset.y, direction, 2);
    }
    return new LaserShot(this, room, x_loc - abs_offset.x, y_loc - abs_offset.y, direction, 2);
  }
}
