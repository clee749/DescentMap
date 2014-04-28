package component;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;

import structure.DescentMap;
import structure.Room;

public class MapConstructionDisplayer {
  public static final int NUM_MARGIN_CELLS = 1;

  private DescentMap map;
  private int pixels_per_cell;
  private Point ref_cell;
  private Point ref_cell_nw_pixel;

  public void setMap(DescentMap map) {
    this.map = map;
  }

  public void centerMap(Dimension dims) {
    int min_x = map.getMinX();
    int max_x = map.getMaxX();
    int min_y = map.getMinY();
    int max_y = map.getMaxY();
    int x_range = max_x - min_x + NUM_MARGIN_CELLS;
    int y_range = max_y - min_y + NUM_MARGIN_CELLS;
    pixels_per_cell = Math.min(dims.width / x_range, dims.height / y_range);
    int x_sum = min_x + max_x;
    int y_sum = min_y + max_y;
    ref_cell = new Point(x_sum / 2, y_sum / 2);
    Point center_pixel = new Point(dims.width / 2, dims.height / 2);
    double x_center = x_sum / 2.0;
    double y_center = y_sum / 2.0;
    ref_cell_nw_pixel =
            new Point(center_pixel.x - (int) ((x_center - ref_cell.x) * pixels_per_cell), center_pixel.y -
                    (int) ((y_center - ref_cell.y) * pixels_per_cell));
  }

  public void paintMap(Graphics2D g, Dimension dims) {
    if (!map.hasRooms()) {
      return;
    }
    centerMap(dims);
    for (Room room : map.getAllRooms()) {
      room.paint(g, null, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    }
  }

  public void paintRoomIfDefined(Graphics2D g, Room room) {
    if (room != null) {
      room.paint(g, null, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    }
  }
}
