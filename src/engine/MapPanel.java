package engine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import mapstructure.DescentMap;

public class MapPanel extends JPanel {
  private DescentMap map;

  public void setMap(DescentMap map) {
    this.map = map;
  }

  @Override
  public void paint(Graphics g) {
    g.setColor(Color.black);
    g.fillRect(0, 0, getWidth(), getHeight());
    map.paint((Graphics2D) g, getSize());
  }
}
