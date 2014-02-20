package component;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.JPanel;

import resource.ImageHandler;
import resource.MusicPlayer;
import structure.DescentMap;

import common.DescentMapException;

enum DisplayMode {
  CONSTRUCTION,
  PLAYTHROUGH;
}


public class MapPanel extends JPanel implements MapDisplayer, ComponentListener, KeyListener {
  public static final Color BACKGROUND_COLOR = Color.black;
  public static final int SIGHT_RADIUS = 3;

  private final ImageHandler images;
  private final MapConstructionDisplayer construction_displayer;
  private final MapPlayDisplayer play_displayer;
  private final MusicPlayer music_player;
  private MapRunner runner;
  private MapEngine engine;
  private DisplayMode display_mode;
  private int music_level;
  private boolean music_active;
  private boolean music_playing;

  public MapPanel() {
    images = new ImageHandler();
    construction_displayer = new MapConstructionDisplayer();
    play_displayer = new MapPlayDisplayer(images, SIGHT_RADIUS);
    music_player = new MusicPlayer();
  }

  @Override
  public void setRunner(MapRunner runner) {
    this.runner = runner;
  }

  @Override
  public void setEngine(MapEngine engine) {
    this.engine = engine;
  }

  @Override
  public void setNewMap(DescentMap map) {
    display_mode = DisplayMode.CONSTRUCTION;
    map = runner.getMap();
    construction_displayer.setMap(map);
    play_displayer.setMap(map);
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

  public int playMusic() {
    music_level = (int) (Math.random() * MusicPlayer.NUM_LEVELS + 1);
    if (music_active) {
      music_player.playMusic(music_level);
      music_playing = true;
    }
    return music_level;
  }

  public void stopMusic() {
    if (music_active) {
      music_player.stop();
      music_playing = false;
    }
  }

  public void closeMusic() {
    music_player.close();
  }

  @Override
  public void componentHidden(ComponentEvent e) {

  }

  @Override
  public void componentMoved(ComponentEvent e) {

  }

  @Override
  public void componentResized(ComponentEvent e) {
    runner.setPaused(true);
    int old_pixels_per_cell = play_displayer.getPixelsPerCell();
    play_displayer.setSizes(getSize());
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

  @Override
  public void componentShown(ComponentEvent e) {

  }

  @Override
  public void keyPressed(KeyEvent e) {
    int key_code = e.getKeyCode();
    switch (key_code) {
      case KeyEvent.VK_M:
        if (music_playing) {
          music_player.stop();
          music_playing = false;
          music_active = false;
        }
        else {
          music_player.playMusic(music_level);
          music_playing = true;
          music_active = true;
        }
        break;
      case KeyEvent.VK_S:
        engine.toggleSounds();
        break;
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {

  }

  @Override
  public void keyTyped(KeyEvent e) {

  }
}
