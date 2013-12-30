package common;

import java.awt.Point;

public enum RoomSide {
  EAST, NORTH, WEST, SOUTH;

  public static RoomSide opposite(RoomSide direction) {
    if (direction == null) {
      return null;
    }
    if (direction.equals(NORTH)) {
      return SOUTH;
    }
    if (direction.equals(SOUTH)) {
      return NORTH;
    }
    if (direction.equals(WEST)) {
      return EAST;
    }
    return WEST;
  }

  public static Point dxdy(RoomSide direction) {
    if (direction != null) {
      if (direction.equals(NORTH)) {
        return new Point(0, -1);
      }
      if (direction.equals(SOUTH)) {
        return new Point(0, 1);
      }
      if (direction.equals(WEST)) {
        return new Point(-1, 0);
      }
      if (direction.equals(EAST)) {
        return new Point(1, 0);
      }
    }
    return new Point(0, 0);
  }

  public static RoomSide next(RoomSide direction) {
    return RoomSide.values()[(direction.ordinal() + 1) % 4];
  }

  public static RoomSide[] adjacents(RoomSide direction) {
    RoomSide[] dirs;
    if (direction.equals(NORTH) || direction.equals(SOUTH)) {
      dirs = new RoomSide[] {WEST, EAST};
    }
    else {
      dirs = new RoomSide[] {NORTH, SOUTH};
    }
    return dirs;
  }

  public static RoomSide direction(Point dxdy) {
    if (dxdy.x == 0 && dxdy.y == 0) {
      return null;
    }
    if (Math.abs(dxdy.x) > Math.abs(dxdy.y)) {
      if (dxdy.x > 0) {
        return EAST;
      }
      return WEST;
    }
    if (dxdy.y > 0) {
      return SOUTH;
    }
    return NORTH;
  }

  public static RoomSide bestDirection(double curx, double cury, double tarx, double tary,
          RoomSide default_dir) {
    if ((Math.abs(curx - tarx) < 0.1) && (Math.abs(cury - tary) < 0.1)) {
      return default_dir;
    }
    if (Math.abs(tarx - curx) > Math.abs(tary - cury)) {
      if (tarx - curx > 0) {
        return EAST;
      }
      return WEST;
    }
    if (tary - cury > 0) {
      return SOUTH;
    }
    return NORTH;
  }
}
