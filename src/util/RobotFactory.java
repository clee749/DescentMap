package util;

import mapobject.unit.robot.BabySpider;
import mapobject.unit.robot.Class2Drone;
import mapobject.unit.robot.LightHulk;
import mapobject.unit.robot.PlatformLaser;
import mapobject.unit.robot.Robot;
import structure.Room;

import common.DescentMapException;
import common.ObjectType;

public class RobotFactory {
  protected RobotFactory() {

  }

  public static Robot newRobot(ObjectType type, Room room, double x_loc, double y_loc, double direction) {
    switch (type) {
      case BabySpider:
        return new BabySpider(room, x_loc, y_loc, direction);
      case Class2Drone:
        return new Class2Drone(room, x_loc, y_loc, direction);
      case LightHulk:
        return new LightHulk(room, x_loc, y_loc, direction);
      case PlatformLaser:
        return new PlatformLaser(room, x_loc, y_loc, direction);
      default:
        throw new DescentMapException("Invalid Robot type: " + type);
    }
  }
}
