package mapobject.unit.robot;

import mapobject.MapObject;
import structure.Room;
import util.PowerupFactory;
import cannon.PlasmaCannon;

import common.Constants;
import common.ObjectType;

public class HeavyDriller extends Robot {
  public HeavyDriller(Room room, double x_loc, double y_loc, double direction) {
    super(Constants.getRadius(ObjectType.HeavyDriller), new PlasmaCannon(
            Constants.getDamage(ObjectType.PlasmaShot)), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.HeavyDriller;
  }

  @Override
  public MapObject releasePowerups() {
    double rand = Math.random();
    if (rand < 0.1) {
      return PowerupFactory.newPowerup(ObjectType.Shield, room, x_loc, y_loc);
    }
    if (rand < 0.2) {
      return PowerupFactory.newPowerup(ObjectType.Energy, room, x_loc, y_loc);
    }
    if (rand < 0.5) {
      return PowerupFactory.newPowerup(ObjectType.PlasmaCannonPowerup, room, x_loc, y_loc);
    }
    return null;
  }
}
