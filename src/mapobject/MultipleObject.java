package mapobject;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.util.LinkedList;

import util.ImageHandler;

import common.ObjectType;
import component.MapEngine;

public class MultipleObject extends MapObject {
  private final LinkedList<MapObject> objects;

  public MultipleObject() {
    super(0.0);
    objects = new LinkedList<MapObject>();
  }

  @Override
  public ObjectType getType() {
    return ObjectType.MultipleObject;
  }

  public void addObject(MapObject object) {
    objects.add(object);
  }

  public LinkedList<MapObject> getObjects() {
    return objects;
  }

  @Override
  public Image getImage(ImageHandler images) {
    return null;
  }

  @Override
  public void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell) {

  }

  @Override
  public void computeNextStep(double s_elapsed) {

  }

  @Override
  public MapObject doNextStep(MapEngine engine, double s_elapsed) {
    return null;
  }
}
