package common;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.HashMap;

public class Constants {
  // builder
  public static final int BUILDER_MAX_ROOM_SIZE = 10;
  public static final int BUILDER_MAX_NUM_ROOMS = 10;
  public static final int BUILDER_EXIT_ROOM_LENGTH = 5;

  // construction displayer
  public static final int CONSTRUCTION_MIN_SIGHT_DIAMETER = 10;

  // images
  public static final String IMAGES_PATH = "/images";
  public static final int IMAGES_NUM_IN_QUADRANT = 10;

  // objects
  public static double getMoveSpeed(ObjectType type) {
    return Constants.OBJECT_MOVE_SPEEDS.get(type);
  }

  public static double getTurnSpeed(ObjectType type) {
    return Constants.OBJECT_TURN_SPEEDS.get(type);
  }

  public static double getRadius(ObjectType type) {
    return Constants.OBJECT_RADII.get(type);
  }

  private static final HashMap<ObjectType, Double> OBJECT_MOVE_SPEEDS = Constants.getObjectMoveSpeeds();
  private static final HashMap<ObjectType, Double> OBJECT_TURN_SPEEDS = Constants.getObjectTurnSpeeds();
  private static final HashMap<ObjectType, Double> OBJECT_RADII = Constants.getObjectRadii();

  private static HashMap<ObjectType, Double> getObjectMoveSpeeds() {
    HashMap<ObjectType, Double> speeds = new HashMap<ObjectType, Double>();
    speeds.put(ObjectType.Pyro, 1.0);
    return speeds;
  }

  private static HashMap<ObjectType, Double> getObjectTurnSpeeds() {
    HashMap<ObjectType, Double> speeds = new HashMap<ObjectType, Double>();
    speeds.put(ObjectType.Pyro, 1.0);
    return speeds;
  }

  private static HashMap<ObjectType, Double> getObjectRadii() {
    HashMap<ObjectType, Double> radii = new HashMap<ObjectType, Double>();
    radii.put(ObjectType.Pyro, 0.25);
    return radii;
  }

  // pilot
  public static final double PILOT_LOCATION_EPSILON = 0.1;
  public static final double PILOT_DIRECTION_EPSILON = 0.1; // 1 radian ~ 57 degrees

  // play displayer
  public static final int PLAY_SIGHT_RADIUS = 3;

  // radians
  public static final double PI_OVER_TWO = Math.PI / 2;
  public static final double THREE_PI_OVER_TWO = 3 * Math.PI / 2;
  public static final double TWO_PI = 2 * Math.PI;

  // room
  public static final Color ROOM_WALL_COLOR = Color.gray;
  public static final Stroke ROOM_WALL_STROKE = new BasicStroke(2);

  // runner
  public static final int RUNNER_NUM_LEVELS = 5;
  public static final long RUNNER_BUILD_SLEEP = 100;
  public static final long RUNNER_PAUSE_AFTER_BUILD_SLEEP = 1000;
  public static final long RUNNER_PAUSE_BEFORE_PLAY_SLEEP = 1000;
  public static final long RUNNER_PLAY_MIN_SLEEP = 100;
  public static final long RUNNER_PLAY_MAX_SLEEP = 110;
  public static final long RUNNER_PAUSE_AFTER_PLAY_SLEEP = 1000;

  protected Constants() {

  }
}
