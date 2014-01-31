package mapobject.shot;

import mapobject.MapObject;
import pilot.HomingPilot;
import pilot.HomingTargetType;
import structure.Room;

import common.ObjectType;

public class HomingMissile extends Shot {
  public HomingMissile(MapObject source, int damage, Room room, double x_loc, double y_loc, double direction) {
    super(new HomingPilot(source.getType().equals(ObjectType.Pyro)
            ? HomingTargetType.ROBOT
            : HomingTargetType.PYRO), source, damage, room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.HomingMissile;
  }
}