package mapobject;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;

import structure.Room;

import common.Constants;
import common.ObjectType;
import component.MapEngine;
import external.ImageHandler;

public abstract class MapObject {
  protected final ObjectType type;
  protected final double radius;
  protected String image_name;
  protected boolean is_in_map;
  protected Room room;
  protected double x_loc;
  protected double y_loc;

  public MapObject(double radius) {
    type = getType();
    this.radius = radius;
  }

  public MapObject(Room room, double x_loc, double y_loc) {
    type = getType();
    radius = Constants.getRadius(type);
    image_name = type.name();
    is_in_map = true;
    this.room = room;
    this.x_loc = x_loc;
    this.y_loc = y_loc;
  }

  public double getRadius() {
    return radius;
  }

  public boolean isInMap() {
    return is_in_map;
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

  public abstract ObjectType getType();

  public abstract Image getImage(ImageHandler images);

  public abstract void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell);

  public abstract void computeNextStep(double s_elapsed);

  public abstract MapObject doNextStep(MapEngine engine, double s_elapsed);
}
