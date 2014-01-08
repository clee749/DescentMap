package util;

import mapobject.unit.robot.Class2Drone;
import mapobject.unit.robot.Robot;
import pilot.RobotPilot;
import structure.Room;

import common.DescentMapException;
import common.ObjectType;

public class RobotFactory {
  protected RobotFactory() {

  }

  public static Robot newRobot(ObjectType type, Room room, double x_loc, double y_loc, double direction) {
    switch (type) {
      case Class2Drone:
        return new Class2Drone(new RobotPilot(), room, x_loc, y_loc, direction);
      default:
        throw new DescentMapException("Invalid Robot type: " + type);
    }
  }
}
