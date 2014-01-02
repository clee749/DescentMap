package structure;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import common.MapUtils;
import common.RoomSide;

public class Room {
  private final Point nw_corner;
  private final Point se_corner;
  private final int width;
  private final int height;
  private final HashMap<RoomSide, RoomConnection> neighbors;

  public Room(Point nw_corner, Point se_corner) {
    this.nw_corner = nw_corner;
    this.se_corner = se_corner;
    width = se_corner.x - nw_corner.x;
    height = se_corner.y - nw_corner.y;
    neighbors = new HashMap<RoomSide, RoomConnection>();
  }

  public Point getNWCorner() {
    return nw_corner;
  }

  public Point getSECorner() {
    return se_corner;
  }

  public Point getNECorner() {
    return new Point(se_corner.x, nw_corner.y);
  }

  public Point getSWCorner() {
    return new Point(nw_corner.x, se_corner.y);
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public HashMap<RoomSide, RoomConnection> getNeighbors() {
    return neighbors;
  }

  public RoomConnection getConnectionInDirection(RoomSide direction) {
    return neighbors.get(direction);
  }

  @Override
  public String toString() {
    return "(" + nw_corner + ", " + se_corner + ")";
  }

  public void paint(Graphics2D g, Point ref_cell, Point ref_cell_corner_pixel, int pixels_per_cell) {
    Point nw_pixel = MapUtils.coordsToPixel(nw_corner, ref_cell, ref_cell_corner_pixel, pixels_per_cell);
    Point se_pixel = new Point(nw_pixel.x + width * pixels_per_cell, nw_pixel.y + height * pixels_per_cell);

    // north wall
    RoomConnection connection = neighbors.get(RoomSide.NORTH);
    if (connection == null) {
      g.drawLine(nw_pixel.x, nw_pixel.y, se_pixel.x, nw_pixel.y);
    }
    else {
      g.drawLine(nw_pixel.x, nw_pixel.y, nw_pixel.x + (connection.min - nw_corner.x) * pixels_per_cell,
              nw_pixel.y);
      g.drawLine(nw_pixel.x + (connection.max - nw_corner.x) * pixels_per_cell, nw_pixel.y, se_pixel.x,
              nw_pixel.y);
    }

    // south wall
    connection = neighbors.get(RoomSide.SOUTH);
    if (connection == null) {
      g.drawLine(nw_pixel.x, se_pixel.y, se_pixel.x, se_pixel.y);
    }
    else {
      g.drawLine(nw_pixel.x, se_pixel.y, nw_pixel.x + (connection.min - nw_corner.x) * pixels_per_cell,
              se_pixel.y);
      g.drawLine(nw_pixel.x + (connection.max - nw_corner.x) * pixels_per_cell, se_pixel.y, se_pixel.x,
              se_pixel.y);
    }

    // west wall
    connection = neighbors.get(RoomSide.WEST);
    if (connection == null) {
      g.drawLine(nw_pixel.x, nw_pixel.y, nw_pixel.x, se_pixel.y);
    }
    else {
      g.drawLine(nw_pixel.x, nw_pixel.y, nw_pixel.x, nw_pixel.y + (connection.min - nw_corner.y) *
              pixels_per_cell);
      g.drawLine(nw_pixel.x, nw_pixel.y + (connection.max - nw_corner.y) * pixels_per_cell, nw_pixel.x,
              se_pixel.y);
    }

    // east wall
    connection = neighbors.get(RoomSide.EAST);
    if (connection == null) {
      g.drawLine(se_pixel.x, nw_pixel.y, se_pixel.x, se_pixel.y);
    }
    else {
      g.drawLine(se_pixel.x, nw_pixel.y, se_pixel.x, nw_pixel.y + (connection.min - nw_corner.y) *
              pixels_per_cell);
      g.drawLine(se_pixel.x, nw_pixel.y + (connection.max - nw_corner.y) * pixels_per_cell, se_pixel.x,
              se_pixel.y);
    }
  }

  public ArrayList<RoomSide> findUnconnectedSides() {
    ArrayList<RoomSide> unconnected_sides = new ArrayList<RoomSide>();
    for (RoomSide side : RoomSide.values()) {
      if (!neighbors.containsKey(side)) {
        unconnected_sides.add(side);
      }
    }
    return unconnected_sides;
  }

  public void addNeighbor(RoomSide side, RoomConnection connection) {
    neighbors.put(side, connection);
  }

  public boolean containsPoint(double x, double y) {
    return nw_corner.x <= x && x <= se_corner.x && nw_corner.y <= y && y <= se_corner.y;
  }

  public boolean containsRange(double x, double y, double range) {
    return nw_corner.x <= x - range && x + range <= se_corner.x && nw_corner.y <= y - range &&
            y + range <= se_corner.y;
  }

  public RoomSide directionOfLeaving(double x, double y) {
    if (x < nw_corner.x) {
      return RoomSide.WEST;
    }
    if (x > se_corner.x) {
      return RoomSide.EAST;
    }
    if (y < nw_corner.y) {
      return RoomSide.NORTH;
    }
    if (y > se_corner.y) {
      return RoomSide.SOUTH;
    }
    return null;
  }
}