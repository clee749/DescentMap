package mapobject.unit;

import java.awt.Graphics2D;
import java.awt.Point;

import mapobject.MapObject;
import mapstructure.Room;
import pilot.Pilot;
import pilot.PilotMove;

public abstract class Unit extends MapObject {
  protected Pilot pilot;
  // protected Gunner gunner;
  protected PilotMove next_move;
  protected boolean fire_cannon;

  public Unit(Room room, double x_loc, double y_loc) {
    super(room, x_loc, y_loc);
  }

  @Override
  public void paint(Graphics2D g, Point ref_cell, Point ref_cell_corner_pixel, int pixels_per_cell) {

  }
}
