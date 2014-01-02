package component;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JFrame;

import mapobject.unit.pyro.Pyro;
import structure.DescentMap;
import structure.Room;

import common.Constants;
import common.DescentMapException;

enum RunnerState {
  BUILD_MAP, PAUSE_AFTER_BUILD, PLAY_MAP, PAUSE_AFTER_PLAY, COMPLETE;
}


public class MapRunner {
  private final DescentMap map;
  private MapEngine engine;
  private RunnerState state;
  private long target_sleep_ms;
  private long last_update_time;
  private int num_build_steps;

  public MapRunner() {
    map = new DescentMap(Constants.BUILDER_MAX_ROOM_SIZE);
    state = RunnerState.BUILD_MAP;
    target_sleep_ms = Constants.RUNNER_BUILD_SLEEP;
    last_update_time = 0L;
    num_build_steps = 0;
  }

  public DescentMap getMap() {
    return map;
  }

  public RunnerState getState() {
    return state;
  }

  public boolean doNextStep() {
    long elapsed_time = System.currentTimeMillis() - last_update_time;
    if (elapsed_time < target_sleep_ms) {
      return false;
    }
    switch (state) {
      case BUILD_MAP:
        doBuildStep();
        break;
      case PAUSE_AFTER_BUILD:
        doPauseAfterBuildStep();
        break;
      case PLAY_MAP:
        doPlayStep(Math.min(elapsed_time, Constants.RUNNER_PLAY_MAX_SLEEP));
        break;
      case PAUSE_AFTER_PLAY:
        doPauseAfterPlayStep();
        break;
      default:
        throw new DescentMapException("Unexpected RunnerState: " + state);
    }
    last_update_time = System.currentTimeMillis();
    return true;
  }

  public void doBuildStep() {
    if (num_build_steps < Constants.BUILDER_MAX_NUM_ROOMS) {
      map.addRoom();
      ++num_build_steps;
    }
    else {
      map.finishBuildingMap();
      Room entrance_room = map.getEntranceRoom();
      Point nw_corner = entrance_room.getNWCorner();
      Point se_corner = entrance_room.getSECorner();
      Pyro ship =
              new Pyro(entrance_room, (nw_corner.x + se_corner.x) / 2.0, (nw_corner.y + se_corner.y) / 2.0);
      map.insertPyro(ship);
      state = RunnerState.PAUSE_AFTER_BUILD;
      target_sleep_ms = Constants.RUNNER_PAUSE_AFTER_BUILD_SLEEP;
      engine = new MapEngine(map);
      engine.setCenterShip(ship);
    }
  }

  public void doPauseAfterBuildStep() {
    state = RunnerState.PLAY_MAP;
    target_sleep_ms = Constants.RUNNER_PLAY_MIN_SLEEP;
  }

  public void doPlayStep(long ms_elapsed) {
    engine.computeNextStep();
    engine.doNextStep(ms_elapsed);
    if (engine.levelComplete()) {
      state = RunnerState.PAUSE_AFTER_PLAY;
      target_sleep_ms = Constants.RUNNER_PAUSE_AFTER_PLAY_SLEEP;
    }
  }

  public void doPauseAfterPlayStep() {
    state = RunnerState.COMPLETE;
  }

  public static void main(String[] args) throws InterruptedException {
    JFrame frame = new JFrame();
    MapPanel panel = new MapPanel();
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setSize(screen.width, screen.height - 50);
    frame.setTitle("MAP");
    frame.add(panel);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setMinimumSize(new Dimension(100, 100));
    frame.setVisible(true);

    for (int level = 1; level <= Constants.RUNNER_NUM_LEVELS; ++level) {
      System.out.println("Level " + level);
      MapRunner runner = new MapRunner();
      panel.setMap(runner.getMap());
      do {
        if (runner.doNextStep()) {
          panel.repaint();
        }
        Thread.sleep(100);
      } while (!runner.getState().equals(RunnerState.COMPLETE));
      System.out.println("DONE");
    }
  }
}
