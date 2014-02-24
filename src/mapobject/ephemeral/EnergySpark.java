package mapobject.ephemeral;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;

import mapobject.MapObject;
import mapobject.powerup.Powerup;
import resource.ImageHandler;
import structure.Room;
import util.MapUtils;

import common.DescentMapException;
import common.ObjectType;
import component.MapEngine;

enum EnergySparkState {
  EXPAND,
  SHINE,
  CONTRACT;
}


public class EnergySpark extends MapObject {
  public static final Color COLOR = Color.yellow;
  public static final Stroke STROKE = new BasicStroke(1);
  public static final double SECONDS_PER_STATE = Powerup.SECONDS_PER_FRAME * 10;

  private EnergySparkState state;
  private double state_time_left;

  public EnergySpark(Room room, double x_loc, double y_loc) {
    super(0.0, room, x_loc, y_loc);
    state = EnergySparkState.EXPAND;
    state_time_left = SECONDS_PER_STATE;
  }

  @Override
  public ObjectType getType() {
    return ObjectType.EnergySpark;
  }

  @Override
  public void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell) {
    Point center_pixel = MapUtils.coordsToPixel(x_loc, y_loc, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    g.setColor(COLOR);
    g.setStroke(STROKE);
    if (state.equals(EnergySparkState.SHINE)) {
      g.drawLine(center_pixel.x - 1, center_pixel.y, center_pixel.x + 1, center_pixel.y);
      g.drawLine(center_pixel.x, center_pixel.y - 1, center_pixel.x, center_pixel.y + 1);
    }
    else {
      g.drawLine(center_pixel.x, center_pixel.y, center_pixel.x, center_pixel.y);
    }
  }

  @Override
  public void planNextAction(double s_elapsed) {

  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    if (state_time_left < 0.0) {
      switch (state) {
        case EXPAND:
          state = EnergySparkState.SHINE;
          break;
        case SHINE:
          state = EnergySparkState.CONTRACT;
          break;
        case CONTRACT:
          is_in_map = false;
          break;
        default:
          throw new DescentMapException("Unexpected EnergySparkState: " + state);
      }
      state_time_left = SECONDS_PER_STATE;
    }
    else {
      state_time_left -= s_elapsed;
    }
    return null;
  }
}
