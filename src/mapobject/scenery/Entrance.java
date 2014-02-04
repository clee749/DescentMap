package mapobject.scenery;

import java.util.LinkedList;

import mapobject.MapObject;
import mapobject.ephemeral.Zunggg;
import mapobject.unit.Pyro;
import pilot.PyroPilot;
import structure.Room;

import common.DescentMapException;
import common.ObjectType;
import common.RoomSide;
import component.MapEngine;

enum EntranceState {
  INACTIVE,
  SPAWN,
  COOLDOWN;
}


class SpawningPyro {
  public final PyroPilot pilot;
  public final boolean is_center_object;

  public SpawningPyro(PyroPilot pilot, boolean is_center_object) {
    this.pilot = pilot;
    this.is_center_object = is_center_object;
  }
}


public class Entrance extends Scenery {
  public static final double COOLDOWN_TIME = 5.0;
  public static final double TIME_TO_SPAWN = 0.25;
  public static final double ZUNGGG_TIME = 1.0;

  private final double spawn_direction;
  private final LinkedList<SpawningPyro> spawn_queue;
  private EntranceState state;
  private double state_time_left;

  public Entrance(Room room, double x_loc, double y_loc, RoomSide spawn_direction) {
    super(room, x_loc, y_loc);
    this.spawn_direction = RoomSide.directionToRadians(spawn_direction);
    spawn_queue = new LinkedList<SpawningPyro>();
    state = EntranceState.INACTIVE;
  }

  @Override
  public ObjectType getType() {
    return ObjectType.Entrance;
  }

  @Override
  public void planNextAction(double s_elapsed) {

  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    for (SpawningPyro spawning_pyro : spawn_queue) {
      spawning_pyro.pilot.handleRespawnDelay(s_elapsed);
    }

    switch (state) {
      case INACTIVE:
        if (!spawn_queue.isEmpty() && spawn_queue.peek().pilot.isReadyToRespawn()) {
          state = EntranceState.SPAWN;
          state_time_left = TIME_TO_SPAWN;
          if (spawn_queue.peek().is_center_object) {
            engine.setCenterObject(this);
          }
          return new Zunggg(room, x_loc, y_loc, ZUNGGG_TIME);
        }
        break;
      case SPAWN:
        if (state_time_left < 0.0) {
          state = EntranceState.COOLDOWN;
          state_time_left = COOLDOWN_TIME;
          SpawningPyro spawning_pyro = spawn_queue.pop();
          Pyro pyro = new Pyro(spawning_pyro.pilot, room, x_loc, y_loc, spawn_direction);
          if (spawning_pyro.is_center_object) {
            engine.setCenterObject(pyro);
          }
          return pyro;
        }
        break;
      case COOLDOWN:
        if (state_time_left < 0.0) {
          state = EntranceState.INACTIVE;
        }
        break;
      default:
        throw new DescentMapException("Unexpected EntranceState: " + state);
    }

    state_time_left -= s_elapsed;
    return null;
  }

  public void spawnPyro(PyroPilot pilot, boolean is_center_object) {
    spawn_queue.add(new SpawningPyro(pilot, is_center_object));
  }
}
