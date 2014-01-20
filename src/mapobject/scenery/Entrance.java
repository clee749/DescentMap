package mapobject.scenery;

import java.util.LinkedList;

import mapobject.MapObject;
import mapobject.ephemeral.Zunggg;
import mapobject.unit.pyro.Pyro;
import structure.Room;

import common.DescentMapException;
import common.ObjectType;
import component.MapEngine;

enum EntranceState {
  INACTIVE,
  SPAWN,
  COOLDOWN;
}


public class Entrance extends Scenery {
  public static final double COOLDOWN_TIME = 5.0;
  public static final double TIME_TO_SPAWN = 0.25;
  public static final double ZUNGGG_TIME = TIME_TO_SPAWN * 2;

  private final LinkedList<Pyro> spawn_queue;
  private EntranceState state;
  private double state_time_left;

  public Entrance(Room room, double x_loc, double y_loc) {
    super(room, x_loc, y_loc);
    spawn_queue = new LinkedList<Pyro>();
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
    switch (state) {
      case INACTIVE:
        if (!spawn_queue.isEmpty()) {
          state = EntranceState.SPAWN;
          state_time_left = TIME_TO_SPAWN;
          return new Zunggg(room, x_loc, y_loc, ZUNGGG_TIME);
        }
        break;
      case SPAWN:
        if (state_time_left < 0.0) {
          state = EntranceState.COOLDOWN;
          state_time_left = COOLDOWN_TIME;
          return spawn_queue.pop();
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

  public void spawnPyro(Pyro pyro) {
    spawn_queue.push(pyro);
  }
}
