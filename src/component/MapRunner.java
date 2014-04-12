package component;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.LinkedList;

import javax.swing.JFrame;

import mapobject.unit.Pyro;
import structure.DescentMap;

import common.DescentMapException;
import common.ObjectType;
import component.builder.BossChambersBuilder;
import component.builder.GauntletBuilder;
import component.builder.MapBuilder;
import component.builder.MazeBuilder;
import component.builder.StandardBuilder;
import component.populator.BossChambersPopulator;
import component.populator.GauntletPopulator;
import component.populator.MapPopulator;
import component.populator.MazePopulator;
import component.populator.StandardPopulator;

enum RunnerState {
  BUILD_MAP,
  PAUSE_AFTER_BUILD,
  PAUSE_BEFORE_PLAY,
  PLAY_MAP,
  PAUSE_AFTER_PLAY,
  COMPLETE;
}


enum MapType {
  STANDARD,
  MAZE,
  GAUNTLET,
  BOSS_CHAMBERS;

  public static final int MAX_ROOM_SIZE = 10;
  public static final int STANDARD_NUM_ROOMS = 10;
  public static final int MAZE_MIN_TOTAL_ROOM_AREA = 100 * MazeBuilder.HALLWAY_WIDTH;
  public static final int GAUNTLET_NUM_GENERATOR_ROOMS = (int) Math.ceil(ObjectType.ROBOTS.length /
          (MAX_ROOM_SIZE / 3.0 * 2));
  public static final int BOSS_CHAMBERS_WIDTH = 2;
  public static final int BOSS_CHAMBERS_HEIGHT = 2;

  public static MapBuilder getBuilder(MapType type) {
    switch (type) {
      case STANDARD:
        return new StandardBuilder(MAX_ROOM_SIZE, STANDARD_NUM_ROOMS);
      case MAZE:
        return new MazeBuilder(MAX_ROOM_SIZE, MAZE_MIN_TOTAL_ROOM_AREA);
      case GAUNTLET:
        return new GauntletBuilder(MAX_ROOM_SIZE, GAUNTLET_NUM_GENERATOR_ROOMS);
      case BOSS_CHAMBERS:
        return new BossChambersBuilder(MAX_ROOM_SIZE, BOSS_CHAMBERS_WIDTH, BOSS_CHAMBERS_HEIGHT);
      default:
        throw new DescentMapException("Unexpected MapType: " + type);
    }
  }

  public static MapPopulator getPopulator(MapType type, DescentMap map) {
    switch (type) {
      case STANDARD:
        return new StandardPopulator(map);
      case MAZE:
        return new MazePopulator(map);
      case GAUNTLET:
        return new GauntletPopulator(map);
      case BOSS_CHAMBERS:
        return new BossChambersPopulator(map);
      default:
        throw new DescentMapException("Unexpected MapType: " + type);
    }
  }
}


public class MapRunner {
  public static final int NUM_LEVELS = 5;
  public static final int NUM_PYROS = 3;
  public static final double STANDARD_MAP_PROB = 0.5;
  public static final double EACH_NON_STANDARD_MAP_PROB = 1.0 / (MapType.values().length - 1);
  public static final long BUILD_SLEEP = 100;
  public static final long PAUSE_AFTER_BUILD_SLEEP = 1000;
  public static final long PAUSE_BEFORE_PLAY_SLEEP = 1000;
  public static final long PLAY_MIN_SLEEP = 50;
  public static final long PLAY_MAX_SLEEP = 11 * PLAY_MIN_SLEEP / 10;
  public static final long PAUSE_AFTER_PLAY_SLEEP = 1000;

  private final LinkedList<Pyro> pyros;
  private final MapDisplayer displayer;
  private final MapEngine engine;
  private MapType map_type;
  private DescentMap map;
  private RunnerState state;
  private long target_sleep_ms;
  private long last_update_time;
  private boolean is_paused;

  public MapRunner() {
    pyros = new LinkedList<Pyro>();
    displayer = new MapDisplayer(this);
    engine = new MapEngine();
  }

  public MapDisplayer getDisplayer() {
    return displayer;
  }

  public MapEngine getEngine() {
    return engine;
  }

  public MapType getMapType() {
    return map_type;
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
    if (Math.random() < STANDARD_MAP_PROB) {
      map_type = MapType.STANDARD;
    }
    else {
      map_type = null;
      double rand = Math.random() - EACH_NON_STANDARD_MAP_PROB;
      MapType[] types = MapType.values();
      for (int index = 1; index < types.length; ++index, rand -= EACH_NON_STANDARD_MAP_PROB) {
        if (rand < 0.0) {
          map_type = types[index];
          break;
        }
      }
      // for double precision errors
      if (map_type == null) {
        map_type = types[types.length - 1];
      }
    }
    map = new DescentMap(MapType.getBuilder(map_type));
    displayer.newMap();
    state = RunnerState.BUILD_MAP;
    target_sleep_ms = BUILD_SLEEP;
    last_update_time = 0L;
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
    if (!map.isReadyToFinishBuilding()) {
      map.addRoom();
    }
    else {
      map.finishBuildingMap();
      MapType.getPopulator(map_type, map).populateMap();
      engine.newMap(map);
      int pyro_count = 0;
      while (pyro_count < pyros.size()) {
        engine.spawnPyro(pyros.get(pyro_count), (pyro_count == 0 ? true : false));
        ++pyro_count;
      }
      while (pyro_count < NUM_PYROS) {
        engine.spawnPyro(pyro_count == 0 ? true : false);
        ++pyro_count;
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
    if (pyros.size() < NUM_PYROS) {
      for (Pyro pyro : engine.getCreatedPyros()) {
        pyros.add(pyro);
      }
    }
  }

  public static void main(String[] args) throws InterruptedException {
    MapRunner runner = new MapRunner();
    JFrame frame = new JFrame();
    MapPanel panel = new MapPanel(runner);
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
      runner.newLevel();
      System.out.print(String.format("Mine %s%04d: ", runner.getMapType(), panel.playMusic()));
      do {
        if (runner.doNextStep()) {
          panel.repaint();
          if (runner.getState().equals(RunnerState.PAUSE_AFTER_PLAY)) {
            System.out.println(String.format("Level %d complete!", level));
            panel.stopMusic();
          }
        }
        runner.sleepAfterStep();
      } while (!runner.getState().equals(RunnerState.COMPLETE));
    }
    panel.closeMusic();
  }
}
