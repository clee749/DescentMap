package mapobject.unit.pyro;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;

import mapobject.unit.Unit;
import pilot.PyroPilot;
import structure.Room;
import util.ImageHandler;

import common.Constants;
import common.MapUtils;
import common.ObjectType;

public class Pyro extends Unit {

  public Pyro(Room room, double x_loc, double y_loc) {
    super(Constants.getMoveSpeed(ObjectType.Pyro), Constants.getTurnSpeed(ObjectType.Pyro), Constants
            .getRadius(ObjectType.Pyro), room, x_loc, y_loc);
    pilot = new PyroPilot(this);
    ((PyroPilot) pilot).startPilot();
  }

  @Override
  public void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_corner_pixel,
          int pixels_per_cell) {
    Point center_pixel =
            MapUtils.coordsToPixel(x_loc, y_loc, ref_cell, ref_cell_corner_pixel, pixels_per_cell);
    Image image = images.getImage(ObjectType.Pyro, direction);
    g.drawImage(image, center_pixel.x - image.getWidth(null) / 2, center_pixel.y - image.getHeight(null) / 2,
            null);

    Point target_pixel =
            MapUtils.coordsToPixel(pilot.getTargetX(), pilot.getTargetY(), ref_cell, ref_cell_corner_pixel,
                    pixels_per_cell);
    g.setColor(Color.yellow);
    g.drawRect(target_pixel.x - 1, target_pixel.y - 1, 2, 2);
  }
}
