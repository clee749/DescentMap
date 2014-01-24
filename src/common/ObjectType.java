package common;

public enum ObjectType {
  // Descent ship
  Pyro,

  // sceneries
  Barrier,
  EnergyCenter,
  RobotGenerator,
  Entrance,
  Exit,

  // robots
  GreenRobot,
  YellowRobot,
  RedRobot,
  BabySpider,
  Class1Drone,
  Class2Drone,
  DefenseRobot,
  LightHulk,
  MediumHulk,
  PlatformLaser,
  SecondaryLifter,
  Spider,
  Bomber,
  HeavyDriller,
  HeavyHulk,
  MediumHulkCloaked,
  PlatformMissile,

  // shots
  LaserShot,
  PlasmaShot,
  FusionShot,
  FireShot,
  ConcussionMissile,
  HomingMissile,
  SmartMissile,
  SmartPlasma,

  // power ups
  Shield,
  Energy,
  QuadLasers,
  Cloak,
  Invulnerability,
  ConcussionMissilePowerup,
  ConcussionPack,
  HomingMissilePowerup,
  HomingPack,
  ProximityPack,
  SmartMissilePowerup,
  LaserCannon,
  PlasmaCannon,
  FusionCannon,
  FireCannon,
  ConcussionCannon,
  HomingCannon,

  // ephemerals
  Explosion,
  Zunggg,

  // miscellaneous
  ProximityBomb,
  MultipleObject;

  public static ObjectType[] getSceneries() {
    ObjectType[] scenery = {Barrier, EnergyCenter, RobotGenerator, Entrance, Exit};
    return scenery;
  }

  public static ObjectType[] getRobots() {
    ObjectType[] robots =
            {GreenRobot, YellowRobot, RedRobot, BabySpider, Class1Drone, Class2Drone, DefenseRobot,
                    LightHulk, MediumHulk, PlatformLaser, SecondaryLifter, Spider, Bomber, HeavyDriller,
                    HeavyHulk, MediumHulkCloaked, PlatformMissile, ProximityBomb};
    return robots;
  }

  public static ObjectType[] getShots() {
    ObjectType[] weapons =
            {LaserShot, PlasmaShot, FusionShot, FireShot, ConcussionMissile, HomingMissile, SmartMissile,
                    SmartPlasma};
    return weapons;
  }

  public static ObjectType[] getPowerups() {
    ObjectType[] powerups =
            {Shield, Energy, QuadLasers, Cloak, Invulnerability, ConcussionMissilePowerup, ConcussionPack,
                    HomingMissilePowerup, HomingPack, ProximityPack, SmartMissilePowerup, LaserCannon,
                    PlasmaCannon, FusionCannon, FireCannon, ConcussionCannon, HomingCannon};
    return powerups;
  }
}
