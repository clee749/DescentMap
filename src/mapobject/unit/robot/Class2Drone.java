package mapobject.unit.robot;

import mapobject.MapObject;
import mapobject.MultipleObject;
import mapobject.powerup.Shield;
import structure.Room;
import util.MapUtils;

import common.Constants;
import common.ObjectType;

public class Class2Drone extends Robot {

  public Class2Drone(Room room, double x_loc, double y_loc, double direction) {
    super(room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Class2Drone;
  }

  @Override
  public MapObject releasePowerups() {
    MultipleObject objects = new MultipleObject();
    for (int i = 0; i < 2; ++i) {
      objects.addObject(new Shield(room, x_loc, y_loc, Math.random() * MapUtils.TWO_PI, Math.random() *
              Constants.POWERUP_MAX_SPEED));
    }
    return objects;
    // if (Math.random() < 0.1) {
    // return new Shield(room, x_loc, y_loc, Math.random() * MapUtils.TWO_PI, Math.random());
    // }
    // return null;
  }
}
