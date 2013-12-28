package mapobject.unit;

import java.awt.Graphics2D;
import java.awt.Point;

import mapobject.MapObject;
import mapstructure.Room;

public class Unit extends MapObject {

  public Unit(Room room, double x_loc, double y_loc) {
    super(room, x_loc, y_loc);
  }

  @Override
  public void paint(Graphics2D g, Point center_location, Point center_location_corner_pixel,
          int pixels_per_location) {

  }
}
