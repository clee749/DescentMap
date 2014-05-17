package component.populator;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import mapobject.unit.Unit;
import structure.DescentMap;
import structure.LockedDoor;
import structure.Room;
import util.MapUtils;
import util.PowerupFactory;
import util.RobotFactory;

import common.ObjectType;
import common.RoomSide;

public class BossLevelPopulator extends BossChambersPopulator {
  private final ArrayList<Room> boss_chambers;

  public BossLevelPopulator(DescentMap map) {
    super(map);
    boss_chambers = new ArrayList<Room>();
  }

  @Override
  public void populateMap() {
    super.populateMap();
    placeBossRobot();
  }

  @Override
  public void populateConnectorRoom(Room room) {
    Point nw_corner = room.getNWCorner();
    for (int dx = 0; dx < room.getWidth(); ++dx) {
      for (int dy = 0; dy < room.getHeight(); ++dy) {
        room.addChild(PowerupFactory.newStationaryPowerup(
                ObjectType.POWERUPS[(int) (Math.random() * ObjectType.POWERUPS.length)], room, nw_corner.x +
                        dx + 0.5, nw_corner.y + dy + 0.5));
      }
    }
  }

  @Override
  public void populateBossChamber(Room room) {
    super.populateBossChamber(room);
    boss_chambers.add(room);
  }

  @Override
  public void populateBossChamberRoomLocation(Room room, double x_loc, double y_loc,
          RoomSide generator_direction1, RoomSide generator_direction2) {
    room.addChild(PowerupFactory.newStationaryPowerup(
            ObjectType.POWERUPS[(int) (Math.random() * ObjectType.POWERUPS.length)], room, x_loc, y_loc));
  }

  public void placeBossRobot() {
    RoomSide exit_side = map.getExitSide();
    Room pre_exit_room = map.getExitRoom().getNeighborInDirection(RoomSide.opposite(exit_side));
    LockedDoor exit_door = new LockedDoor(pre_exit_room, exit_side, pre_exit_room.removeNeighbor(exit_side));

    Room boss_room = boss_chambers.get((int) (Math.random() * boss_chambers.size()));
    ObjectType type = ObjectType.BOSS_ROBOTS[(int) (Math.random() * ObjectType.BOSS_ROBOTS.length)];
    Point2D.Double location =
            MapUtils.randomInternalPoint(boss_room.getNWCorner(), boss_room.getSECorner(),
                    Unit.getRadius(type));
    boss_room.addChild(RobotFactory.newBossRobot(type, exit_door, boss_room, location.x, location.y,
            Math.random() * MapUtils.TWO_PI));
  }
}
