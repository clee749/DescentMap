package mapobject.unit.robot;

import external.ImageHandler;
import gunner.Gunner;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import mapobject.MapObject;
import mapobject.unit.Unit;
import pilot.Pilot;
import structure.Room;
import util.MapUtils;

public abstract class Robot extends Unit {

  public Robot(Pilot pilot, Gunner gunner, Room room, double x_loc, double y_loc, double direction) {
    super(pilot, gunner, room, x_loc, y_loc, direction);
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
    return null;
  }
}
