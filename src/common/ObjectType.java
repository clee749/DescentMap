package common;

public enum ObjectType {
  // Descent ship
  Pyro,

  // scenery
  Barrier, EnergyCenter, RobotGenerator, Entrance, Exit,

  // robots
  GreenRobot, YellowRobot, RedRobot, BabySpider, Class1Drone, Class2Drone, DefenseRobot, LightHulk, MediumHulk, PlatformLaser, SecondaryLifter, Spider, Bomber, HeavyDriller, HeavyHulk, MediumHulkCloaked, PlatformMissile,

  // weapons
  LaserShot, PlasmaShot, FusionShot, FireShot, ConcussionMissile, HomingMissile, SmartMissile, SmartPlasma,

  // power ups
  Shield, Energy, QuadLasers, Cloak, Invulnerability, ConcussionMissilePowerup, ConcussionPack, HomingMissilePowerup, HomingPack, ProximityPack, SmartMissilePowerup, LaserCannon, PlasmaCannon, FusionCannon, FireCannon, ConcussionCannon, HomingCannon,

  // transients
  Explosion, Zunggg, SpawningUnit,

  // miscellaneous
  ProximityBomb, MultipleObject;

  public static ObjectType[] getScenery() {
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

  public static ObjectType[] getWeapons() {
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
