package mapstructure;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import engine.MapBuilder;

public class DescentMap {
  private final MapBuilder builder;
  private final ArrayList<Room> rooms;
  
  public DescentMap(int max_room_size) {
    builder = new MapBuilder(max_room_size);
    rooms = builder.getRooms();
  }
  
  public void addRoom() {
    builder.addRoom();
  }
  
  public void paint(Graphics2D g, Point center_cell, Point center_cell_corner_pixel, int pixels_per_cell) {
    for (Room room : rooms) {
      room.paint(g, center_cell, center_cell_corner_pixel, pixels_per_cell);
    }
  }
}
