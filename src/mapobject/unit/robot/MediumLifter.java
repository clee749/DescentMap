package mapobject.unit.robot;

import mapobject.MapObject;
import mapobject.unit.Unit;
import structure.Room;
import util.PowerupFactory;

import common.ObjectType;

public class MediumLifter extends MeleeRobot {
  public MediumLifter(Room room, double x_loc, double y_loc, double direction) {
    super(Unit.getRadius(ObjectType.MediumLifter), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.MediumLifter;
  }

  @Override
  public MapObject releasePowerups() {
    if (Math.random() < 0.1) {
      return PowerupFactory.newPowerup(ObjectType.Shield, room, x_loc, y_loc);
    }
    return null;
  }
}
