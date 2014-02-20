package resource;

import java.io.IOException;
import java.net.URL;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;

public class MusicPlayer {
  public static final String DEFAULT_PATH = "resrc/music";
  public static final int NUM_LEVELS = 22;

  private final String path;
  private Sequencer sequencer;

  public MusicPlayer(String path) {
    this.path = path;
    try {
      sequencer = MidiSystem.getSequencer();
      sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
    }
    catch (MidiUnavailableException e) {
      System.out.println("Cannot instantiate Midi device!");
    }
  }

  public MusicPlayer() {
    this(DEFAULT_PATH);
  }

  public void playMusic(int level) {
    if (sequencer == null) {
      return;
    }
    String filename = String.format("%s/GAME%02d.MID", path, level);
    URL midiFile = getClass().getClassLoader().getResource(filename);
    try {
      sequencer.setSequence(MidiSystem.getSequence(midiFile));
      if (!sequencer.isOpen()) {
        sequencer.open();
      }
      sequencer.start();
    }
    catch (MidiUnavailableException mue) {
      System.out.println("Midi device unavailable!");
    }
    catch (InvalidMidiDataException imde) {
      System.out.println("Invalid Midi data!");
    }
    catch (IOException ioe) {
      System.out.println("I/O Error!");
    }
  }

  public int playMusic() {
    int level = (int) (Math.random() * NUM_LEVELS + 1);
    playMusic(level);
    return level;
  }

  public void stop() {
    if (sequencer != null && sequencer.isOpen()) {
      sequencer.stop();
    }
  }

  public void close() {
    if (sequencer != null) {
      sequencer.close();
    }
  }
}
