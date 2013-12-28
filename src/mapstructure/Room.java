package mapstructure;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

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

  public void paint(Graphics2D g, Point ref_cell, Point ref_cell_corner_pixel, int pixels_per_cell) {
    Point nw_pixel =
            new Point(ref_cell_corner_pixel.x - ((ref_cell.x - nw_corner.x) * pixels_per_cell),
                    ref_cell_corner_pixel.y - ((ref_cell.y - nw_corner.y) * pixels_per_cell));
    Point se_pixel = new Point(nw_pixel.x + width * pixels_per_cell, nw_pixel.y + height * pixels_per_cell);

    // north wall
    RoomConnection connection = neighbors.get(RoomSide.North);
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
    connection = neighbors.get(RoomSide.South);
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
    connection = neighbors.get(RoomSide.West);
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
    connection = neighbors.get(RoomSide.East);
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

  @Override
  public String toString() {
    return "(" + nw_corner + ", " + se_corner + ")";
  }
}
