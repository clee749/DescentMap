package component;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.LinkedList;

import javax.swing.JFrame;

import mapobject.unit.Pyro;
import structure.DescentMap;

import common.DescentMapException;

enum RunnerState {
  BUILD_MAP,
  PAUSE_AFTER_BUILD,
  PAUSE_BEFORE_PLAY,
  PLAY_MAP,
  PAUSE_AFTER_PLAY,
  COMPLETE;
}


public class MapRunner {
  public static final int MAX_ROOM_SIZE = 10;
  public static final int MAX_NUM_ROOMS = 10;
  public static final int NUM_LEVELS = 5;
  public static final int NUM_PYROS = 3;
  public static final long BUILD_SLEEP = 100;
  public static final long PAUSE_AFTER_BUILD_SLEEP = 1000;
  public static final long PAUSE_BEFORE_PLAY_SLEEP = 1000;
  public static final long PLAY_MIN_SLEEP = 50;
  public static final long PLAY_MAX_SLEEP = 11 * PLAY_MIN_SLEEP / 10;
  public static final long PAUSE_AFTER_PLAY_SLEEP = 1000;

  private final MapDisplayer displayer;
  private final LinkedList<Pyro> pyros;
  private final MapEngine engine;
  private DescentMap map;
  private RunnerState state;
  private long target_sleep_ms;
  private long last_update_time;
  private int num_build_steps;
  private boolean is_paused;

  public MapRunner(MapDisplayer displayer) {
    this.displayer = displayer;
    displayer.setRunner(this);
    engine = new MapEngine();
    displayer.setEngine(engine);
    pyros = new LinkedList<Pyro>();
  }

  public DescentMap getMap() {
    return map;
  }

  public RunnerState getState() {
    return state;
  }

  public void setPaused(boolean is_paused) {
    this.is_paused = is_paused;
  }

  public void newLevel() {
    map = new DescentMap(MAX_ROOM_SIZE);
    displayer.setNewMap(map);
    state = RunnerState.BUILD_MAP;
    target_sleep_ms = BUILD_SLEEP;
    last_update_time = 0L;
    num_build_steps = 0;
  }

  public void sleepAfterStep() throws InterruptedException {
    Thread.sleep(target_sleep_ms);
  }

  public boolean doNextStep() {
    if (is_paused) {
      return false;
    }
    long ms_elapsed = System.currentTimeMillis() - last_update_time;
    if (ms_elapsed < target_sleep_ms) {
      return false;
    }
    switch (state) {
      case BUILD_MAP:
        doBuildStep();
        break;
      case PAUSE_AFTER_BUILD:
        doPauseAfterBuildStep();
        break;
      case PAUSE_BEFORE_PLAY:
        doPauseBeforePlayStep();
        break;
      case PLAY_MAP:
        doPlayStep(Math.min(ms_elapsed, PLAY_MAX_SLEEP) / 1000.0);
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
    if (num_build_steps < MAX_NUM_ROOMS) {
      map.addRoom();
      ++num_build_steps;
    }
    else {
      map.finishBuildingMap();
      MapPopulator.populateMap(map);
      engine.newMap(map);
      if (pyros.isEmpty()) {
        for (int count = 0; count < NUM_PYROS; ++count) {
          engine.spawnPyro(count == 0 ? true : false);
        }
      }
      else {
        for (int count = 0; count < pyros.size(); ++count) {
          engine.spawnPyro(pyros.get(count), (count == 0 ? true : false));
        }
      }
      state = RunnerState.PAUSE_AFTER_BUILD;
      target_sleep_ms = PAUSE_AFTER_BUILD_SLEEP;
    }
  }

  public void doPauseAfterBuildStep() {
    displayer.finishBuildingMap();
    state = RunnerState.PAUSE_BEFORE_PLAY;
    target_sleep_ms = PAUSE_BEFORE_PLAY_SLEEP;
  }

  public void doPauseBeforePlayStep() {
    state = RunnerState.PLAY_MAP;
    target_sleep_ms = PLAY_MIN_SLEEP;
  }

  public void doPlayStep(double s_elapsed) {
    engine.planNextStep(s_elapsed);
    engine.doNextStep(s_elapsed);
    if (engine.levelComplete()) {
      state = RunnerState.PAUSE_AFTER_PLAY;
      target_sleep_ms = PAUSE_AFTER_PLAY_SLEEP;
    }
  }

  public void doPauseAfterPlayStep() {
    state = RunnerState.COMPLETE;
    if (pyros.isEmpty()) {
      for (Pyro pyro : engine.getCreatedPyros()) {
        pyros.add(pyro);
      }
    }
  }

  public static void main(String[] args) throws InterruptedException {
    JFrame frame = new JFrame();
    MapPanel panel = new MapPanel();
    MapRunner runner = new MapRunner(panel);
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setMinimumSize(new Dimension(100, 100));
    frame.setSize(screen.width, screen.height - 50);
    frame.setTitle("MAP");
    frame.setVisible(true);
    frame.add(panel);
    frame.addComponentListener(panel);
    frame.addKeyListener(panel);

    for (int level = 1; level <= NUM_LEVELS; ++level) {
      System.out.print(String.format("Mine MN%04d: ", panel.playMusic()));
      runner.newLevel();
      do {
        if (runner.doNextStep()) {
          panel.repaint();
        }
        if (runner.getState().equals(RunnerState.PAUSE_AFTER_PLAY)) {
          System.out.println(String.format("Level %d complete!", level));
          panel.stopMusic();
        }
        runner.sleepAfterStep();
      } while (!runner.getState().equals(RunnerState.COMPLETE));
    }
    panel.closeMusic();
  }
}
