package mapobject.unit.robot;

import java.awt.geom.Point2D;

import mapobject.MapObject;
import mapobject.shot.Shot;
import mapobject.unit.Unit;
import structure.Room;
import util.MapUtils;
import cannon.LaserCannon;

import common.ObjectType;

public class SecondaryLifter extends Robot {
  public SecondaryLifter(Room room, double x_loc, double y_loc, double direction) {
    super(Unit.getRadius(ObjectType.SecondaryLifter),
            new LaserCannon(Shot.getDamage(ObjectType.LaserShot), 1), room, x_loc, y_loc, direction);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.SecondaryLifter;
  }

  @Override
  public MapObject releasePowerups() {
    return null;
  }

  @Override
  public MapObject fireCannon() {
    Point2D.Double abs_offset = MapUtils.perpendicularVector(cannon_offset, direction);
    return cannon.fireCannon(this, room, x_loc - abs_offset.x, y_loc - abs_offset.y, direction);
  }
}
