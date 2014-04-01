package mapobject.unit.robot;

import mapobject.MapObject;
import mapobject.MultipleObject;
import mapobject.shot.Shot;
import mapobject.unit.Unit;
import structure.Room;
import util.PowerupFactory;
import cannon.ConcussionMissileCannon;

import common.ObjectType;

public class MediumHulk extends Robot {
  public static final int MAX_CONCUSSION_MISSILES_RELEASED = 3;

  public MediumHulk(Room room, double x_loc, double y_loc, double direction) {
    super(Unit.getRadius(ObjectType.MediumHulk), new ConcussionMissileCannon(
            Shot.getDamage(ObjectType.ConcussionMissile), false), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.MediumHulk;
  }

  @Override
  public MapObject releasePowerups() {
    int num_concussion_missiles = (int) (Math.random() * (MAX_CONCUSSION_MISSILES_RELEASED + 1));
    if (num_concussion_missiles < 1) {
      return null;
    }
    MultipleObject powerups = new MultipleObject();
    for (int i = 0; i < num_concussion_missiles; ++i) {
      powerups.addObject(PowerupFactory.newReleasedPowerup(ObjectType.ConcussionMissilePowerup, room, x_loc,
              y_loc));
    }
    return powerups;
  }
}
