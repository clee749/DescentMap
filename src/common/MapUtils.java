package common;

import java.awt.Point;

public class MapUtils {
  protected MapUtils() {

  }

  public static void coordsToPixel() {
    // screenX = zoom_factor*cartX + screen_width/2 + offsetX
    // screenY = screen_height/2 - zoom_factor*cartY + offsetY
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

  public static double angleTo(double src_x, double src_y, double dst_x, double dst_y) {
    return -Math.atan2(src_x * dst_y - dst_x * src_y, src_x * dst_x + src_y * dst_y);
  }

  public static double angleTo(double direction, double dst_x, double dst_y) {
    return MapUtils.angleTo(Math.cos(direction), Math.sin(direction), dst_x, dst_y);
  }

  public static double angleTo(double src_direction, double dst_direction) {
    return MapUtils.angleTo(Math.cos(src_direction), Math.sin(src_direction), Math.cos(dst_direction),
            Math.sin(dst_direction));
  }

  public static double normalizeAngle(double angle) {
    if (angle < 0) {
      return angle + 2 * Math.PI;
    }
    if (angle >= 2 * Math.PI) {
      return angle - 2 * Math.PI;
    }
    return angle;
  }

  public static boolean rectanglesIntersect(Point nw_corner1, Point se_corner1, Point nw_corner2,
          Point se_corner2) {
    return se_corner1.x >= nw_corner2.x && nw_corner1.x <= se_corner2.x && se_corner1.y >= nw_corner2.y &&
            nw_corner1.y <= se_corner2.y;
  }
}
