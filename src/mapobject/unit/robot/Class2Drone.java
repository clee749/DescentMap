package mapobject.unit.robot;

import gunner.Gunner;
import pilot.Pilot;
import structure.Room;

import common.ObjectType;

public class Class2Drone extends Robot {

  public Class2Drone(Pilot pilot, Gunner gunner, Room room, double x_loc, double y_loc, double direction) {
    super(pilot, gunner, room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Class2Drone;
  }
}
