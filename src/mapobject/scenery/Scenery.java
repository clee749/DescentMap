package mapobject.scenery;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;

import mapobject.MapObject;
import structure.Room;
import util.MapUtils;
import external.ImageHandler;

public abstract class Scenery extends MapObject {
  protected final double nw_corner_x;
  protected final double nw_corner_y;

  public Scenery(Room room, double x_loc, double y_loc) {
    super(0.5, room, x_loc, y_loc);
    nw_corner_x = x_loc - 0.5;
    nw_corner_y = y_loc - 0.5;
  }

  @Override
  public void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell) {
    Point nw_pixel =
            MapUtils.coordsToPixel(nw_corner_x, nw_corner_y, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    Image image = images.getImage(image_name);
    g.drawImage(image, nw_pixel.x, nw_pixel.y, null);
  }
}
