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

  // pilot
  public static final double PILOT_DIRECTION_EPSILON = 0.1; // 1 radian ~ 57 degrees

  // play displayer
  public static final int PLAY_SIGHT_RADIUS = 3;

  // Pyro additional constants
  public static final double PYRO_OUTER_CANNON_OFFSET = 0.8;
  public static final double PYRO_CANNON_FORWARD_OFFSET = 0.2;
  public static final double PYRO_MISSILE_OFFSET = 0.2;

  // robot
  public static final double ROBOT_START_EXPLORE_PROB = 0.1;
  public static final double ROBOT_END_EXPLORE_PROB = 0.1;

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

  // objects
  public static Double getRadius(ObjectType type) {
    return Constants.RADII.get(type);
  }

  public static Double getMoveSpeed(ObjectType type) {
    return Constants.MOVE_SPEEDS.get(type);
  }

  public static Double getTurnSpeed(ObjectType type) {
    return Constants.TURN_SPEEDS.get(type);
  }

  public static Double getReloadTime(ObjectType type) {
    return Constants.RELOAD_TIMES.get(type);
  }

  public static Integer getShotsPerVolley(ObjectType type) {
    return Constants.SHOTS_PER_VOLLEYS.get(type);
  }

  public static Double getVolleyReloadTime(ObjectType type) {
    return Constants.VOLLEY_RELOAD_TIMES.get(type);
  }

  public static Double getCannonOffset(ObjectType type) {
    return Constants.CANNON_OFFSETS.get(type);
  }

  private static final HashMap<ObjectType, Double> RADII = Constants.getRadii();
  private static final HashMap<ObjectType, Double> MOVE_SPEEDS = Constants.getMoveSpeeds();
  private static final HashMap<ObjectType, Double> TURN_SPEEDS = Constants.getTurnSpeeds();
  private static final HashMap<ObjectType, Double> RELOAD_TIMES = Constants.getReloadTimes();
  private static final HashMap<ObjectType, Integer> SHOTS_PER_VOLLEYS = Constants.getShotsPerVolleys();
  private static final HashMap<ObjectType, Double> VOLLEY_RELOAD_TIMES = Constants.getVolleyReloadTimes();
  private static final HashMap<ObjectType, Double> CANNON_OFFSETS = Constants.getCannonOffsets();

  private static HashMap<ObjectType, Double> getRadii() {
    HashMap<ObjectType, Double> radii = new HashMap<ObjectType, Double>();
    radii.put(ObjectType.Pyro, 0.25);
    radii.put(ObjectType.LaserShot, 0.1);
    radii.put(ObjectType.Class2Drone, 0.2);
    return radii;
  }

  private static HashMap<ObjectType, Double> getMoveSpeeds() {
    HashMap<ObjectType, Double> speeds = new HashMap<ObjectType, Double>();
    speeds.put(ObjectType.Pyro, 1.0);
    speeds.put(ObjectType.LaserShot, 3.0);
    speeds.put(ObjectType.Class2Drone, 0.5);
    return speeds;
  }

  private static HashMap<ObjectType, Double> getTurnSpeeds() {
    HashMap<ObjectType, Double> speeds = new HashMap<ObjectType, Double>();
    speeds.put(ObjectType.Pyro, 1.0);
    speeds.put(ObjectType.Class2Drone, 1.0);
    return speeds;
  }

  private static HashMap<ObjectType, Double> getReloadTimes() {
    HashMap<ObjectType, Double> times = new HashMap<ObjectType, Double>();
    times.put(ObjectType.Pyro, 3.0);
    times.put(ObjectType.Class2Drone, 2.0);
    return times;
  }

  private static HashMap<ObjectType, Integer> getShotsPerVolleys() {
    HashMap<ObjectType, Integer> shots = new HashMap<ObjectType, Integer>();
    shots.put(ObjectType.Pyro, 4);
    shots.put(ObjectType.Class2Drone, 2);
    return shots;
  }

  private static HashMap<ObjectType, Double> getVolleyReloadTimes() {
    HashMap<ObjectType, Double> times = new HashMap<ObjectType, Double>();
    times.put(ObjectType.Pyro, 0.1);
    times.put(ObjectType.Class2Drone, 0.2);
    return times;
  }

  private static HashMap<ObjectType, Double> getCannonOffsets() {
    HashMap<ObjectType, Double> offsets = new HashMap<ObjectType, Double>();
    offsets.put(ObjectType.Pyro, 0.525);
    offsets.put(ObjectType.Class2Drone, 0.0);
    return offsets;
  }

  protected Constants() {

  }
}
