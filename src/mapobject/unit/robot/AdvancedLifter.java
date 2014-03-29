package mapobject.unit.robot;

import mapobject.MapObject;
import mapobject.unit.Unit;
import structure.Room;
import util.PowerupFactory;

import common.ObjectType;

public class AdvancedLifter extends MeleeRobot {
  public AdvancedLifter(Room room, double x_loc, double y_loc, double direction) {
    super(Unit.getRadius(ObjectType.AdvancedLifter), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.AdvancedLifter;
  }

  @Override
  public MapObject releasePowerups() {
    if (Math.random() < 0.1) {
      return PowerupFactory.newPowerup(ObjectType.Shield, room, x_loc, y_loc);
    }
    return null;
  }
}
