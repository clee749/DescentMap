package mapobject.scenery;

import mapobject.MapObject;
import mapobject.ephemeral.EnergySpark;
import mapobject.powerup.Powerup;
import mapobject.unit.Pyro;
import structure.Room;

import common.ObjectType;
import component.MapEngine;

public class EnergyCenter extends Scenery {
  public static final double SECONDS_BETWEEN_SPARKS = Powerup.SECONDS_PER_FRAME;
  public static final double SPARK_PLACEMENT_RADIUS = 0.4;
  public static final double SPARK_PLACEMENT_RANGE = SPARK_PLACEMENT_RADIUS * 2;

  private double time_to_next_spark;

  public EnergyCenter(Room room, double x_loc, double y_loc) {
    super(room, x_loc, y_loc);
  }

  @Override
  public ObjectType getType() {
    return ObjectType.EnergyCenter;
  }

  @Override
  public void planNextAction(double s_elapsed) {

  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    for (Pyro pyro : room.getPyros()) {
      if (pyro.getShields() >= 0 && Math.abs(x_loc - pyro.getX()) < radius &&
              Math.abs(y_loc - pyro.getY()) < radius) {
        pyro.rechargeEnergy();
      }
    }

    time_to_next_spark -= s_elapsed;
    if (time_to_next_spark < 0.0) {
      time_to_next_spark = SECONDS_BETWEEN_SPARKS;
      return new EnergySpark(room, x_loc + Math.random() * SPARK_PLACEMENT_RANGE - SPARK_PLACEMENT_RADIUS,
              y_loc + Math.random() * SPARK_PLACEMENT_RANGE - SPARK_PLACEMENT_RADIUS);
    }
    return null;
  }
}
