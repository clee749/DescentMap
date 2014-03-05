package component;

import java.awt.Graphics;

import org.jdesktop.jdic.screensaver.SimpleScreensaver;

public class MapScreensaver extends SimpleScreensaver {
  private MapRunner runner;
  private MapDisplayer displayer;

  @Override
  public void init() {
    // ScreensaverSettings settings = getContext().getSettings();
    // boolean checkbox = settings.getProperty("checkbox") != null;
    // System.out.println(checkbox);
    // runner = new MapRunner();
    // displayer = runner.getDisplayer();
    // displayer.setSize(getContext().getComponent().getSize());
    // runner.newLevel();
  }

  @Override
  public void paint(Graphics g) {
    // if (runner.getState().equals(RunnerState.COMPLETE)) {
    // runner.newLevel();
    // }
    // if (runner.doNextStep()) {
    // displayer.paint((Graphics2D) g);
    // }
  }
}
