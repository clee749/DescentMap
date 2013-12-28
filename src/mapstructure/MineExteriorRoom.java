package mapstructure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import common.Constants;

/*
 * Mock Room to connect with the exit Room.
 */
public class MineExteriorRoom extends Room {

  public MineExteriorRoom(Point nw_corner, Point se_corner) {
    super(nw_corner, se_corner);
  }

  @Override
  public void paint(Graphics2D g, Point ref_cell, Point ref_cell_corner_pixel, int pixels_per_cell) {
    g.setColor(Color.cyan);
    g.setStroke(new BasicStroke(1));
    super.paint(g, ref_cell, ref_cell_corner_pixel, pixels_per_cell);
    g.setColor(Constants.ROOM_WALL_COLOR);
    g.setStroke(new BasicStroke(Constants.ROOM_WALL_THICKNESS));
  }
}
