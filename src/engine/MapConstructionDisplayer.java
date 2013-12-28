package engine;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;

import mapstructure.Room;

import common.Constants;

public class MapConstructionDisplayer {
  private final MapBuilder builder;
  private int pixels_per_cell;
  private Point ref_cell;
  private Point ref_cell_nw_pixel;

  public MapConstructionDisplayer(MapBuilder builder) {
    this.builder = builder;
  }

  public void centerMap(Dimension dims) {
    int min_x = builder.getMinX();
    int max_x = builder.getMaxX();
    int min_y = builder.getMinY();
    int max_y = builder.getMaxY();
    int x_range = max_x - min_x + 1;
    int y_range = max_y - min_y + 1;
    int sight_diameter = Math.max(Math.max(x_range, y_range), Constants.CONSTRUCTION_MIN_SIGHT_DIAMETER);
    pixels_per_cell = Math.min(dims.width / sight_diameter, dims.height / sight_diameter);
    ref_cell = new Point((min_x + max_x) / 2, (min_y + max_y) / 2);
    Point center_pixel = new Point(dims.width / 2, dims.height / 2);
    ref_cell_nw_pixel = new Point(center_pixel.x - pixels_per_cell / 2, center_pixel.y - pixels_per_cell / 2);
  }

  public void displayMap(Graphics2D g, Dimension dims) {
    centerMap(dims);
    g.setColor(Constants.ROOM_WALL_COLOR);
    g.setStroke(new BasicStroke(Constants.ROOM_WALL_THICKNESS));
    for (Room room : builder.getRooms()) {
      room.paint(g, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    }
  }
}
