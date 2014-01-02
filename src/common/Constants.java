package common;

import java.awt.Color;

public class Constants {
  // builder
  public static final int BUILDER_MAX_ROOM_SIZE = 10;
  public static final int BUILDER_MAX_NUM_ROOMS = 10;
  public static final int BUILDER_EXIT_ROOM_LENGTH = 10;

  // construction displayer
  public static final int CONSTRUCTION_MIN_SIGHT_DIAMETER = 10;

  // pilot
  public static final double PILOT_LOCATION_EPSILON = 0.1;
  public static final double PILOT_DIRECTION_EPSILON = 0.1; // 1 radian ~ 57 degrees

  // pyro
  public static final double PYRO_MOVE_SPEED = 1.0;
  public static final double PYRO_TURN_SPEED = 1.0;
  public static final double PYRO_RADIUS = 0.25;

  // room
  public static final int ROOM_WALL_THICKNESS = 2;
  public static final Color ROOM_WALL_COLOR = Color.gray;

  // runner
  public static final int RUNNER_NUM_LEVELS = 15;
  public static final long RUNNER_BUILD_SLEEP = 100;
  public static final long RUNNER_PAUSE_AFTER_BUILD_SLEEP = 1000;
  public static final long RUNNER_PLAY_MIN_SLEEP = 100;
  public static final long RUNNER_PLAY_MAX_SLEEP = 110;
  public static final long RUNNER_PAUSE_AFTER_PLAY_SLEEP = 1000;

  protected Constants() {

  }
}
