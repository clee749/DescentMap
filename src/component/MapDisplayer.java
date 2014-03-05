package component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import resource.ImageHandler;
import structure.DescentMap;

import common.DescentMapException;

enum DisplayMode {
  CONSTRUCTION,
  PLAYTHROUGH;
}


public class MapDisplayer {
  public static final Color BACKGROUND_COLOR = Color.black;
  public static final int SIGHT_RADIUS = 3;

  private final MapRunner runner;
  private final ImageHandler images;
  private final MapConstructionDisplayer construction_displayer;
  private final MapPlayDisplayer play_displayer;
  private DisplayMode display_mode;
  private Dimension dims;
  private Image buffer;
  private Graphics2D bufferg;

  public MapDisplayer(MapRunner runner) {
    this.runner = runner;
    images = new ImageHandler();
    construction_displayer = new MapConstructionDisplayer();
    play_displayer = new MapPlayDisplayer(images, SIGHT_RADIUS);
  }

  public void newMap() {
    DescentMap map = runner.getMap();
    display_mode = DisplayMode.CONSTRUCTION;
    construction_displayer.setMap(map);
    play_displayer.setMap(map);
  }

  public void setSize(Dimension size) {
    runner.setPaused(true);
    dims = size;
    buffer = new BufferedImage(dims.width, dims.height, BufferedImage.TYPE_INT_RGB);
    bufferg = (Graphics2D) buffer.getGraphics();
    int old_pixels_per_cell = play_displayer.getPixelsPerCell();
    play_displayer.setSizes(dims);
    int new_pixels_per_cell = play_displayer.getPixelsPerCell();
    if (new_pixels_per_cell != old_pixels_per_cell) {
      try {
        images.loadImages(play_displayer.getPixelsPerCell());
      }
      catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
    runner.setPaused(false);
  }

  public void finishBuildingMap() {
    display_mode = DisplayMode.PLAYTHROUGH;
  }

  public void paint(Graphics2D g) {
    bufferg.setColor(BACKGROUND_COLOR);
    bufferg.fillRect(0, 0, dims.width, dims.height);
    if (display_mode == null) {
      return;
    }
    switch (display_mode) {
      case CONSTRUCTION:
        construction_displayer.paintMap(bufferg, dims);
        break;
      case PLAYTHROUGH:
        play_displayer.paintMap(bufferg);
        break;
      default:
        throw new DescentMapException("Unexpected DisplayMode: " + display_mode);
    }
    g.drawImage(buffer, 0, 0, null);
  }
}
