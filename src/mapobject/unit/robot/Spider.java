package mapobject.unit.robot;

import java.awt.geom.Point2D;

import mapobject.MapObject;
import mapobject.MultipleObject;
import mapobject.unit.Unit;
import structure.Room;
import util.MapUtils;
import util.PowerupFactory;
import util.RobotFactory;
import cannon.FireballCannon;

import common.DescentMapException;
import common.ObjectType;

public class Spider extends Robot {
  public static final int SPIDER_CANNON_DAMAGE = 8;
  public static final int MAX_NUM_CHILDREN = 3;

  public Spider(Room room, double x_loc, double y_loc, double direction) {
    super(Unit.getRadius(ObjectType.Spider), new FireballCannon(SPIDER_CANNON_DAMAGE), room, x_loc, y_loc,
            direction);
    cannon_side = (int) (Math.random() * 3);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Spider;
  }

  @Override
  public MapObject releasePowerups() {
    if (Math.random() < 0.1) {
      return PowerupFactory.newPowerup(ObjectType.Shield, room, x_loc, y_loc);
    }
    return null;
  }

  @Override
  public MapObject fireCannon() {
    ++cannon_side;
    switch (cannon_side % 3) {
      case 0:
        Point2D.Double abs_offset = MapUtils.perpendicularVector(cannon_offset, direction);
        return cannon.fireCannon(this, room, x_loc + abs_offset.x, y_loc + abs_offset.y, direction);
      case 1:
        abs_offset = MapUtils.perpendicularVector(cannon_offset, direction);
        return cannon.fireCannon(this, room, x_loc - abs_offset.x, y_loc - abs_offset.y, direction);
      case 2:
        return cannon.fireCannon(this, room, x_loc, y_loc, direction);
      default:
        throw new DescentMapException("Mod is broken!");
    }
  }

  @Override
  public MapObject handleDeath(double s_elapsed) {
    MapObject created_objects = super.handleDeath(s_elapsed);
    if (!is_in_map) {
      MultipleObject death_objects = new MultipleObject();
      death_objects.addObject(created_objects);
      death_objects.addObject(spawnChildren());
      return death_objects;
    }
    return created_objects;
  }

  public MapObject spawnChildren() {
    int num_children = (int) (Math.random() * (MAX_NUM_CHILDREN + 1));
    if (num_children < 1) {
      return null;
    }
    double radius_range = radius - Unit.getRadius(ObjectType.BabySpider);
    double location_range = 2 * radius_range;
    MultipleObject children = new MultipleObject();
    for (int i = 0; i < num_children; ++i) {
      children.addObject(RobotFactory.newRobot(ObjectType.BabySpider, room, x_loc + Math.random() *
              location_range - radius_range, y_loc + Math.random() * location_range - radius_range, direction));
    }
    return children;
  }
}
