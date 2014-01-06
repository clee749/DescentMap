package mapobject.unit.robot;

import gunner.Gunner;
import mapobject.MapObject;
import mapobject.unit.Unit;
import pilot.Pilot;
import structure.Room;

public abstract class Robot extends Unit {

  public Robot(Pilot pilot, Gunner gunner, Room room, double x_loc, double y_loc, double direction) {
    super(pilot, gunner, room, x_loc, y_loc, direction);
  }

  @Override
  public MapObject fireCannon() {
    return null;
  }
}
