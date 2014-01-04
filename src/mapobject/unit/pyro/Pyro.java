package mapobject.unit.pyro;

import gunner.PyroGunner;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import mapobject.unit.Unit;
import pilot.PyroPilot;
import structure.Room;
import util.ImageHandler;

import common.Constants;
import common.MapUtils;
import common.ObjectType;

public class Pyro extends Unit {

  public Pyro(Room room, double x_loc, double y_loc, double direction) {
    super(new PyroPilot(Constants.getRadius(ObjectType.Pyro), room), new PyroGunner(), room, x_loc, y_loc,
            direction);
    ((PyroPilot) pilot).startPilot();
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Pyro;
  }

  @Override
  public void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell) {
    super.paint(g, images, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    Point target_pixel =
            MapUtils.coordsToPixel(pilot.getTargetX(), pilot.getTargetY(), ref_cell, ref_cell_nw_pixel,
                    pixels_per_cell);
    g.setColor(Color.yellow);
    g.drawRect(target_pixel.x - 1, target_pixel.y - 1, 2, 2);
  }
}
