package structure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;

import mapobject.unit.pyro.Pyro;

import common.Constants;
import common.DescentMapException;
import component.MapBuilder;
import component.MapConstructionDisplayer;
import component.MapDisplayer;

enum DisplayMode {
  CONSTRUCTION, PLAYTHROUGH;
}


public class DescentMap {
  private final MapBuilder builder;
  private final ArrayList<Room> rooms;
  private final MapConstructionDisplayer construction_displayer;
  private final MapDisplayer map_displayer;
  private DisplayMode display_mode;
  private Room entrance_room;
  private Room exit_room;
  private Pyro center_ship;

  public DescentMap(int max_room_size) {
    builder = new MapBuilder(max_room_size);
    rooms = builder.getRooms();
    construction_displayer = new MapConstructionDisplayer(builder);
    map_displayer = new MapDisplayer(this);
    display_mode = DisplayMode.CONSTRUCTION;
  }

  public Iterator<Room> getRooms() {
    return rooms.iterator();
  }

  public Room getEntranceRoom() {
    return entrance_room;
  }

  public Room getExitRoom() {
    return exit_room;
  }

  public void addRoom() {
    builder.addRoom();
  }

  public void finishBuildingMap() {
    builder.placeEntranceAndExitRooms();
    entrance_room = builder.getEntranceRoom();
    exit_room = builder.getExitRoom();
    display_mode = DisplayMode.PLAYTHROUGH;
  }

  public void insertPyro(Pyro ship) {
    center_ship = ship;
  }

  public void paint(Graphics2D g, Dimension dims) {
    g.setColor(Color.gray);
    g.setStroke(new BasicStroke(Constants.ROOM_WALL_THICKNESS));
    switch (display_mode) {
      case CONSTRUCTION:
        construction_displayer.displayMap(g, dims);
        break;
      case PLAYTHROUGH:
        construction_displayer.displayMap(g, dims);
        construction_displayer.displayShip(g, center_ship);
        break;
      default:
        throw new DescentMapException("Unexpected DisplayMode: " + display_mode);
    }
  }
}
