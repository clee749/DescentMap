package common;

import java.util.HashMap;

import util.MapUtils;

public class Constants {
  protected Constants() {

  }

  public static Double getMaxMoveSpeed(ObjectType type) {
    return MAX_MOVE_SPEEDS.get(type);
  }

  public static Double getMaxTurnSpeed(ObjectType type) {
    return MAX_TURN_SPEEDS.get(type);
  }

  public static Double getRadius(ObjectType type) {
    return RADII.get(type);
  }

  public static Double getCannonOffset(ObjectType type) {
    return CANNON_OFFSETS.get(type);
  }

  public static Integer getStartingShields(ObjectType type) {
    return STARTING_SHIELDS.get(type);
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

  public static Integer getDamage(ObjectType type) {
    return DAMAGES.get(type);
  }

  private static final HashMap<ObjectType, Double> MAX_MOVE_SPEEDS = getMaxMoveSpeeds(); // MovableObjects
  private static final HashMap<ObjectType, Double> MAX_TURN_SPEEDS = getMaxTurnSpeeds(); // MovableObjects
  private static final HashMap<ObjectType, Double> RADII = getRadii(); // Units
  private static final HashMap<ObjectType, Double> CANNON_OFFSETS = getCannonOffsets(); // Units
  private static final HashMap<ObjectType, Integer> STARTING_SHIELDS = getStartingShields(); // Units
  private static final HashMap<ObjectType, Double> RELOAD_TIMES = getReloadTimes(); // Robots
  private static final HashMap<ObjectType, Integer> SHOTS_PER_VOLLEYS = getShotsPerVolleys(); // Robots
  private static final HashMap<ObjectType, Double> VOLLEY_RELOAD_TIMES = getVolleyReloadTimes(); // Robots
  private static final HashMap<ObjectType, Integer> DAMAGES = getDamages(); // Shots

  private static HashMap<ObjectType, Double> getMaxMoveSpeeds() {
    HashMap<ObjectType, Double> speeds = new HashMap<ObjectType, Double>();
    speeds.put(ObjectType.PlatformMissile, 0.1);
    speeds.put(ObjectType.MediumHulk, 0.2);
    speeds.put(ObjectType.DefenseRobot, 0.3);
    speeds.put(ObjectType.PlatformLaser, 0.3);
    speeds.put(ObjectType.Spider, 0.3);
    speeds.put(ObjectType.HeavyDriller, 0.4);
    speeds.put(ObjectType.LightHulk, 0.4);
    speeds.put(ObjectType.Class1Drone, 0.5);
    speeds.put(ObjectType.Class2Drone, 0.5);
    speeds.put(ObjectType.SecondaryLifter, 0.5);
    speeds.put(ObjectType.BabySpider, 0.6);
    speeds.put(ObjectType.Pyro, 1.0);
    speeds.put(ObjectType.FireballShot, 1.1);
    speeds.put(ObjectType.ConcussionMissile, 2.5);
    speeds.put(ObjectType.LaserShot, 3.0);
    speeds.put(ObjectType.PlasmaShot, 3.5);
    return speeds;
  }

  private static HashMap<ObjectType, Double> getMaxTurnSpeeds() {
    HashMap<ObjectType, Double> speeds = new HashMap<ObjectType, Double>();
    speeds.put(ObjectType.PlatformMissile, Math.PI / 4);
    speeds.put(ObjectType.Pyro, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.Class1Drone, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.Class2Drone, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.DefenseRobot, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.HeavyDriller, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.LightHulk, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.MediumHulk, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.PlatformLaser, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.Spider, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.BabySpider, Math.PI);
    speeds.put(ObjectType.SecondaryLifter, Math.PI);
    return speeds;
  }

  private static HashMap<ObjectType, Double> getRadii() {
    HashMap<ObjectType, Double> radii = new HashMap<ObjectType, Double>();
    // 1m == 0.05
    radii.put(ObjectType.BabySpider, 0.12);
    radii.put(ObjectType.Class1Drone, 0.15);
    radii.put(ObjectType.Class2Drone, 0.2);
    radii.put(ObjectType.SecondaryLifter, 0.2);
    radii.put(ObjectType.Pyro, 0.25);
    radii.put(ObjectType.LightHulk, 0.25);
    radii.put(ObjectType.PlatformLaser, 0.25);
    radii.put(ObjectType.PlatformMissile, 0.25);
    radii.put(ObjectType.DefenseRobot, 0.3);
    radii.put(ObjectType.MediumHulk, 0.35);
    radii.put(ObjectType.HeavyDriller, 0.4);
    radii.put(ObjectType.Spider, 0.4);
    return radii;
  }

  private static HashMap<ObjectType, Double> getCannonOffsets() {
    HashMap<ObjectType, Double> offsets = new HashMap<ObjectType, Double>();
    offsets.put(ObjectType.BabySpider, 0.0);
    offsets.put(ObjectType.Class2Drone, 0.0);
    offsets.put(ObjectType.PlatformLaser, 0.0);
    offsets.put(ObjectType.PlatformMissile, 0.0);
    offsets.put(ObjectType.Spider, 0.13);
    offsets.put(ObjectType.SecondaryLifter, 0.4);
    offsets.put(ObjectType.Pyro, 0.525);
    offsets.put(ObjectType.LightHulk, 0.79);
    offsets.put(ObjectType.HeavyDriller, 0.81);
    offsets.put(ObjectType.MediumHulk, 0.83);
    offsets.put(ObjectType.DefenseRobot, 0.84);
    offsets.put(ObjectType.Class1Drone, 0.86);
    return offsets;
  }

  private static HashMap<ObjectType, Integer> getStartingShields() {
    HashMap<ObjectType, Integer> shields = new HashMap<ObjectType, Integer>();
    shields.put(ObjectType.BabySpider, 8);
    shields.put(ObjectType.Class1Drone, 8);
    shields.put(ObjectType.Class2Drone, 11);
    shields.put(ObjectType.SecondaryLifter, 20);
    shields.put(ObjectType.DefenseRobot, 23);
    shields.put(ObjectType.LightHulk, 23);
    shields.put(ObjectType.PlatformLaser, 23);
    shields.put(ObjectType.MediumHulk, 32);
    shields.put(ObjectType.Spider, 35);
    shields.put(ObjectType.HeavyDriller, 47);
    shields.put(ObjectType.PlatformMissile, 47);
    shields.put(ObjectType.Pyro, 100);
    return shields;
  }

  private static HashMap<ObjectType, Double> getReloadTimes() {
    HashMap<ObjectType, Double> times = new HashMap<ObjectType, Double>();
    times.put(ObjectType.BabySpider, 2.0);
    times.put(ObjectType.Class1Drone, 2.0);
    times.put(ObjectType.Class2Drone, 2.0);
    times.put(ObjectType.DefenseRobot, 2.0);
    times.put(ObjectType.HeavyDriller, 2.0);
    times.put(ObjectType.LightHulk, 2.0);
    times.put(ObjectType.PlatformLaser, 2.0);
    times.put(ObjectType.SecondaryLifter, 2.0);
    times.put(ObjectType.Spider, 2.0);
    times.put(ObjectType.MediumHulk, 3.0);
    times.put(ObjectType.PlatformMissile, 4.0);
    return times;
  }

  private static HashMap<ObjectType, Integer> getShotsPerVolleys() {
    HashMap<ObjectType, Integer> shots = new HashMap<ObjectType, Integer>();
    shots.put(ObjectType.BabySpider, 2);
    shots.put(ObjectType.Class1Drone, 2);
    shots.put(ObjectType.Class2Drone, 2);
    shots.put(ObjectType.LightHulk, 2);
    shots.put(ObjectType.MediumHulk, 2);
    shots.put(ObjectType.SecondaryLifter, 2);
    shots.put(ObjectType.HeavyDriller, 3);
    shots.put(ObjectType.PlatformMissile, 3);
    shots.put(ObjectType.Spider, 3);
    shots.put(ObjectType.DefenseRobot, 4);
    shots.put(ObjectType.PlatformLaser, 4);
    return shots;
  }

  private static HashMap<ObjectType, Double> getVolleyReloadTimes() {
    HashMap<ObjectType, Double> times = new HashMap<ObjectType, Double>();
    times.put(ObjectType.BabySpider, 0.1);
    times.put(ObjectType.Class1Drone, 0.1);
    times.put(ObjectType.Class2Drone, 0.1);
    times.put(ObjectType.DefenseRobot, 0.1);
    times.put(ObjectType.HeavyDriller, 0.1);
    times.put(ObjectType.LightHulk, 0.1);
    times.put(ObjectType.MediumHulk, 0.1);
    times.put(ObjectType.PlatformLaser, 0.1);
    times.put(ObjectType.PlatformMissile, 0.1);
    times.put(ObjectType.SecondaryLifter, 0.1);
    times.put(ObjectType.Spider, 0.1);
    return times;
  }

  private static HashMap<ObjectType, Integer> getDamages() {
    HashMap<ObjectType, Integer> damages = new HashMap<ObjectType, Integer>();
    damages.put(ObjectType.LaserShot, 3);
    damages.put(ObjectType.FireballShot, 6);
    damages.put(ObjectType.PlasmaShot, 7);
    damages.put(ObjectType.ConcussionMissile, 16);
    return damages;
  }
}
