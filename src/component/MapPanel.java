package component;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;

import resource.MusicPlayer;

public class MapPanel extends JPanel implements ComponentListener, KeyListener {
  private final MapDisplayer displayer;
  private final MapEngine engine;
  private final MusicPlayer music;
  private int music_level;
  private boolean music_active;
  private boolean music_playing;

  public MapPanel(MapRunner runner) {
    displayer = runner.getDisplayer();
    engine = runner.getEngine();
    music = new MusicPlayer();
  }

  @Override
  public void paint(Graphics g) {
    displayer.paint((Graphics2D) g);
  }

  public int playMusic() {
    music_level = (int) (Math.random() * MusicPlayer.NUM_LEVELS + 1);
    if (music_active) {
      music.playMusic(music_level);
      music_playing = true;
    }
    return music_level;
  }

  public void stopMusic() {
    if (music_active) {
      music.stop();
      music_playing = false;
    }
  }

  public void closeMusic() {
    music.close();
  }

  @Override
  public void componentHidden(ComponentEvent e) {

  }

  @Override
  public void componentMoved(ComponentEvent e) {

  }

  @Override
  public void componentResized(ComponentEvent e) {
    displayer.setSize(getSize());
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
          music.stop();
          music_playing = false;
          music_active = false;
        }
        else {
          music.playMusic(music_level);
          music_playing = true;
          music_active = true;
        }
        return;
      case KeyEvent.VK_S:
        engine.toggleSounds();
        return;
      case KeyEvent.VK_BACK_SPACE:
        engine.togglePlayability();
        return;
    }
    engine.handleKeyPressed(key_code);
  }

  @Override
  public void keyReleased(KeyEvent e) {
    engine.handleKeyReleased(e.getKeyCode());
  }

  @Override
  public void keyTyped(KeyEvent e) {

  }
}
