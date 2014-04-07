package mapobject.unit.robot;

import mapobject.MapObject;
import mapobject.ProximityBomb;
import mapobject.unit.Unit;
import pilot.BomberPilot;
import structure.Room;
import util.PowerupFactory;
import cannon.ProximityBombCannon;

import common.ObjectType;

public class Gopher extends Robot {
  public Gopher(Room room, double x_loc, double y_loc, double direction) {
    super(Unit.getRadius(ObjectType.Gopher), new BomberPilot(), new ProximityBombCannon(ProximityBomb.DAMAGE,
            false), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Gopher;
  }

  @Override
  public MapObject releasePowerups() {
    if (Math.random() < 0.1) {
      return PowerupFactory.newReleasedPowerup(ObjectType.Shield, room, x_loc, y_loc);
    }
    return null;
  }
}
