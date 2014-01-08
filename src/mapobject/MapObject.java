package mapobject;

import java.awt.Graphics2D;
import java.awt.Point;

import structure.Room;

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

  public MapObject() {
    type = getType();
    radius = getRadius();
    is_in_map = true;
  }

  public MapObject(Room room, double x_loc, double y_loc) {
    this();
    image_name = type.name();
    this.room = room;
    this.x_loc = x_loc;
    this.y_loc = y_loc;
  }

  public MapObject(Room room, double x_loc, double y_loc, double radius) {
    type = getType();
    this.radius = radius;
    is_in_map = true;
    image_name = type.name();
    this.room = room;
    this.x_loc = x_loc;
    this.y_loc = y_loc;
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

  public abstract double getRadius();

  public abstract void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell);

  public abstract void planNextAction(double s_elapsed);

  public abstract MapObject doNextAction(MapEngine engine, double s_elapsed);
}
