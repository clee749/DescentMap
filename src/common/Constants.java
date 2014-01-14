package common;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.HashMap;

import util.MapUtils;

public class Constants {
  // builder
  public static final int BUILDER_MAX_ROOM_SIZE = 10;
  public static final int BUILDER_MAX_NUM_ROOMS = 10;
  public static final int BUILDER_EXIT_ROOM_LENGTH = 5;

  // construction displayer
  public static final int CONSTRUCTION_MIN_SIGHT_DIAMETER = 10;

  // explosion
  public static final Color[] EXPLOSION_COLORS = {Color.magenta, Color.orange, Color.pink, Color.red,
          Color.yellow, Color.white};
  public static final double EXPLOSION_DISTANCE_BETWEEN_LAYERS = 0.1;

  // images
  public static final String IMAGES_PATH = "/images";
  public static final int IMAGES_NUM_IN_QUADRANT = 10;

  // pilot
  public static final double PILOT_DIRECTION_EPSILON = Math.PI / 32;
  public static final double PILOT_ROBOT_TARGET_DIRECTION_EPSILON = MapUtils.PI_OVER_TWO;
  public static final double PILOT_SHOT_EVASION_THRESHOLD = Math.PI / 16;

  // play displayer
  public static final int PLAY_SIGHT_RADIUS = 3;

  // power up
  public static final double POWERUP_RADIUS = 0.1;
  public static final double POWERUP_MOVE_SPEED_DECELERATION = 0.5;
  public static final double POWERUP_MAX_SPEED = 2.0;
  public static final int POWERUP_SHIELD_AMOUNT = 18;

  // Pyro additional constants
  public static final double PYRO_OUTER_CANNON_OFFSET = 0.8;
  public static final double PYRO_CANNON_FORWARD_OFFSET = 0.2;
  public static final double PYRO_MISSILE_OFFSET = 0.2;
  public static final int PYRO_MAX_SHIELDS = 200;
  public static final int PYRO_STARTING_ENERGY = 100;
  public static final int PYRO_MAX_ENERGY = 200;

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
  public static final long RUNNER_PLAY_MIN_SLEEP = 30;
  public static final long RUNNER_PLAY_MAX_SLEEP = 11 * RUNNER_PLAY_MIN_SLEEP / 10;
  public static final long RUNNER_PAUSE_AFTER_PLAY_SLEEP = 1000;

  // shot
  public static final double SHOT_EXPLOSION_RADIUS_DIVISOR = 30.0;
  public static final double SHOT_EXPLOSION_TIME_DIVISOR = 3.0;

  // unit
  public static final double UNIT_EXPLOSION_RADIUS_MULTIPLIER = 1.1;
  public static final double UNIT_EXPLOSION_MAX_TIME = 1.0;

  // objects
  public static Double getRadius(ObjectType type) {
    return RADII.get(type);
  }

  public static Double getMaxMoveSpeed(ObjectType type) {
    return MAX_MOVE_SPEEDS.get(type);
  }

  public static Double getMaxTurnSpeed(ObjectType type) {
    return MAX_TURN_SPEEDS.get(type);
  }

  public static Double getReloadTime(ObjectType type) {
    return RELOAD_TIMES.get(type);
  }

  public static Integer getShotsPerVolley(ObjectType type) {
    return SHOTS_PER_VOLLEYS.get(type);
  }

  public static Double getVolleyReloadTime(ObjectType type) {
    return VOLLEY_RELOAD_TIMES.get(type);
  }

  public static Double getCannonOffset(ObjectType type) {
    return CANNON_OFFSETS.get(type);
  }

  public static Integer getStartingShields(ObjectType type) {
    return STARTING_SHIELDS.get(type);
  }

  public static Integer getDamage(ObjectType type) {
    return DAMAGES.get(type);
  }

  private static final HashMap<ObjectType, Double> RADII = getRadii();
  private static final HashMap<ObjectType, Double> MAX_MOVE_SPEEDS = getMaxMoveSpeeds();
  private static final HashMap<ObjectType, Double> MAX_TURN_SPEEDS = getMaxTurnSpeeds();
  private static final HashMap<ObjectType, Double> RELOAD_TIMES = getReloadTimes();
  private static final HashMap<ObjectType, Integer> SHOTS_PER_VOLLEYS = getShotsPerVolleys();
  private static final HashMap<ObjectType, Double> VOLLEY_RELOAD_TIMES = getVolleyReloadTimes();
  private static final HashMap<ObjectType, Double> CANNON_OFFSETS = getCannonOffsets();
  private static final HashMap<ObjectType, Integer> STARTING_SHIELDS = getStartingShields();
  private static final HashMap<ObjectType, Integer> DAMAGES = getDamages();

  private static HashMap<ObjectType, Double> getRadii() {
    HashMap<ObjectType, Double> radii = new HashMap<ObjectType, Double>();
    radii.put(ObjectType.Pyro, 0.25);
    radii.put(ObjectType.LaserShot, 0.1);
    radii.put(ObjectType.Class2Drone, 0.2);
    return radii;
  }

  private static HashMap<ObjectType, Double> getMaxMoveSpeeds() {
    HashMap<ObjectType, Double> speeds = new HashMap<ObjectType, Double>();
    speeds.put(ObjectType.Pyro, 1.0);
    speeds.put(ObjectType.LaserShot, 3.0);
    speeds.put(ObjectType.Class2Drone, 0.5);
    return speeds;
  }

  private static HashMap<ObjectType, Double> getMaxTurnSpeeds() {
    HashMap<ObjectType, Double> speeds = new HashMap<ObjectType, Double>();
    speeds.put(ObjectType.Pyro, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.Class2Drone, MapUtils.PI_OVER_TWO);
    return speeds;
  }

  private static HashMap<ObjectType, Double> getReloadTimes() {
    HashMap<ObjectType, Double> times = new HashMap<ObjectType, Double>();
    times.put(ObjectType.Pyro, 0.25);
    times.put(ObjectType.Class2Drone, 2.0);
    return times;
  }

  private static HashMap<ObjectType, Integer> getShotsPerVolleys() {
    HashMap<ObjectType, Integer> shots = new HashMap<ObjectType, Integer>();
    shots.put(ObjectType.Class2Drone, 2);
    return shots;
  }

  private static HashMap<ObjectType, Double> getVolleyReloadTimes() {
    HashMap<ObjectType, Double> times = new HashMap<ObjectType, Double>();
    times.put(ObjectType.Class2Drone, 0.2);
    return times;
  }

  private static HashMap<ObjectType, Double> getCannonOffsets() {
    HashMap<ObjectType, Double> offsets = new HashMap<ObjectType, Double>();
    offsets.put(ObjectType.Pyro, 0.525);
    offsets.put(ObjectType.Class2Drone, 0.0);
    return offsets;
  }

  private static HashMap<ObjectType, Integer> getStartingShields() {
    HashMap<ObjectType, Integer> shields = new HashMap<ObjectType, Integer>();
    shields.put(ObjectType.Pyro, 100);
    shields.put(ObjectType.Class2Drone, 11);
    return shields;
  }

  private static HashMap<ObjectType, Integer> getDamages() {
    HashMap<ObjectType, Integer> damages = new HashMap<ObjectType, Integer>();
    damages.put(ObjectType.LaserShot, 3);
    return damages;
  }

  protected Constants() {

  }
}
