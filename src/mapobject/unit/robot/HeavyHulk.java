package mapobject.unit.robot;

import mapobject.MapObject;
import mapobject.MultipleObject;
import mapobject.unit.Unit;
import structure.Room;
import util.PowerupFactory;
import cannon.HomingMissileCannon;

import common.ObjectType;

public class HeavyHulk extends Robot {
  public static final int HOMING_MISSILE_DAMAGE = 5;
  public static final int MAX_HOMING_MISSILES_RELEASED = 3;

  public HeavyHulk(Room room, double x_loc, double y_loc, double direction) {
    super(Unit.getRadius(ObjectType.HeavyHulk), new HomingMissileCannon(HOMING_MISSILE_DAMAGE, false), room,
            x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.HeavyHulk;
  }

  @Override
  public MapObject releasePowerups() {
    int num_homing_missiles = (int) (Math.random() * (MAX_HOMING_MISSILES_RELEASED + 1));
    if (num_homing_missiles < 1) {
      return null;
    }
    MultipleObject powerups = new MultipleObject();
    for (int i = 0; i < num_homing_missiles; ++i) {
      powerups.addObject(PowerupFactory.newPowerup(ObjectType.HomingMissilePowerup, room, x_loc, y_loc));
    }
    return powerups;
  }
}
