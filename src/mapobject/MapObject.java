package mapobject;

import java.awt.Graphics2D;
import java.awt.Point;

public abstract class MapObject {
  public abstract void paint(Graphics2D g, Point center_location, Point center_location_corner_pixel,
          int pixels_per_location);
}
