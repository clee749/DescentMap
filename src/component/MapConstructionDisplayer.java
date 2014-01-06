package component;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;

import structure.DescentMap;
import structure.Room;

import common.Constants;

public class MapConstructionDisplayer {
  private final DescentMap map;
  private int pixels_per_cell;
  private Point ref_cell;
  private Point ref_cell_nw_pixel;

  public MapConstructionDisplayer(DescentMap map) {
    this.map = map;
  }

  public void centerMap(Dimension dims) {
    int min_x = map.getMinX();
    int max_x = map.getMaxX();
    int min_y = map.getMinY();
    int max_y = map.getMaxY();
    int x_range = max_x - min_x + 1;
    int y_range = max_y - min_y + 1;
    int sight_diameter = Math.max(Math.max(x_range, y_range), Constants.CONSTRUCTION_MIN_SIGHT_DIAMETER);
    pixels_per_cell = Math.min(dims.width / sight_diameter, dims.height / sight_diameter);
    ref_cell = new Point((min_x + max_x) / 2, (min_y + max_y) / 2);
    Point center_pixel = new Point(dims.width / 2, dims.height / 2);
    ref_cell_nw_pixel = new Point(center_pixel.x - pixels_per_cell / 2, center_pixel.y - pixels_per_cell / 2);
  }

  public void displayMap(Graphics2D g, Dimension dims) {
    if (!map.hasRooms()) {
      return;
    }
    centerMap(dims);
    for (Room room : map.getRooms()) {
      room.paint(g, null, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    }
  }
}
