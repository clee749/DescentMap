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
  FireballShot,
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
  LaserCannonPowerup,
  PlasmaCannonPowerup,
  FusionCannonPowerup,

  // ephemerals
  Explosion,
  Zunggg,

  // miscellaneous
  ProximityBomb,
  MultipleObject;

  public static final ObjectType[] SCENERIES = {Barrier, EnergyCenter, RobotGenerator, Entrance, Exit};

  public static final ObjectType[] ROBOTS = {GreenRobot, YellowRobot, RedRobot, BabySpider, Class1Drone,
          Class2Drone, DefenseRobot, LightHulk, MediumHulk, PlatformLaser, SecondaryLifter, Spider, Bomber,
          HeavyDriller, HeavyHulk, MediumHulkCloaked, PlatformMissile, ProximityBomb};

  public static final ObjectType[] SHOTS = {LaserShot, PlasmaShot, FusionShot, FireballShot,
          ConcussionMissile, HomingMissile, SmartMissile, SmartPlasma};

  public static final ObjectType[] POWERUPS = {Shield, Energy, QuadLasers, Cloak, Invulnerability,
          ConcussionMissilePowerup, ConcussionPack, HomingMissilePowerup, HomingPack, ProximityPack,
          SmartMissilePowerup, LaserCannonPowerup, PlasmaCannonPowerup, FusionCannonPowerup};
}
