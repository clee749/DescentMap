package util;

import java.awt.Point;
import java.awt.geom.Point2D;

import mapobject.MapObject;
import mapobject.MovableObject;
import structure.Room;
import structure.RoomConnection;

import common.DescentMapException;
import common.RoomSide;

public class MapUtils {
  public static final double PI_OVER_FOUR = Math.PI / 4;
  public static final double PI_OVER_TWO = Math.PI / 2;
  public static final double THREE_PI_OVER_FOUR = 3 * Math.PI / 4;
  public static final double FIVE_PI_OVER_FOUR = 5 * Math.PI / 4;
  public static final double THREE_PI_OVER_TWO = 3 * Math.PI / 2;
  public static final double SEVEN_PI_OVER_FOUR = 7 * Math.PI / 4;
  public static final double TWO_PI = 2 * Math.PI;

  protected MapUtils() {

  }

  /**
   * 
   * @param coords coordinates in a DescentMap
   * @param ref_cell any location
   * @param ref_cell_nw_pixel the pixel that marks the NW corner of ref_cell
   * @param pixels_per_cell length of a cell in pixels
   * @return pixel coordinates where coords should be drawn
   */
  public static Point coordsToPixel(Point coords, Point ref_cell, Point ref_cell_nw_pixel, int pixels_per_cell) {
    return new Point(ref_cell_nw_pixel.x - (ref_cell.x - coords.x) * pixels_per_cell, ref_cell_nw_pixel.y -
            (ref_cell.y - coords.y) * pixels_per_cell);
  }

  /**
   * 
   * @param x_coord x-coordinate of the location in a DescentMap
   * @param y_coord y-coordinate of the location in a DescentMap
   * @param ref_cell any location
   * @param ref_cell_nw_pixel the pixel that marks the NW corner of ref_cell
   * @param pixels_per_cell length of a cell in pixels
   * @return pixel coordinates where the point should be drawn
   */
  public static Point coordsToPixel(double x_coord, double y_coord, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell) {
    return new Point((int) (ref_cell_nw_pixel.x - (ref_cell.x - x_coord) * pixels_per_cell),
            (int) (ref_cell_nw_pixel.y - (ref_cell.y - y_coord) * pixels_per_cell));
  }

  /**
   * 
   * @param x_coord x-coordinate of the location in a DescentMap
   * @param y_coord y-coordinate of the location in a DescentMap
   * @param ref_cell any location
   * @param ref_cell_nw_pixel the pixel that marks the NW corner of ref_cell
   * @param pixels_per_cell length of a cell in pixels
   * @return rounded pixel coordinates where the point should be drawn
   */
  public static Point coordsToPixelRounded(double x_coord, double y_coord, Point ref_cell,
          Point ref_cell_nw_pixel, int pixels_per_cell) {
    return new Point((int) Math.round(ref_cell_nw_pixel.x - (ref_cell.x - x_coord) * pixels_per_cell),
            (int) Math.round(ref_cell_nw_pixel.y - (ref_cell.y - y_coord) * pixels_per_cell));
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
    return angleTo(Math.cos(src_direction), Math.sin(src_direction), dst_x, dst_y);
  }

  /**
   * 
   * @param src_object any MovableObject with location and direction
   * @param dst_object any MapObject
   * @return smallest angle from src_object's direction to dst_object
   */
  public static double angleTo(MovableObject src_object, MapObject dst_object) {
    return angleTo(src_object.getDirection(), dst_object.getX() - src_object.getX(), dst_object.getY() -
            src_object.getY());
  }

  /**
   * 
   * @param src_direction direction of source vector
   * @param dst_direction direction of destination vector
   * @return smallest angle from source vector to destination vector
   */
  public static double angleTo(double src_direction, double dst_direction) {
    return angleTo(Math.cos(src_direction), Math.sin(src_direction), Math.cos(dst_direction),
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
    return -normalizeAngle(Math.atan2(dst_y - src_y, dst_x - src_x));
  }

  /**
   * 
   * @param angle angle in range [-2 * Math.PI, 4 * Math.PI)
   * @return congruent angle normalized to range [0, 2 * Math.PI)
   */
  public static double normalizeAngle(double angle) {
    if (angle < 0) {
      return angle + TWO_PI;
    }
    if (angle >= TWO_PI) {
      return angle - TWO_PI;
    }
    return angle;
  }

  /**
   * 
   * @param nw_corner1 NW corner of first rectangle
   * @param se_corner1 SE corner of first rectangle
   * @param nw_corner2 NW corner of second rectangle
   * @param se_corner2 SE corner of second rectangle
   * @return true if the rectangles intersect, false otherwise
   */
  public static boolean rectanglesIntersect(Point nw_corner1, Point se_corner1, Point nw_corner2,
          Point se_corner2) {
    return se_corner1.x >= nw_corner2.x && nw_corner1.x <= se_corner2.x && se_corner1.y >= nw_corner2.y &&
            nw_corner1.y <= se_corner2.y;
  }

  /**
   * 
   * @param nw_corner NW corner of Room
   * @param se_corner SE corner of Room
   * @param point_radius min distance of random point from a wall of the Room
   * @return random point inside the Room at least radius distance from a wall
   */
  public static Point2D.Double randomInternalPoint(Point nw_corner, Point se_corner, double point_radius) {
    double x_range = se_corner.x - nw_corner.x - 2 * point_radius;
    double y_range = se_corner.y - nw_corner.y - 2 * point_radius;
    return new Point2D.Double(nw_corner.x + point_radius + Math.random() * x_range, nw_corner.y +
            point_radius + Math.random() * y_range);
  }

  /**
   * 
   * @param test_angle angle in range [-Math.PI, Math.PI]
   * @param angle1 angle in same range
   * @param angle2 angle in same range
   * @return true if test_angle is between angle1 and angle2 exclusive, false otherwise
   */
  public static boolean isAngleBetween(double test_angle, double angle1, double angle2) {
    return (angle1 < test_angle && test_angle < angle2) || (angle2 < test_angle && test_angle < angle1);
  }

  /**
   * 
   * @param magnitude magnitude of resultant vector
   * @param direction any angle
   * @return vector with magnitude magnitude and direction perpendicular to direction
   */
  public static Point2D.Double perpendicularVector(double magnitude, double direction) {
    double axis = direction + MapUtils.PI_OVER_TWO;
    return new Point2D.Double(Math.cos(axis) * magnitude, Math.sin(axis) * magnitude);
  }

  /**
   * 
   * @param obj1 any MapObject
   * @param obj2 any MapObject
   * @return squared distance between the two MapObjects
   */
  public static double distance2(MapObject obj1, MapObject obj2) {
    double dx = obj2.getX() - obj1.getX();
    double dy = obj2.getY() - obj1.getY();
    return dx * dx + dy * dy;
  }

  /**
   * 
   * @param src any MapObject
   * @param neighbor_side the RoomSide of src's neighbor Room
   * @return the angles from src in the direction returned by
   *         RoomSide.directionToRadians(neighbor_side) to the corners between the Rooms in the
   *         format (min angle, max angle), use RoomSide.directionToRadians(neighbor_side) on other
   *         angles before comparing
   */
  public static Point2D.Double anglesToNeighborConnectionPoints(MapObject src, RoomSide neighbor_side) {
    double src_x = src.getX();
    double src_y = src.getY();
    Room src_room = src.getRoom();
    RoomConnection connection = src_room.getConnectionInDirection(neighbor_side);
    double neighbor_direction = RoomSide.directionToRadians(neighbor_side);
    double angle_to_connection_min;
    double angle_to_connection_max;
    switch (neighbor_side) {
      case NORTH:
        double dy = src_room.getNWCorner().y - src_y;
        angle_to_connection_min = angleTo(neighbor_direction, connection.min - src_x, dy);
        angle_to_connection_max = angleTo(neighbor_direction, connection.max - src_x, dy);
        break;
      case SOUTH:
        dy = src_room.getSECorner().y - src_y;
        angle_to_connection_min = angleTo(neighbor_direction, connection.min - src_x, dy);
        angle_to_connection_max = angleTo(neighbor_direction, connection.max - src_x, dy);
        break;
      case WEST:
        double dx = src_room.getNWCorner().x - src_x;
        angle_to_connection_min = angleTo(neighbor_direction, dx, connection.min - src_y);
        angle_to_connection_max = angleTo(neighbor_direction, dx, connection.max - src_y);
        break;
      case EAST:
        dx = src_room.getSECorner().x - src_x;
        angle_to_connection_min = angleTo(neighbor_direction, dx, connection.min - src_y);
        angle_to_connection_max = angleTo(neighbor_direction, dx, connection.max - src_y);
        break;
      default:
        throw new DescentMapException("Unexpected RoomSide: " + neighbor_side);
    }
    return new Point2D.Double(Math.min(angle_to_connection_min, angle_to_connection_max), Math.max(
            angle_to_connection_min, angle_to_connection_max));
  }

  /**
   * 
   * @param src any MapObject
   * @param dst any MapObject in a neighbor Room from src
   * @param neighbor_side the RoomSide to get from src's Room to dst's Room
   * @param angles_to_connection the result of calling
   *        MapUtils.anglesToNeighborConnectionPoints(src, neighbor_side)
   * @return true if src can see dst, false otherwise
   */
  public static boolean canSeeObjectInNeighborRoom(MapObject src, MapObject dst, RoomSide neighbor_side,
          Point2D.Double angles_to_connection) {
    double angle_to_location =
            angleTo(RoomSide.directionToRadians(neighbor_side), dst.getX() - src.getX(),
                    dst.getY() - src.getY());
    return angles_to_connection.x < angle_to_location && angle_to_location < angles_to_connection.y;
  }

  /**
   * 
   * @param src any MapObject
   * @param dst any MapObject in a neighbor Room from src
   * @param neighbor_side the RoomSide to get from src's Room to dst's Room
   * @return true if src can see dst, false otherwise
   */
  public static boolean canSeeObjectInNeighborRoom(MapObject src, MapObject dst, RoomSide neighbor_side) {
    return canSeeObjectInNeighborRoom(src, dst, neighbor_side,
            anglesToNeighborConnectionPoints(src, neighbor_side));
  }

  /**
   * 
   * @param x_coord x-coordinate of point
   * @param y_coord y-coordinate of point
   * @param line_info standard form info of line
   * @return min distance from point to line
   */
  public static double pointToLineDistance(double x_coord, double y_coord, LineInfo line_info) {
    return Math.abs(line_info.a * x_coord + line_info.b * y_coord + line_info.c) / line_info.hypot_a_b;
  }
}
