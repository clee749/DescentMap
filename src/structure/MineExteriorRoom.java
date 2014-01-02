package structure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;

/*
 * Mock Room to connect with the exit Room.
 */
public class MineExteriorRoom extends Room {
  private static final Color WALL_COLOR = Color.cyan;
  private static final Stroke WALL_STROKE = new BasicStroke(1);

  public MineExteriorRoom(Point nw_corner, Point se_corner) {
    super(nw_corner, se_corner);
  }

  @Override
  public void paint(Graphics2D g, Point ref_cell, Point ref_cell_corner_pixel, int pixels_per_cell) {
    super.paint(g, MineExteriorRoom.WALL_COLOR, MineExteriorRoom.WALL_STROKE, ref_cell,
            ref_cell_corner_pixel, pixels_per_cell);
  }

  @Override
  public String toString() {
    return super.toString() + " (Exterior)";
  }
}
