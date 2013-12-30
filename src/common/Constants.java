package common;

import java.awt.Color;

public class Constants {
  // builder
  public static final int BUILDER_MAX_ROOM_SIZE = 10;
  public static final int BUILDER_MAX_NUM_ROOMS = 100;
  public static final int BUILDER_EXIT_ROOM_LENGTH = 20;

  // construction displayer
  public static final int CONSTRUCTION_MIN_SIGHT_DIAMETER = 10;

  // room
  public static final int ROOM_WALL_THICKNESS = 2;
  public static final Color ROOM_WALL_COLOR = Color.gray;

  // runner
  public static final long RUNNER_BUILD_SLEEP = 100;
  public static final long RUNNER_PAUSE_AFTER_BUILD_SLEEP = 1000;
  public static final long RUNNER_PLAY_MIN_SLEEP = 1000;
  public static final long RUNNER_PLAY_MAX_SLEEP = 11000;
  public static final long RUNNER_PAUSE_AFTER_PLAY_SLEEP = 1000;

  protected Constants() {

  }
}
