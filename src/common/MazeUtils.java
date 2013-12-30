package common;

import java.awt.Point;

public class MazeUtils {
  protected MazeUtils() {

  }

  public static Point coordsToPixel(Point coords, Point ref_cell, Point ref_cell_corner_pixel,
          int pixels_per_cell) {
    return new Point(ref_cell_corner_pixel.x - (ref_cell.x - coords.x) * pixels_per_cell,
            ref_cell_corner_pixel.y - (ref_cell.y - coords.y) * pixels_per_cell);
  }

  public static Point coordsToPixel(double x_coord, double y_coord, Point ref_cell,
          Point ref_cell_corner_pixel, int pixels_per_cell) {
    return new Point((int) (ref_cell_corner_pixel.x - (ref_cell.x - x_coord) * pixels_per_cell),
            (int) (ref_cell_corner_pixel.y - (ref_cell.y - y_coord) * pixels_per_cell));
  }
}
