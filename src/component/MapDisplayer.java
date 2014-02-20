package component;

import java.awt.Graphics;

import structure.DescentMap;

public interface MapDisplayer {
  public void setRunner(MapRunner runner);

  public void setEngine(MapEngine engine);

  public void setNewMap(DescentMap map);

  public void finishBuildingMap();

  public void paint(Graphics g);
}
