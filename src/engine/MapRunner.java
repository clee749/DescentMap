package engine;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import mapstructure.DescentMap;

import common.Constants;

public class MapRunner {
  private final DescentMap map;
  
  public MapRunner() {
    map = new DescentMap(Constants.BUILDER_MAX_ROOM_SIZE);
  }
  
  public DescentMap getMap() {
    return map;
  }
  
  public void stepMapBuilder() {
    map.addRoom();
  }
  
  public static void main(String[] args) {
    MapRunner runner = new MapRunner();
    JFrame frame = new JFrame();
    MapPanel panel = new MapPanel(runner);
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    frame.setSize(screen.width, screen.height - 50);
    frame.setTitle("MAP");
    frame.add(panel);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setMinimumSize(new Dimension(100, 100));
    frame.setVisible(true);
    
    for (int i = 0; i < Constants.BUILDER_MAX_NUM_ROOMS; ++i) {
      runner.stepMapBuilder();
      panel.repaint();
      try {
        Thread.sleep(Constants.RUNNER_SLEEP);
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    System.out.println("DONE");
  }
}
