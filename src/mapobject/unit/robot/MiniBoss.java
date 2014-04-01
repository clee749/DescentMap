package mapobject.unit.robot;

import mapobject.MapObject;
import mapobject.shot.Shot;
import mapobject.unit.Unit;
import pilot.LargeRobotPilot;
import structure.Room;
import util.PowerupFactory;
import cannon.FusionCannon;

import common.ObjectType;

public class MiniBoss extends Robot {
  public static final int MIN_ROOM_SIZE = 2;

  public MiniBoss(Room room, double x_loc, double y_loc, double direction) {
    super(Unit.getRadius(ObjectType.MiniBoss), new LargeRobotPilot(MIN_ROOM_SIZE), new FusionCannon(
            Shot.getDamage(ObjectType.FusionShot), false), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.MiniBoss;
  }

  @Override
  public MapObject releasePowerups() {
    double rand = Math.random();
    if (rand < 0.1) {
      return PowerupFactory.newReleasedPowerup(ObjectType.Shield, room, x_loc, y_loc);
    }
    if (rand < 0.2) {
      return PowerupFactory.newReleasedPowerup(ObjectType.Energy, room, x_loc, y_loc);
    }
    if (rand < 0.5) {
      return PowerupFactory.newReleasedPowerup(ObjectType.FusionCannonPowerup, room, x_loc, y_loc);
    }
    return null;
  }
}
