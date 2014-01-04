package pilot;

import java.util.Map.Entry;

import structure.Room;
import structure.RoomConnection;

import common.RoomSide;

public abstract class Pilot {
  protected final double object_radius;
  // we cannot just use unit.getRoom because we could go into an unintentional Room while turning
  protected Room current_room;
  protected Entry<RoomSide, RoomConnection> target_room_info;
  protected double target_x;
  protected double target_y;
  protected double target_direction;

  public Pilot(double object_radius, Room current_room) {
    this.object_radius = object_radius;
    this.current_room = current_room;
  }

  public double getTargetX() {
    return target_x;
  }

  public double getTargetY() {
    return target_y;
  }

  public void updateCurrentRoom(Room room) {
    current_room = room;
  }

  public abstract PilotMove findNextMove(double current_x, double current_y, double current_direction);
}
