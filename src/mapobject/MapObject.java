package mapobject;

import java.awt.Graphics2D;
import java.awt.Point;

import mapstructure.Room;

public abstract class MapObject {
  protected Room room;
  protected double x_loc;
  protected double y_loc;

  public MapObject(Room room, double x_loc, double y_loc) {
    this.room = room;
    this.x_loc = x_loc;
    this.y_loc = y_loc;
  }

  public Room getRoom() {
    return room;
  }

  public double getX() {
    return x_loc;
  }

  public double getY() {
    return y_loc;
  }

  public abstract void paint(Graphics2D g, Point ref_cell, Point ref_cell_corner_pixel, int pixels_per_cell);

  public abstract void computeNextStep();

  public abstract void doNextStep(long ms_elapsed);
}
