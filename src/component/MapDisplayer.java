package component;

import java.awt.Graphics;

import structure.DescentMap;

public interface MapDisplayer {
  public void setMap(DescentMap map);

  public void finishBuildingMap();

  public void paint(Graphics g);
}
