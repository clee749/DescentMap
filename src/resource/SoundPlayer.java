package resource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class SoundPlayer {
  public static final String DEFAULT_PATH = "resrc/sounds/";

  private final String base_path;
  private final HashMap<String, LinkedList<Clip>> sounds;

  public SoundPlayer(String base_path) {
    this.base_path = base_path;
    sounds = new HashMap<String, LinkedList<Clip>>();
  }

  public SoundPlayer() {
    this(DEFAULT_PATH);
  }

  public void readSounds(String[] type_paths) {
    for (String type_path : type_paths) {
      try {
        String[] resources = getResourceListing(base_path + type_path);
        for (String resource : resources) {
          String key = type_path + resource;
          try {
            Clip clip = initClip(key);
            if (clip != null) {
              LinkedList<Clip> clips = new LinkedList<Clip>();
              clips.add(clip);
              sounds.put(key, clips);
            }
          }
          catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      catch (URISyntaxException e) {
        e.printStackTrace();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public String[] getResourceListing(String path) throws URISyntaxException, IOException {
    URL dirURL = getClass().getClassLoader().getResource(path);
    if (dirURL != null && dirURL.getProtocol().equals("file")) {
      return new File(dirURL.toURI()).list();
    }

    if (dirURL == null) {
      String me = getClass().getName().replace(".", "/") + ".class";
      dirURL = getClass().getClassLoader().getResource(me);
    }

    if (dirURL.getProtocol().equals("jar")) {
      String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
      JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
      Enumeration<JarEntry> entries = jar.entries();
      Set<String> result = new HashSet<String>();
      while (entries.hasMoreElements()) {
        String name = entries.nextElement().getName();
        if (name.startsWith(path)) {
          String entry = name.substring(path.length());
          int checkSubdir = entry.indexOf("/");
          if (checkSubdir >= 0) {
            entry = entry.substring(0, checkSubdir);
          }
          result.add(entry);
        }
      }
      return result.toArray(new String[result.size()]);
    }

    throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
  }

  public Clip initClip(String subpath) throws Exception {
    URL wavFile = SoundPlayer.class.getClassLoader().getResource(base_path + subpath);
    AudioInputStream ais = AudioSystem.getAudioInputStream(wavFile);
    Clip clip = AudioSystem.getClip();
    clip.open(ais);
    return clip;
  }

  public void playSound(String key, float delta_gain) {
    LinkedList<Clip> clips = sounds.get(key);
    Clip clip = null;
    try {
      if (clips == null) {
        clip = initClip(key);
        clips = new LinkedList<Clip>();
        clips.add(clip);
        sounds.put(key, clips);
      }
      else {
        for (Clip current : clips) {
          if (!current.isRunning()) {
            clip = current;
            break;
          }
        }
        if (clip == null) {
          clip = initClip(key);
          clips.add(clip);
        }
      }
    }
    catch (Exception e) {
      System.out.println("Sound file " + base_path + key + " not found!");
      return;
    }
    // -80.0 <= MASTER_GAIN <= 6.0206
    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
    gainControl.setValue(Math.max(Math.min(delta_gain, gainControl.getMaximum()), gainControl.getMinimum()));
    clip.setFramePosition(0);
    clip.start();
  }

  public static void main(String[] args) throws Exception {
    String[] type_dirs = new String[] {"effects/", "enemies/", "weapons/"};
    SoundPlayer sp = new SoundPlayer(SoundPlayer.DEFAULT_PATH);
    sp.readSounds(type_dirs);
    for (String type_dir : type_dirs) {
      String[] resources = sp.getResourceListing(SoundPlayer.DEFAULT_PATH + type_dir);
      for (String resource : resources) {
        String key = type_dir + resource;
        System.out.println(key);
        sp.playSound(key, 0.0f);
        Thread.sleep(2000);
      }
    }
    // 1 Bomber, Class1Drone
    // 2 MediumLifter
    // 3 MediumHulk, MediumHulkCloaked
    // 4 LightHulk
    // 5
    // 6
    // 7 HeavyHulk
    // 8 Yellow
    // 9 AdvancedLifter
    // 10 Red
    // 11 Class2Drone
    // 12 BabySpider
    // 13 Green
    // 14 Spider
    // 15 VulcanMan
    // 16 PlatformMissile
    // 17 Supervisor
    // 19 VulcanManCloaked
    // 20 PlatformLaser
    // 21 DefenseRobot
    // 25 SecondaryLifter
    // 27 HeavyDriller
    // 34 Gopher
    // 36 MiniBoss

    // 1 Pyro LaserShot 4
    // 2 Pyro LaserShot 3
    // 3 Pyro LaserShot 1
    // 4 Pyro LaserShot 2
    // 5
    // 6 Robot PlasmaShot
    // 7 Reactor
    // 12 FireballShot
  }
}
