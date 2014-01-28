package mapobject;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.LinkedList;

import common.DescentMapException;
import common.ObjectType;
import component.MapEngine;

import external.ImageHandler;

public class MultipleObject extends MapObject {
  private final LinkedList<MapObject> objects;

  public MultipleObject() {
    objects = new LinkedList<MapObject>();
  }

  @Override
  public ObjectType getType() {
    return ObjectType.MultipleObject;
  }

  public void addObject(MapObject object) {
    if (object != null) {
      objects.add(object);
    }
  }

  public LinkedList<MapObject> getObjects() {
    return objects;
  }

  @Override
  public void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell) {
    throw new DescentMapException("MultipleObject should not be painted!");
  }

  @Override
  public void planNextAction(double s_elapsed) {
    throw new DescentMapException("MultipleObject should have no actions!");
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    throw new DescentMapException("MultipleObject should have no actions!");
  }
}
