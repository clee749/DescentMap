package util;

import mapobject.unit.robot.BabySpider;
import mapobject.unit.robot.Class2Drone;
import mapobject.unit.robot.DefenseRobot;
import mapobject.unit.robot.LightHulk;
import mapobject.unit.robot.MediumHulk;
import mapobject.unit.robot.PlatformLaser;
import mapobject.unit.robot.PlatformMissile;
import mapobject.unit.robot.Robot;
import mapobject.unit.robot.SecondaryLifter;
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
      case DefenseRobot:
        return new DefenseRobot(room, x_loc, y_loc, direction);
      case LightHulk:
        return new LightHulk(room, x_loc, y_loc, direction);
      case MediumHulk:
        return new MediumHulk(room, x_loc, y_loc, direction);
      case PlatformLaser:
        return new PlatformLaser(room, x_loc, y_loc, direction);
      case PlatformMissile:
        return new PlatformMissile(room, x_loc, y_loc, direction);
      case SecondaryLifter:
        return new SecondaryLifter(room, x_loc, y_loc, direction);
      default:
        throw new DescentMapException("Unexpected Robot type: " + type);
    }
  }
}
