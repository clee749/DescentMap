package mapstructure;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import engine.MapBuilder;
import engine.MapConstructionDisplayer;

public class DescentMap {
  private final MapBuilder builder;
  private final ArrayList<Room> rooms;
  private final MapConstructionDisplayer construction_displayer;
  
  public DescentMap(int max_room_size) {
    builder = new MapBuilder(max_room_size);
    rooms = builder.getRooms();
    construction_displayer = new MapConstructionDisplayer(builder);
  }
  
  public MapConstructionDisplayer getConstructionDisplayer() {
    return construction_displayer;
  }
  
  public void addRoom() {
    builder.addRoom();
  }
  
  public void paint(Graphics2D g, Point center_cell, Point center_cell_corner_pixel) {
    int pixels_per_cell = 10;
    for (Room room : rooms) {
      room.paint(g, center_cell, center_cell_corner_pixel, pixels_per_cell);
    }
  }
}
