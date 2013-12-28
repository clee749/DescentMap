package engine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import mapstructure.DescentMap;

public class MapPanel extends JPanel {
  private final DescentMap map;
  private final MapConstructionDisplayer construction_displayer;

  public MapPanel(MapRunner runner) {
    map = runner.getMap();
    construction_displayer = map.getConstructionDisplayer();
  }

  @Override
  public void paint(Graphics g) {
    g.setColor(Color.black);
    g.fillRect(0, 0, getWidth(), getHeight());
    construction_displayer.displayMap((Graphics2D) g, getSize());
  }
}
