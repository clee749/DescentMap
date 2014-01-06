package util;

import java.awt.Point;

public class MapUtils {
  public static final double PI_OVER_TWO = Math.PI / 2;
  public static final double THREE_PI_OVER_TWO = 3 * Math.PI / 2;
  public static final double TWO_PI = 2 * Math.PI;

  protected MapUtils() {

  }

  public static Point coordsToPixel(Point coords, Point ref_cell, Point ref_cell_nw_pixel, int pixels_per_cell) {
    return new Point(ref_cell_nw_pixel.x - (ref_cell.x - coords.x) * pixels_per_cell, ref_cell_nw_pixel.y -
            (ref_cell.y - coords.y) * pixels_per_cell);
  }

  public static Point coordsToPixel(double x_coord, double y_coord, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell) {
    return new Point((int) (ref_cell_nw_pixel.x - (ref_cell.x - x_coord) * pixels_per_cell),
            (int) (ref_cell_nw_pixel.y - (ref_cell.y - y_coord) * pixels_per_cell));
  }

  /**
   * 
   * @param src_x x-coordinate of source vector (from origin)
   * @param src_y y-coordinate of source vector
   * @param dst_x x-coordinate of destination vector (from origin)
   * @param dst_y y-coordinate of destination vector
   * @return smallest angle from source vector to destination vector
   */
  public static double angleTo(double src_x, double src_y, double dst_x, double dst_y) {
    return -Math.atan2(src_x * dst_y - dst_x * src_y, src_x * dst_x + src_y * dst_y);
  }

  /**
   * 
   * @param src_direction direction of source vector
   * @param dst_x x-coordinate of destination vector (from origin)
   * @param dst_y y-coordinate of destination vector
   * @return smallest angle from source vector to destination vector
   */
  public static double angleTo(double src_direction, double dst_x, double dst_y) {
    return MapUtils.angleTo(Math.cos(src_direction), Math.sin(src_direction), dst_x, dst_y);
  }

  /**
   * 
   * @param src_direction direction of source vector
   * @param dst_direction direction of destination vector
   * @return smallest angle from source vector to destination vector
   */
  public static double angleTo(double src_direction, double dst_direction) {
    return MapUtils.angleTo(Math.cos(src_direction), Math.sin(src_direction), Math.cos(dst_direction),
            Math.sin(dst_direction));
  }

  /**
   * 
   * @param src_x x-coordinate of source point
   * @param src_y y-coordinate of source point
   * @param dst_x x-coordinate of destination point
   * @param dst_y y-coordinate of destination point
   * @return direction from source point to destination point
   */
  public static double absoluteAngleTo(double src_x, double src_y, double dst_x, double dst_y) {
    return -MapUtils.normalizeAngle(Math.atan2(dst_y - src_y, dst_x - src_x));
  }

  public static double normalizeAngle(double angle) {
    if (angle < 0) {
      return angle + MapUtils.TWO_PI;
    }
    if (angle >= MapUtils.TWO_PI) {
      return angle - MapUtils.TWO_PI;
    }
    return angle;
  }

  public static boolean rectanglesIntersect(Point nw_corner1, Point se_corner1, Point nw_corner2,
          Point se_corner2) {
    return se_corner1.x >= nw_corner2.x && nw_corner1.x <= se_corner2.x && se_corner1.y >= nw_corner2.y &&
            nw_corner1.y <= se_corner2.y;
  }
}
