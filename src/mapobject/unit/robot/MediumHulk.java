package mapobject.unit.robot;

import mapobject.MapObject;
import structure.Room;
import cannon.ConcussionMissileCannon;

import common.Constants;
import common.ObjectType;

public class MediumHulk extends Robot {
  public MediumHulk(Room room, double x_loc, double y_loc, double direction) {
    super(Constants.getRadius(ObjectType.MediumHulk), new ConcussionMissileCannon(
            Constants.getDamage(ObjectType.ConcussionMissile)), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.MediumHulk;
  }

  @Override
  public MapObject releasePowerups() {
    return null;
  }
}
