package mapobject.unit.robot;

import pilot.Pilot;
import structure.Room;

import common.ObjectType;

public class Class2Drone extends Robot {

  public Class2Drone(Pilot pilot, Room room, double x_loc, double y_loc, double direction) {
    super(pilot, room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Class2Drone;
  }
}
