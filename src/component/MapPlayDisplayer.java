package component;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;

import mapobject.MapObject;
import mapobject.unit.Pyro;
import resource.ImageHandler;
import structure.DescentMap;
import structure.Room;
import util.MapUtils;

import common.ObjectType;

public class MapPlayDisplayer {
  public static final String FONT_PATH = "/resrc/fonts/DescScor.TTF";
  public static final float FONT_SIZE = 12.0f;

  private final ImageHandler images;
  private final int sight_radius;
  private final Font descent_font;
  private DescentMap map;
  private int pixels_per_cell;
  private Point center_pixel;
  private int max_col_offset;
  private int max_row_offset;

  public MapPlayDisplayer(ImageHandler images, int sight_radius) {
    this.images = images;
    this.sight_radius = sight_radius;
    descent_font = readFont();
  }

  public int getPixelsPerCell() {
    return pixels_per_cell;
  }

  public void setMap(DescentMap map) {
    this.map = map;
  }

  public Font readFont() {
    InputStream is = getClass().getResourceAsStream(FONT_PATH);
    try {
      return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(FONT_SIZE);
    }
    catch (FontFormatException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public void setSizes(Dimension dims) {
    int sight_diameter = 2 * sight_radius + 1;
    pixels_per_cell = Math.min(dims.width / sight_diameter, dims.height / sight_diameter);
    center_pixel = new Point(dims.width / 2, dims.height / 2);
    max_col_offset = (int) Math.ceil((double) center_pixel.x / pixels_per_cell) + 1;
    max_row_offset = (int) Math.ceil((double) center_pixel.y / pixels_per_cell) + 1;
  }

  public void paintMap(Graphics2D g) {
    g.setFont(descent_font);
    MapObject center_object = map.getCenterObject();
    double center_x = center_object.getX();
    double center_y = center_object.getY();
    Point center_cell = new Point((int) center_x, (int) center_y);
    Point center_cell_nw_pixel =
            new Point(center_pixel.x - (int) ((center_x - center_cell.x) * pixels_per_cell), center_pixel.y -
                    (int) ((center_y - center_cell.y) * pixels_per_cell));
    int min_x = center_cell.x - max_col_offset;
    int min_y = center_cell.y - max_row_offset;
    Point nw_corner = new Point(min_x, min_y);
    Point se_corner = new Point(min_x + max_col_offset * 2, min_y + max_row_offset * 2);
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
