package structure;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.LinkedList;

import mapobject.MapObject;
import mapobject.unit.Pyro;
import resource.ImageHandler;

import component.MapEngine;

/*
 * Mock Room to connect with the exit Room.
 */
public class MineExteriorRoom extends Room {
  private static final Color WALL_COLOR = Color.cyan;

  public MineExteriorRoom(Point nw_corner) {
    super(nw_corner, 1, 1);
  }

  @Override
  public void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_corner_pixel,
          int pixels_per_cell) {
    super.paint(g, images, WALL_COLOR, WALL_STROKE, ref_cell, ref_cell_corner_pixel, pixels_per_cell);
  }

  @Override
  public String toString() {
    return super.toString() + " (Exterior)";
  }

  @Override
  public LinkedList<MapObject> doNextStep(MapEngine engine, double s_elapsed) {
    for (Pyro pyro : pyros) {
      pyro.removeFromMap();
    }
    return super.doNextStep(engine, s_elapsed);
  }
}
