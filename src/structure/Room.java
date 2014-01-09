package structure;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import mapobject.MapObject;
import mapobject.powerup.Powerup;
import mapobject.scenery.Scenery;
import mapobject.shot.Shot;
import mapobject.unit.Unit;
import util.MapUtils;

import common.Constants;
import common.RoomSide;
import component.MapEngine;

import external.ImageHandler;

public class Room {
  private final Point nw_corner;
  private final Point se_corner;
  private final int width;
  private final int height;
  private final HashMap<RoomSide, RoomConnection> neighbors;
  private final HashSet<Scenery> sceneries;
  private final HashSet<Powerup> powerups;
  private final HashSet<Shot> shots;
  private final HashSet<Unit> units;
  private final HashSet<MapObject> misc_objects;

  public Room(Point nw_corner, Point se_corner) {
    this.nw_corner = nw_corner;
    this.se_corner = se_corner;
    width = se_corner.x - nw_corner.x;
    height = se_corner.y - nw_corner.y;
    neighbors = new HashMap<RoomSide, RoomConnection>();
    sceneries = new HashSet<Scenery>();
    powerups = new HashSet<Powerup>();
    shots = new HashSet<Shot>();
    units = new HashSet<Unit>();
    misc_objects = new HashSet<MapObject>();
  }

  public Point getNWCorner() {
    return nw_corner;
  }

  public Point getSECorner() {
    return se_corner;
  }

  public Point getNECorner() {
    return new Point(se_corner.x, nw_corner.y);
  }

  public Point getSWCorner() {
    return new Point(nw_corner.x, se_corner.y);
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public HashMap<RoomSide, RoomConnection> getNeighbors() {
    return neighbors;
  }

  public RoomConnection getConnectionInDirection(RoomSide direction) {
    return neighbors.get(direction);
  }

  public HashSet<Scenery> getSceneries() {
    return sceneries;
  }

  public HashSet<Powerup> getPowerups() {
    return powerups;
  }

  public HashSet<Shot> getShots() {
    return shots;
  }

  public HashSet<Unit> getUnits() {
    return units;
  }

  public HashSet<MapObject> getMiscObjects() {
    return misc_objects;
  }

  @Override
  public String toString() {
    return "(" + nw_corner + ", " + se_corner + ")";
  }

  public void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell) {
    paint(g, images, Constants.ROOM_WALL_COLOR, Constants.ROOM_WALL_STROKE, ref_cell, ref_cell_nw_pixel,
            pixels_per_cell);
  }

  public void paint(Graphics2D g, ImageHandler images, Color wall_color, Stroke wall_stroke, Point ref_cell,
          Point ref_cell_nw_pixel, int pixels_per_cell) {
    Point nw_pixel = MapUtils.coordsToPixel(nw_corner, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    Point se_pixel = new Point(nw_pixel.x + width * pixels_per_cell, nw_pixel.y + height * pixels_per_cell);
    g.setColor(wall_color);
    g.setStroke(wall_stroke);

    // north wall
    RoomConnection connection = neighbors.get(RoomSide.NORTH);
    if (connection == null) {
      g.drawLine(nw_pixel.x, nw_pixel.y, se_pixel.x, nw_pixel.y);
    }
    else {
      g.drawLine(nw_pixel.x, nw_pixel.y, nw_pixel.x + (connection.min - nw_corner.x) * pixels_per_cell,
              nw_pixel.y);
      g.drawLine(nw_pixel.x + (connection.max - nw_corner.x) * pixels_per_cell, nw_pixel.y, se_pixel.x,
              nw_pixel.y);
    }

    // south wall
    connection = neighbors.get(RoomSide.SOUTH);
    if (connection == null) {
      g.drawLine(nw_pixel.x, se_pixel.y, se_pixel.x, se_pixel.y);
    }
    else {
      g.drawLine(nw_pixel.x, se_pixel.y, nw_pixel.x + (connection.min - nw_corner.x) * pixels_per_cell,
              se_pixel.y);
      g.drawLine(nw_pixel.x + (connection.max - nw_corner.x) * pixels_per_cell, se_pixel.y, se_pixel.x,
              se_pixel.y);
    }

    // west wall
    connection = neighbors.get(RoomSide.WEST);
    if (connection == null) {
      g.drawLine(nw_pixel.x, nw_pixel.y, nw_pixel.x, se_pixel.y);
    }
    else {
      g.drawLine(nw_pixel.x, nw_pixel.y, nw_pixel.x, nw_pixel.y + (connection.min - nw_corner.y) *
              pixels_per_cell);
      g.drawLine(nw_pixel.x, nw_pixel.y + (connection.max - nw_corner.y) * pixels_per_cell, nw_pixel.x,
              se_pixel.y);
    }

    // east wall
    connection = neighbors.get(RoomSide.EAST);
    if (connection == null) {
      g.drawLine(se_pixel.x, nw_pixel.y, se_pixel.x, se_pixel.y);
    }
    else {
      g.drawLine(se_pixel.x, nw_pixel.y, se_pixel.x, nw_pixel.y + (connection.min - nw_corner.y) *
              pixels_per_cell);
      g.drawLine(se_pixel.x, nw_pixel.y + (connection.max - nw_corner.y) * pixels_per_cell, se_pixel.x,
              se_pixel.y);
    }

    // children
    if (images != null) {
      for (Scenery scenery : sceneries) {
        scenery.paint(g, images, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
      }
      for (Powerup powerup : powerups) {
        powerup.paint(g, images, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
      }
      for (Shot shot : shots) {
        shot.paint(g, images, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
      }
      for (Unit unit : units) {
        unit.paint(g, images, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
      }
      for (MapObject object : misc_objects) {
        object.paint(g, images, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
      }
    }
  }

  public ArrayList<RoomSide> findUnconnectedSides() {
    ArrayList<RoomSide> unconnected_sides = new ArrayList<RoomSide>();
    for (RoomSide side : RoomSide.values()) {
      if (!neighbors.containsKey(side)) {
        unconnected_sides.add(side);
      }
    }
    return unconnected_sides;
  }

  public void addNeighbor(RoomSide side, RoomConnection connection) {
    neighbors.put(side, connection);
  }

  public void addChild(MapObject object) {
    if (object instanceof Scenery) {
      sceneries.add((Scenery) object);
    }
    else if (object instanceof Powerup) {
      powerups.add((Powerup) object);
    }
    else if (object instanceof Shot) {
      shots.add((Shot) object);
    }
    else if (object instanceof Unit) {
      units.add((Unit) object);
    }
    else {
      misc_objects.add(object);
    }
  }

  public boolean removeChild(MapObject child) {
    if (child instanceof Scenery) {
      return sceneries.remove(child);
    }
    if (child instanceof Powerup) {
      return powerups.remove(child);
    }
    if (child instanceof Shot) {
      return shots.remove(child);
    }
    if (child instanceof Unit) {
      return units.remove(child);
    }
    return misc_objects.remove(child);
  }

  public void computeNextStep(double s_elapsed) {
    for (Scenery scenery : sceneries) {
      scenery.planNextAction(s_elapsed);
    }
    for (Powerup powerup : powerups) {
      powerup.planNextAction(s_elapsed);
    }
    for (Shot shot : shots) {
      shot.planNextAction(s_elapsed);
    }
    for (Unit unit : units) {
      unit.planNextAction(s_elapsed);
    }
    for (MapObject object : misc_objects) {
      object.planNextAction(s_elapsed);
    }
  }

  public LinkedList<MapObject> doNextStep(MapEngine engine, double s_elapsed) {
    LinkedList<MapObject> created_objects = doNextStep(engine, s_elapsed, sceneries);
    created_objects.addAll(doNextStep(engine, s_elapsed, powerups));
    created_objects.addAll(doNextStep(engine, s_elapsed, shots));
    created_objects.addAll(doNextStep(engine, s_elapsed, units));
    created_objects.addAll(doNextStep(engine, s_elapsed, misc_objects));
    return created_objects;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  public LinkedList<MapObject> doNextStep(MapEngine engine, double s_elapsed, Collection objects) {
    LinkedList<MapObject> created_objects = new LinkedList<MapObject>();
    for (Iterator<MapObject> it = objects.iterator(); it.hasNext();) {
      MapObject object = it.next();
      MapObject created = object.doNextAction(engine, s_elapsed);
      if (created != null) {
        created_objects.add(created);
      }
      if (!object.isInMap()) {
        it.remove();
      }
    }
    return created_objects;
  }
}
