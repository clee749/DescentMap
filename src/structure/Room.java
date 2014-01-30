package structure;

import java.awt.BasicStroke;
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
import mapobject.unit.pyro.Pyro;
import util.MapUtils;

import common.ObjectType;
import common.RoomSide;
import component.MapEngine;

import external.ImageHandler;

public class Room {
  public static final Color WALL_COLOR = Color.lightGray;
  public static final Stroke WALL_STROKE = new BasicStroke(2);

  private final Point nw_corner;
  private final Point se_corner;
  private final int width;
  private final int height;
  private final HashMap<RoomSide, RoomConnection> neighbors;
  private final HashSet<Shot> shots;
  private final HashSet<Powerup> powerups;
  private final HashSet<Pyro> pyros;
  private final HashSet<Unit> mechs; // all non-Pyro Units
  private final HashSet<Scenery> sceneries;
  private final HashSet<MapObject> misc_objects;

  public Room(Point nw_corner, Point se_corner) {
    this.nw_corner = nw_corner;
    this.se_corner = se_corner;
    width = se_corner.x - nw_corner.x;
    height = se_corner.y - nw_corner.y;
    neighbors = new HashMap<RoomSide, RoomConnection>();
    shots = new HashSet<Shot>();
    powerups = new HashSet<Powerup>();
    pyros = new HashSet<Pyro>();
    mechs = new HashSet<Unit>();
    sceneries = new HashSet<Scenery>();
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

  public Room getNeighborInDirection(RoomSide direction) {
    RoomConnection connection = neighbors.get(direction);
    return (connection != null ? connection.neighbor : null);
  }

  public HashSet<Shot> getShots() {
    return shots;
  }

  public HashSet<Powerup> getPowerups() {
    return powerups;
  }

  public HashSet<Pyro> getPyros() {
    return pyros;
  }

  public HashSet<Unit> getMechs() {
    return mechs;
  }

  public HashSet<Scenery> getSceneries() {
    return sceneries;
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
    paint(g, images, WALL_COLOR, WALL_STROKE, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
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
      for (Unit unit : mechs) {
        unit.paint(g, images, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
      }
      for (Pyro pyro : pyros) {
        pyro.paint(g, images, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
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
    if (object instanceof Shot) {
      shots.add((Shot) object);
    }
    else if (object instanceof Powerup) {
      powerups.add((Powerup) object);
    }
    else if (object.getType().equals(ObjectType.Pyro)) {
      pyros.add((Pyro) object);
    }
    else if (object instanceof Unit) {
      mechs.add((Unit) object);
    }
    else if (object instanceof Scenery) {
      sceneries.add((Scenery) object);
    }
    else {
      misc_objects.add(object);
    }
  }

  public boolean removeChild(MapObject child) {
    if (child instanceof Shot) {
      return shots.remove(child);
    }
    if (child instanceof Powerup) {
      return powerups.remove(child);
    }
    if (child.getType().equals(ObjectType.Pyro)) {
      return pyros.remove(child);
    }
    if (child instanceof Unit) {
      return mechs.remove(child);
    }
    if (child instanceof Scenery) {
      return sceneries.remove(child);
    }
    return misc_objects.remove(child);
  }

  public void computeNextStep(double s_elapsed) {
    for (Shot shot : shots) {
      shot.planNextAction(s_elapsed);
    }
    for (Powerup powerup : powerups) {
      powerup.planNextAction(s_elapsed);
    }
    for (Pyro pyro : pyros) {
      pyro.planNextAction(s_elapsed);
    }
    for (Unit unit : mechs) {
      unit.planNextAction(s_elapsed);
    }
    for (Scenery scenery : sceneries) {
      scenery.planNextAction(s_elapsed);
    }
    for (MapObject object : misc_objects) {
      object.planNextAction(s_elapsed);
    }
  }

  public LinkedList<MapObject> doNextStep(MapEngine engine, double s_elapsed) {
    LinkedList<MapObject> created_objects = doNextStep(engine, s_elapsed, shots);
    created_objects.addAll(doNextStep(engine, s_elapsed, powerups));
    created_objects.addAll(doNextStep(engine, s_elapsed, pyros));
    created_objects.addAll(doNextStep(engine, s_elapsed, mechs));
    created_objects.addAll(doNextStep(engine, s_elapsed, sceneries));
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

  public void doSplashDamage(MapObject src_object, int max_damage, double damage_radius,
          Unit already_damaged) {
    doSplashDamage(src_object, max_damage, damage_radius, null, already_damaged);
  }

  public void doSplashDamage(MapObject src_object, int max_damage, double damage_radius,
          RoomSide which_neighbor, Unit already_damaged) {
    for (Pyro pyro : pyros) {
      if (!pyro.equals(already_damaged)) {
        applySplashDamage(pyro, src_object, max_damage, damage_radius, which_neighbor);
      }
    }
    for (Unit unit : mechs) {
      if (!unit.equals(already_damaged)) {
        applySplashDamage(unit, src_object, max_damage, damage_radius, which_neighbor);
      }
    }

    // check in neighbor Rooms
    double src_x = src_object.getX();
    double src_y = src_object.getY();
    if (which_neighbor == null) {
      // north neighbor
      if (nw_corner.y - src_y < damage_radius) {
        doSplashDamageInNeighbor(src_object, max_damage, damage_radius, RoomSide.NORTH);
      }

      // south neighbor
      if (src_y - se_corner.y < damage_radius) {
        doSplashDamageInNeighbor(src_object, max_damage, damage_radius, RoomSide.SOUTH);
      }

      // west neighbor
      if (nw_corner.x - src_x < damage_radius) {
        doSplashDamageInNeighbor(src_object, max_damage, damage_radius, RoomSide.WEST);
      }

      // east neighbor
      if (src_x - se_corner.x < damage_radius) {
        doSplashDamageInNeighbor(src_object, max_damage, damage_radius, RoomSide.EAST);
      }
    }
  }

  public void doSplashDamageInNeighbor(MapObject src_object, int max_damage,
          double damage_radius, RoomSide direction) {
    Room neighbor = getNeighborInDirection(direction);
    if (neighbor != null) {
      neighbor.doSplashDamage(src_object, max_damage, damage_radius, direction, null);
    }
  }

  public void applySplashDamage(Unit unit, MapObject src_object, int max_damage,
          double damage_radius, RoomSide which_neighbor) {
    double distance = Math.max(
      Math.hypot(unit.getX() - src_object.getX(), unit.getY() - src_object.getY()) - unit.getRadius(), 0.0);
    if (distance < damage_radius &&
            (which_neighbor == null || MapUtils.canSeeObjectInNeighborRoom(src_object, unit, which_neighbor))) {
      unit.beDamaged((int) (max_damage * (1 - distance / damage_radius)));
    }
  }
}
