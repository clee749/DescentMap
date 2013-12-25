package engine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JPanel;

import mapstructure.DescentMap;

public class MapPanel extends JPanel {
  private final DescentMap map;
  
  public MapPanel(MapRunner runner) {
    map = runner.getMap();
  }
  
  @Override
  public void paint(Graphics g) {
    g.setColor(Color.black);
    g.fillRect(0, 0, getWidth(), getHeight());
    map.paint((Graphics2D) g, new Point(0, 0), new Point(getWidth() / 2, getHeight() / 2), 10);
  }
}
