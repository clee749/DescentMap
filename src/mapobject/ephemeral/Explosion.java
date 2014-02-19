package mapobject.ephemeral;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import mapobject.MapObject;
import resource.ImageHandler;
import structure.Room;
import util.MapUtils;

import common.ObjectType;
import component.MapEngine;

public class Explosion extends MapObject {
  public static final Color[] COLORS = {Color.magenta, Color.orange, Color.pink, Color.red, Color.yellow,
          Color.white};
  public static final double DISTANCE_BETWEEN_LAYERS = 0.1;

  private final double total_time;
  private final double half_life;
  private double time_elapsed;

  public Explosion(Room room, double x_loc, double y_loc, double max_radius, double total_time) {
    super(max_radius, room, x_loc, y_loc);
    this.total_time = total_time;
    half_life = total_time / 2;
    time_elapsed = 0.0;
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Explosion;
  }

  public double getHalfLife() {
    return half_life;
  }

  @Override
  public void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell) {
    for (double layer_radius = radius * (half_life - Math.abs(time_elapsed - half_life)) / half_life; layer_radius > 0; layer_radius -=
            DISTANCE_BETWEEN_LAYERS) {
      Point layer_nw_corner_pixel =
              MapUtils.coordsToPixel(x_loc - layer_radius, y_loc - layer_radius, ref_cell, ref_cell_nw_pixel,
                      pixels_per_cell);
      int pixel_diameter = (int) (pixels_per_cell * 2 * layer_radius);
      g.setColor(COLORS[(int) (Math.random() * COLORS.length)]);
      g.fillOval(layer_nw_corner_pixel.x, layer_nw_corner_pixel.y, pixel_diameter, pixel_diameter);
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
