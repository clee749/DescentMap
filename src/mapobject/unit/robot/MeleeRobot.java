package mapobject.unit.robot;

import mapobject.unit.Pyro;
import pilot.MeleeRobotPilot;
import structure.Room;

import component.MapEngine;

public abstract class MeleeRobot extends Robot {
  public static final int CLAWS_DAMAGE = 3;

  public MeleeRobot(double radius, Room room, double x_loc, double y_loc, double direction) {
    super(radius, new MeleeRobotPilot(), null, room, x_loc, y_loc, direction);
  }

  @Override
  public void handlePyroCollision(MapEngine engine, Pyro pyro, double dx, double dy, double distance) {
    pyro.bePushed(engine, -dx / distance, -dy / distance);
    if (reload_time_left < 0.0) {
      pyro.beDamaged(engine, CLAWS_DAMAGE, false);
      reload_time_left = reload_time;
      pyro.playPublicSound("enemies/tear01.wav");
    }
    else {
      pyro.playPublicSound("effects/ramfast.wav");
    }
  }
}
