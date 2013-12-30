package mapobject.unit.pyro;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import mapobject.unit.Unit;
import mapstructure.Room;
import pilot.PyroPilot;

import common.MazeUtils;

public class Pyro extends Unit {

  public Pyro(Room room, double x_loc, double y_loc) {
    super(room, x_loc, y_loc);
    pilot = new PyroPilot(room);
  }

  @Override
  public void paint(Graphics2D g, Point ref_cell, Point ref_cell_corner_pixel, int pixels_per_cell) {
    Point nw_corner = MazeUtils.coordsToPixel(x_loc, y_loc, ref_cell, ref_cell_corner_pixel, pixels_per_cell);
    g.setColor(Color.green);
    g.drawRect(nw_corner.x - 1, nw_corner.y - 1, 3, 3);
  }

  @Override
  public void computeNextStep() {
    next_move = pilot.nextMove();
  }

  @Override
  public void doNextStep(long ms_elapsed) {
    // temp
    room = ((PyroPilot) pilot).nextRoom();
    Point nw_corner = room.getNWCorner();
    Point se_corner = room.getSECorner();
    x_loc = (nw_corner.x + se_corner.x) / 2.0;
    y_loc = (nw_corner.y + se_corner.y) / 2.0;
    ((PyroPilot) pilot).visitRoom(room);
  }
}
