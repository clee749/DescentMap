package util;

import mapobject.unit.robot.BabySpider;
import mapobject.unit.robot.Bomber;
import mapobject.unit.robot.Class1Drone;
import mapobject.unit.robot.Class2Drone;
import mapobject.unit.robot.DefenseRobot;
import mapobject.unit.robot.HeavyDriller;
import mapobject.unit.robot.HeavyHulk;
import mapobject.unit.robot.LightHulk;
import mapobject.unit.robot.MediumHulk;
import mapobject.unit.robot.PlatformLaser;
import mapobject.unit.robot.PlatformMissile;
import mapobject.unit.robot.Robot;
import mapobject.unit.robot.SecondaryLifter;
import mapobject.unit.robot.Spider;
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
      case Bomber:
        return new Bomber(room, x_loc, y_loc, direction);
      case Class1Drone:
        return new Class1Drone(room, x_loc, y_loc, direction);
      case Class2Drone:
        return new Class2Drone(room, x_loc, y_loc, direction);
      case DefenseRobot:
        return new DefenseRobot(room, x_loc, y_loc, direction);
      case HeavyDriller:
        return new HeavyDriller(room, x_loc, y_loc, direction);
      case HeavyHulk:
        return new HeavyHulk(room, x_loc, y_loc, direction);
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
      case Spider:
        return new Spider(room, x_loc, y_loc, direction);
      default:
        throw new DescentMapException("Unexpected Robot type: " + type);
    }
  }

  public static Robot newRobot(ObjectType type, Room room, double x_loc, double y_loc, double direction,
          double inactive_time) {
    Robot robot = newRobot(type, room, x_loc, y_loc, direction);
    robot.tempDisable(inactive_time);
    return robot;
  }
}
