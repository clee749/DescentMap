package mapobject.ephemeral;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import mapobject.MapObject;
import structure.Room;
import util.MapUtils;

import common.ObjectType;
import component.MapEngine;

import external.ImageHandler;

public class Zunggg extends MapObject {
  public static final int STROKE_WIDTH_DIVISOR = 20;
  public static final int NUM_BOLTS_DIVISOR = 5;
  public static final int MIN_NUM_BOLTS = 10;

  private final double total_time;
  private double time_elapsed;

  public Zunggg(Room room, double x_loc, double y_loc, double total_time) {
    super(0.5, room, x_loc, y_loc);
    this.total_time = total_time;
    time_elapsed = 0.0;
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Zunggg;
  }

  @Override
  public void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell) {
    Point center_pixel = MapUtils.coordsToPixel(x_loc, y_loc, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    int pixels_per_half_cell = pixels_per_cell / 2;
    g.setColor(Color.cyan);
    g.setStroke(new BasicStroke(Math.min(pixels_per_cell / STROKE_WIDTH_DIVISOR, 2)));
    int num_bolts = Math.min(pixels_per_cell / NUM_BOLTS_DIVISOR, MIN_NUM_BOLTS);
    for (int i = 0; i < num_bolts; ++i) {
      g.drawLine(center_pixel.x, center_pixel.y, center_pixel.x +
              (int) (Math.random() * pixels_per_cell - pixels_per_half_cell),
              center_pixel.y + (int) (Math.random() * pixels_per_cell - pixels_per_half_cell));
    }
  }

  @Override
  public void planNextAction(double s_elapsed) {

  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    time_elapsed += s_elapsed;
    if (time_elapsed > total_time) {
      is_in_map = false;
    }
    return null;
  }
}
