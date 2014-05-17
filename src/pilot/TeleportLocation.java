package pilot;

import structure.Room;

public class TeleportLocation {
  public final Room room;
  public final double x_loc;
  public final double y_loc;

  public TeleportLocation(Room room, double x_loc, double y_loc) {
    this.room = room;
    this.x_loc = x_loc;
    this.y_loc = y_loc;
  }
}
