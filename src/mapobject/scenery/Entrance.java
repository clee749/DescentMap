package mapobject.scenery;

import java.util.Iterator;
import java.util.LinkedList;

import mapobject.MapObject;
import mapobject.ephemeral.Zunggg;
import mapobject.unit.Pyro;
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
  public final Pyro pyro;
  public final boolean is_center_object;

  public SpawningPyro(Pyro pyro, boolean is_center_object) {
    this.pyro = pyro;
    this.is_center_object = is_center_object;
  }
}


public class Entrance extends Scenery {
  public static final double COOLDOWN_TIME = 5.0;
  public static final double TIME_TO_SPAWN = 0.25;
  public static final double ZUNGGG_TIME = 1.0;

  private final double spawn_direction;
  private final LinkedList<SpawningPyro> spawn_queue;
  private SpawningPyro current_spawning_pyro;
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
    for (SpawningPyro spawning_pyro : spawn_queue) {
      if (spawning_pyro.pyro != null) {
        spawning_pyro.pyro.handleRespawnDelay(s_elapsed);
      }
    }
    state_time_left -= s_elapsed;
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    switch (state) {
      case INACTIVE:
        for (Iterator<SpawningPyro> it = spawn_queue.iterator(); it.hasNext();) {
          SpawningPyro spawning_pyro = it.next();
          if (spawning_pyro.pyro == null || spawning_pyro.pyro.isReadyToRespawn()) {
            current_spawning_pyro = spawning_pyro;
            it.remove();
            state = EntranceState.SPAWN;
            state_time_left = TIME_TO_SPAWN;
            if (spawning_pyro.is_center_object) {
              engine.setCenterObject(this);
            }
            playSound(engine, "effects/mtrl01.wav");
            return new Zunggg(room, x_loc, y_loc, ZUNGGG_TIME);
          }
        }
        break;
      case SPAWN:
        if (state_time_left < 0.0) {
          state = EntranceState.COOLDOWN;
          state_time_left = COOLDOWN_TIME;
          Pyro pyro = current_spawning_pyro.pyro;
          if (pyro == null) {
            pyro = new Pyro(room, x_loc, y_loc, spawn_direction);
            engine.addCreatedPyro(pyro);
          }
          pyro.spawn(room, x_loc, y_loc, spawn_direction);
          if (current_spawning_pyro.is_center_object) {
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
    return null;
  }

  public void spawnPyro(Pyro pyro, boolean is_center_object) {
    spawn_queue.add(new SpawningPyro(pyro, is_center_object));
  }
}
