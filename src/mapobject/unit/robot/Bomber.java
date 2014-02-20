package mapobject.unit.robot;

import mapobject.MapObject;
import mapobject.ProximityBomb;
import mapobject.unit.Unit;
import pilot.BomberPilot;
import structure.Room;
import util.PowerupFactory;
import cannon.ProximityBombCannon;

import common.ObjectType;

public class Bomber extends Robot {
  public Bomber(Room room, double x_loc, double y_loc, double direction) {
    super(Unit.getRadius(ObjectType.Bomber), new BomberPilot(), new ProximityBombCannon(ProximityBomb.DAMAGE,
            false), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Bomber;
  }

  @Override
  public MapObject releasePowerups() {
    if (Math.random() < 0.1) {
      return PowerupFactory.newPowerup(ObjectType.Energy, room, x_loc, y_loc);
    }
    if (Math.random() < 0.2) {
      return PowerupFactory.newPowerup(ObjectType.ProximityPack, room, x_loc, y_loc);
    }
    return null;
  }
}
