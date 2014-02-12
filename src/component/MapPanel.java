package component;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;

import javax.swing.JPanel;

import structure.DescentMap;

import common.DescentMapException;

import external.ImageHandler;

enum DisplayMode {
  CONSTRUCTION,
  PLAYTHROUGH;
}


public class MapPanel extends JPanel implements ComponentListener, MapDisplayer {
  public static final Color BACKGROUND_COLOR = Color.black;
  public static final int SIGHT_RADIUS = 3;

  private final ImageHandler images;
  private DisplayMode display_mode;
  private final MapConstructionDisplayer construction_displayer;
  private final MapPlayDisplayer play_displayer;

  public MapPanel() {
    images = new ImageHandler();
    construction_displayer = new MapConstructionDisplayer();
    play_displayer = new MapPlayDisplayer(images, SIGHT_RADIUS);
  }

  @Override
  public void setMap(DescentMap map) {
    display_mode = DisplayMode.CONSTRUCTION;
    construction_displayer.setMap(map);
    play_displayer.setMap(map);
    play_displayer.setSizes(getSize());
  }

  @Override
  public void finishBuildingMap() {
    display_mode = DisplayMode.PLAYTHROUGH;
  }

  @Override
  public void paint(Graphics g) {
    g.setColor(BACKGROUND_COLOR);
    g.fillRect(0, 0, getWidth(), getHeight());
    if (display_mode == null) {
      return;
    }
    switch (display_mode) {
      case CONSTRUCTION:
        construction_displayer.paintMap((Graphics2D) g, getSize());
        break;
      case PLAYTHROUGH:
        play_displayer.paintMap((Graphics2D) g);
        break;
      default:
        throw new DescentMapException("Unexpected DisplayMode: " + display_mode);
    }
  }

  public void handleComponentResized() {
    play_displayer.setSizes(getSize());
    try {
      images.loadImages(play_displayer.getPixelsPerCell());
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void componentHidden(ComponentEvent arg0) {

  }

  @Override
  public void componentMoved(ComponentEvent arg0) {

  }

  @Override
  public void componentResized(ComponentEvent arg0) {
    handleComponentResized();
  }

  @Override
  public void componentShown(ComponentEvent arg0) {

  }
}
