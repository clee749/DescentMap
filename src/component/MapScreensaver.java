package component;

import java.awt.Graphics;
import java.awt.Graphics2D;

import org.jdesktop.jdic.screensaver.ScreensaverSettings;
import org.jdesktop.jdic.screensaver.SimpleScreensaver;

public class MapScreensaver extends SimpleScreensaver {
  private MapRunner runner;
  private MapDisplayer displayer;

  @Override
  public void init() {
    ScreensaverSettings settings = getContext().getSettings();
    String play_min_sleep_str = settings.getProperty("min-ms-per-frame");
    long play_min_sleep = MapRunner.PLAY_MIN_FRAME_TIME;
    if (play_min_sleep_str != null) {
      try {
        play_min_sleep = Long.parseLong(play_min_sleep_str);
      }
      catch (NumberFormatException nfe) {

      }
    }

    runner = new MapRunner(play_min_sleep);
    displayer = runner.getDisplayer();
    displayer.setSize(getContext().getComponent().getSize());
    runner.newLevel();
  }

  @Override
  public void paint(Graphics g) {
    if (runner.getState().equals(RunnerState.COMPLETE)) {
      runner.newLevel();
    }
    if (runner.doNextStep()) {
      displayer.paint((Graphics2D) g);
    }
  }
}
