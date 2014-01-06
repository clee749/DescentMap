package structure;

import java.util.ArrayList;

import mapobject.MapObject;

import component.MapBuilder;

public class DescentMap {
  private final MapBuilder builder;
  private final ArrayList<Room> rooms;
  private Room entrance_room;
  private Room exit_room;
  private MapObject center_object;

  public DescentMap(int max_room_size) {
    builder = new MapBuilder(max_room_size);
    rooms = builder.getRooms();
  }

  public boolean hasRooms() {
    return !rooms.isEmpty();
  }

  public ArrayList<Room> getRooms() {
    return rooms;
  }

  public int getMinX() {
    return builder.getMinX();
  }

  public int getMaxX() {
    return builder.getMaxX();
  }

  public int getMinY() {
    return builder.getMinY();
  }

  public int getMaxY() {
    return builder.getMaxY();
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

  public MapObject getCenterObject() {
    return center_object;
  }

  public void setCenterObject(MapObject center_object) {
    this.center_object = center_object;
  }

  public void finishBuildingMap() {
    builder.placeEntranceAndExitRooms();
    entrance_room = builder.getEntranceRoom();
    exit_room = builder.getExitRoom();
  }
}
