package mapobject.unit.pyro;

import java.awt.Graphics2D;
import java.awt.Point;

import mapobject.unit.Unit;
import mapstructure.Room;

public class Pyro extends Unit {

  public Pyro(Room room, double x_loc, double y_loc) {
    super(room, x_loc, y_loc);
  }

  @Override
  public void paint(Graphics2D g, Point center_location, Point center_location_corner_pixel,
          int pixels_per_location) {

  }
}
