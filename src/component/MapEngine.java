package component;

import java.util.ArrayList;
import java.util.LinkedList;

import mapobject.MapObject;
import mapobject.MultipleObject;
import mapobject.unit.Pyro;
import mapobject.unit.robot.Robot;
import pilot.ComputerPyroPilot;
import pilot.HumanPyroPilot;
import resource.SoundPlayer;
import structure.DescentMap;
import structure.Room;

import common.ObjectType;

class RoomChange {
  public final MapObject object;
  public final Room src_room;
  public final Room dst_room;

  public RoomChange(MapObject object, Room src_room, Room dst_room) {
    this.object = object;
    this.src_room = src_room;
    this.dst_room = dst_room;
  }
}


public class MapEngine {
  public static final double MAX_SOUND_DISTANCE = MapPanel.SIGHT_RADIUS * 2;
  public static final float SOUND_GAIN_REDUCTION_PER_UNIT_DISTANCE = 5.0f;
  public static final float MAX_SOUND_GAIN_REDUCTION = 40.0f;
  public static final double EXPECTED_NUM_GROWLERS_PER_SECOND = 3.0;

  private final SoundPlayer sounds;
  private final LinkedList<Pyro> created_pyros;
  private final LinkedList<RoomChange> room_changes;
  private final ArrayList<Robot> growlers;
  private final HumanPyroPilot human_pilot;
  private DescentMap map;
  private MapObject center_object;
  private boolean sounds_active;
  private boolean is_playable;

  public MapEngine() {
    sounds = new SoundPlayer();
    created_pyros = new LinkedList<Pyro>();
    room_changes = new LinkedList<RoomChange>();
    growlers = new ArrayList<Robot>();
    human_pilot = new HumanPyroPilot();
  }

  public void newMap(DescentMap map) {
    this.map = map;
    created_pyros.clear();
    room_changes.clear();
    center_object = map.getCenterObject();
  }

  public LinkedList<Pyro> getCreatedPyros() {
    return created_pyros;
  }

  public void addCreatedPyro(Pyro pyro) {
    created_pyros.add(pyro);
    pyro.setEngine(this);
  }

  public void setCenterObject(MapObject center_object) {
    this.center_object = center_object;
    if (center_object.getType().equals(ObjectType.Pyro)) {
      ((Pyro) center_object).setPlayPersonalSounds(sounds_active);
    }
    map.setCenterObject(center_object);
  }

  public void addObject(MapObject object) {
    if (object.getType().equals(ObjectType.MultipleObject)) {
      for (MapObject inner_object : ((MultipleObject) object).getObjects()) {
        addObject(inner_object);
      }
    }
    else {
      object.getRoom().addChild(object);
    }
  }

  public void planNextStep(double s_elapsed) {
    for (Room room : map.getAllRooms()) {
      room.planNextStep(s_elapsed);
    }
  }

  public void doNextStep(double s_elapsed) {
    room_changes.clear();
    LinkedList<MapObject> created_objects = new LinkedList<MapObject>();
    for (Room room : map.getAllRooms()) {
      created_objects.addAll(room.doNextStep(this, s_elapsed));
    }
    for (RoomChange room_change : room_changes) {
      room_change.src_room.removeChild(room_change.object);
      room_change.dst_room.addChild(room_change.object);
    }
    for (MapObject created : created_objects) {
      addObject(created);
    }
    handleGrowlingRobots(s_elapsed);
  }

  public void changeRooms(MapObject object, Room src_room, Room dst_room) {
    room_changes.add(new RoomChange(object, src_room, dst_room));
  }

  public boolean levelComplete() {
    return map.getExitRoom().equals(center_object.getRoom());
  }

  public void spawnPyro(Pyro pyro, boolean is_center_object) {
    map.spawnPyro(pyro, is_center_object);
    if (pyro != null) {
      pyro.setEngine(this);
    }
  }

  public void spawnPyro(boolean is_center_object) {
    spawnPyro(null, is_center_object);
  }

  public void respawnPyroAfterDeath(Pyro pyro) {
    spawnPyro(pyro, pyro.equals(center_object));
  }

  public void toggleSounds() {
    sounds_active = !sounds_active;
    if (center_object instanceof Pyro) {
      ((Pyro) center_object).setPlayPersonalSounds(sounds_active);
    }
  }

  public void playSound(String name) {
    sounds.playSound(name, 0.0f);
  }

  public void playSound(String name, double src_x, double src_y) {
    if (sounds_active) {
      double distance = Math.hypot(src_x - center_object.getX(), src_y - center_object.getY());
      if (distance > MAX_SOUND_DISTANCE) {
        return;
      }
      float gain_reduction =
              Math.min(SOUND_GAIN_REDUCTION_PER_UNIT_DISTANCE * (float) distance, MAX_SOUND_GAIN_REDUCTION);
      sounds.playSound(name, -gain_reduction);
    }
  }

  public void registerGrowler(Robot robot) {
    if (Math.hypot(robot.getX() - center_object.getX(), robot.getY() - center_object.getY()) < MAX_SOUND_DISTANCE) {
      growlers.add(robot);
    }
  }

  public void handleGrowlingRobots(double s_elapsed) {
    if (!growlers.isEmpty() && Math.random() / s_elapsed < EXPECTED_NUM_GROWLERS_PER_SECOND) {
      Robot growler = growlers.get((int) (Math.random() * growlers.size()));
      growler.confirmGrowl();
      playSound(growler.getGrowlSoundKey(), growler.getX(), growler.getY());
    }
    growlers.clear();
  }

  public void togglePlayability() {
    is_playable = !is_playable;
    if (center_object instanceof Pyro) {
      if (is_playable) {
        ((Pyro) center_object).setPilot(human_pilot);
      }
      else {
        ((Pyro) center_object).setPilot(new ComputerPyroPilot());
      }
    }
  }

  public void handleKeyPressed(int key_code) {
    if (is_playable) {
      human_pilot.handleKeyPressed(key_code);
    }
  }

  public void handleKeyReleased(int key_code) {
    if (is_playable) {
      human_pilot.handleKeyReleased(key_code);
    }
  }
}
