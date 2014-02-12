package component;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;

import mapobject.MapObject;
import mapobject.unit.Pyro;
import structure.DescentMap;
import structure.Room;
import util.MapUtils;

import common.ObjectType;

import external.ImageHandler;

public class MapPlayDisplayer {
  private final ImageHandler images;
  private final int sight_radius;
  private DescentMap map;
  private int pixels_per_cell;
  private Point center_pixel;
  private int num_cols;
  private int num_rows;

  public MapPlayDisplayer(ImageHandler images, int sight_radius) {
    this.images = images;
    this.sight_radius = sight_radius;
  }

  public int getPixelsPerCell() {
    return pixels_per_cell;
  }

  public void setMap(DescentMap map) {
    this.map = map;
  }

  public void setSizes(Dimension dims) {
    int sight_diameter = 2 * sight_radius + 1;
    pixels_per_cell = Math.min(dims.width / sight_diameter, dims.height / sight_diameter);
    center_pixel = new Point(dims.width / 2, dims.height / 2);
    num_cols = (int) ((double) dims.width / pixels_per_cell) + 2;
    num_rows = (int) ((double) dims.height / pixels_per_cell) + 2;
  }

  public void paintMap(Graphics2D g) {
    MapObject center_object = map.getCenterObject();
    double center_x = center_object.getX();
    double center_y = center_object.getY();
    Point center_cell = new Point((int) center_x, (int) center_y);
    Point center_cell_nw_pixel =
            new Point(center_pixel.x - (int) ((center_x - center_cell.x) * pixels_per_cell), center_pixel.y -
                    (int) ((center_y - center_cell.y) * pixels_per_cell));
    int min_x = center_cell.x - num_cols / 2 - 1;
    int min_y = center_cell.y - num_rows / 2 - 1;
    Point nw_corner = new Point(min_x, min_y);
    Point se_corner = new Point(min_x + num_cols, min_y + num_rows);
    for (Room room : map.getAllRooms()) {
      if (MapUtils.rectanglesIntersect(nw_corner, se_corner, room.getNWCorner(), room.getSECorner())) {
        room.paintSceneries(g, images, center_cell, center_cell_nw_pixel, pixels_per_cell);
      }
    }
    for (Room room : map.getAllRooms()) {
      if (MapUtils.rectanglesIntersect(nw_corner, se_corner, room.getNWCorner(), room.getSECorner())) {
        room.paint(g, images, center_cell, center_cell_nw_pixel, pixels_per_cell);
      }
    }
    if (center_object.getType().equals(ObjectType.Pyro) && center_object.isInMap()) {
      ((Pyro) center_object).paintInfo(g);
    }
  }
}
