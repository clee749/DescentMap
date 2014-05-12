package common;

public enum ObjectType {
  // Descent ship
  Pyro,

  // sceneries
  Barrier,
  EnergyCenter,
  Entrance,
  Exit,
  RobotGenerator,

  // robots
  GreenRobot,
  YellowRobot,
  RedRobot,
  AdvancedLifter,
  BabySpider,
  Bomber,
  Class1Drone,
  Class2Drone,
  DefenseRobot,
  Gopher,
  HeavyDriller,
  HeavyHulk,
  LightHulk,
  MediumHulk,
  MediumHulkCloaked,
  MediumLifter,
  MiniBoss,
  PlatformLaser,
  PlatformMissile,
  SecondaryLifter,
  Spider,

  // shots
  ConcussionMissile,
  FireballShot,
  FusionShot,
  HomingMissile,
  LaserShot,
  MegaMissile,
  PlasmaShot,
  SmartMissile,
  SmartPlasma,
  SpreadfireShot,

  // power ups
  Cloak,
  ConcussionMissilePowerup,
  ConcussionPack,
  Energy,
  FusionCannonPowerup,
  HomingMissilePowerup,
  HomingPack,
  Invulnerability,
  LaserCannonPowerup,
  MegaMissilePowerup,
  PlasmaCannonPowerup,
  ProximityPack,
  QuadLasers,
  Shield,
  SmartMissilePowerup,
  SpreadfireCannonPowerup,

  // ephemerals
  EnergySpark,
  Explosion,
  Zunggg,

  // miscellaneous
  MultipleObject,
  ProximityBomb;

  public static final ObjectType[] SCENERIES = {Barrier, EnergyCenter, Entrance, Exit, RobotGenerator};

  public static final ObjectType[] ROBOTS =
          {AdvancedLifter, BabySpider, Bomber, Class1Drone, Class2Drone, DefenseRobot, Gopher, HeavyDriller,
                  HeavyHulk, LightHulk, MediumHulk, MediumHulkCloaked, MediumLifter, MiniBoss, PlatformLaser,
                  PlatformMissile, SecondaryLifter, Spider};

  public static final ObjectType[] SHOTS =
          {ConcussionMissile, FireballShot, FusionShot, HomingMissile, LaserShot, MegaMissile, PlasmaShot,
                  SmartMissile, SmartPlasma, SpreadfireShot};

  public static final ObjectType[] POWERUPS =
          {Cloak, ConcussionMissilePowerup, ConcussionPack, Energy, FusionCannonPowerup,
                  HomingMissilePowerup, HomingPack, Invulnerability, LaserCannonPowerup, MegaMissilePowerup,
                  PlasmaCannonPowerup, ProximityPack, QuadLasers, Shield, SmartMissilePowerup,
                  SpreadfireCannonPowerup};
}
