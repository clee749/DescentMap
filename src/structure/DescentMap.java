package structure;

import java.util.ArrayList;

import mapobject.MapObject;
import mapobject.scenery.Entrance;
import mapobject.unit.Pyro;

import common.RoomSide;
import component.MapBuilder;

public class DescentMap {
  private final MapBuilder builder;
  private final ArrayList<Room> all_rooms;
  private Room entrance_room;
  private Room exit_room;
  private MineExteriorRoom exterior_room;
  private MapObject center_object;
  private Entrance entrance;

  public DescentMap(int max_room_size) {
    builder = new MapBuilder(max_room_size);
    all_rooms = builder.getAllRooms();
  }

  public boolean hasRooms() {
    return !all_rooms.isEmpty();
  }

  public ArrayList<Room> getAllRooms() {
    return all_rooms;
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

  public RoomSide getEntranceSide() {
    return builder.getEntranceSide();
  }

  public RoomSide getExitSide() {
    return builder.getExitSide();
  }

  public MineExteriorRoom getExteriorRoom() {
    return exterior_room;
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

  public void setEntrance(Entrance entrance) {
    this.entrance = entrance;
  }

  public void finishBuildingMap() {
    builder.placeEntranceAndExitRooms();
    entrance_room = builder.getEntranceRoom();
    exit_room = builder.getExitRoom();
    exterior_room = builder.getExteriorRoom();
  }

  public void spawnPyro(Pyro pyro, boolean is_center_object) {
    entrance.spawnPyro(pyro, is_center_object);
  }
}
