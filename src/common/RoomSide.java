package common;

import util.MapUtils;

public enum RoomSide {
  EAST,
  NORTH,
  WEST,
  SOUTH;

  public static RoomSide opposite(RoomSide direction) {
    if (direction == null) {
      return null;
    }
    return RoomSide.values()[(direction.ordinal() + 2) % 4];
  }

  public static double directionToRadians(RoomSide direction) {
    // direction for north and south reversed because y-coordinates increase down screen
    switch (direction) {
      case EAST:
        return 0.0;
      case NORTH:
        return MapUtils.THREE_PI_OVER_TWO;
      case WEST:
        return Math.PI;
      case SOUTH:
        return MapUtils.PI_OVER_TWO;
      default:
        throw new DescentMapException("Unexpected RoomSide: " + direction);
    }
  }

  public static RoomSide closestRoomSide(double direction) {
    if (direction < MapUtils.PI_OVER_FOUR || direction > MapUtils.SEVEN_PI_OVER_FOUR) {
      return EAST;
    }
    if (direction < MapUtils.THREE_PI_OVER_FOUR) {
      return SOUTH;
    }
    if (direction < MapUtils.FIVE_PI_OVER_FOUR) {
      return WEST;
    }
    return NORTH;
  }
}
