package mapobject.unit.robot;

import mapobject.MapObject;
import mapobject.shot.Shot;
import mapobject.unit.Unit;
import structure.Room;
import util.PowerupFactory;
import cannon.PlasmaCannon;

import common.ObjectType;

public class HeavyDriller extends Robot {
  public HeavyDriller(Room room, double x_loc, double y_loc, double direction) {
    super(Unit.getRadius(ObjectType.HeavyDriller), new PlasmaCannon(Shot.getDamage(ObjectType.PlasmaShot),
            false), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.HeavyDriller;
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
      return PowerupFactory.newReleasedPowerup(ObjectType.PlasmaCannonPowerup, room, x_loc, y_loc);
    }
    return null;
  }
}
