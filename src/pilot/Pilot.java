package pilot;

import java.util.Map.Entry;

import mapobject.unit.Unit;
import structure.Room;
import structure.RoomConnection;

import common.RoomSide;

public abstract class Pilot {
  protected Unit unit;
  // we cannot just use unit.getRoom because we could go into an unintentional Room while turning
  protected Room current_room;
  protected Entry<RoomSide, RoomConnection> target_room_info;
  protected double target_x;
  protected double target_y;
  protected double target_direction;

  public Pilot(Unit unit) {
    this.unit = unit;
    current_room = unit.getRoom();
  }

  public double getTargetX() {
    return target_x;
  }

  public double getTargetY() {
    return target_y;
  }

  public abstract PilotMove findNextMove();

  public abstract void updateCurrentRoom(Room room);
}
