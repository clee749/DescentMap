package mapobject.unit.pyro;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import mapobject.unit.Unit;
import pilot.PyroPilot;
import structure.Room;

import common.Constants;
import common.MapUtils;

public class Pyro extends Unit {

  public Pyro(Room room, double x_loc, double y_loc) {
    super(Constants.PYRO_MOVE_SPEED, Constants.PYRO_TURN_SPEED, Constants.PYRO_RADIUS, room, x_loc, y_loc);
    pilot = new PyroPilot(this);
    ((PyroPilot) pilot).startPilot();
  }

  @Override
  public void paint(Graphics2D g, Point ref_cell, Point ref_cell_corner_pixel, int pixels_per_cell) {
    Point center_pixel =
            MapUtils.coordsToPixel(x_loc, y_loc, ref_cell, ref_cell_corner_pixel, pixels_per_cell);
    int radius_in_pixels = (int) (radius * pixels_per_cell);
    g.setColor(Color.green);
    g.setStroke(new BasicStroke(1));
    g.drawRect(center_pixel.x - radius_in_pixels, center_pixel.y - radius_in_pixels, 2 * radius_in_pixels,
            2 * radius_in_pixels);

    g.setColor(Color.red);
    g.drawLine(center_pixel.x, center_pixel.y, center_pixel.x + (int) (Math.cos(direction) * 10),
            center_pixel.y + (int) (Math.sin(direction) * 10));

    g.setColor(Color.yellow);
    Point target_pixel =
            MapUtils.coordsToPixel(pilot.getTargetX(), pilot.getTargetY(), ref_cell, ref_cell_corner_pixel,
                    pixels_per_cell);
    g.setColor(Color.yellow);
    g.drawRect(target_pixel.x - 1, target_pixel.y - 1, 2, 2);
  }
}
