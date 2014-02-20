package component;

import java.util.LinkedList;

import mapobject.MapObject;
import mapobject.MultipleObject;
import mapobject.unit.Pyro;
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
  public static final float GAIN_REDUCTION_PER_UNIT_DISTANCE = 5.0f;
  public static final float MAX_GAIN_REDUCTION = 40.0f;

  private final SoundPlayer sounds;
  private final LinkedList<Pyro> created_pyros;
  private final LinkedList<RoomChange> room_changes;
  private DescentMap map;
  private MapObject center_object;
  private boolean sounds_active;

  public MapEngine() {
    sounds = new SoundPlayer();
    created_pyros = new LinkedList<Pyro>();
    room_changes = new LinkedList<RoomChange>();
  }

  public void newMap(DescentMap map) {
    this.map = map;
    created_pyros.clear();
    room_changes.clear();
    center_object = null;
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
    if (center_object instanceof Pyro) {
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

  public void computeNextStep(double s_elapsed) {
    for (Room room : map.getAllRooms()) {
      room.computeNextStep(s_elapsed);
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
              Math.min(GAIN_REDUCTION_PER_UNIT_DISTANCE * (float) distance, MAX_GAIN_REDUCTION);
      sounds.playSound(name, -gain_reduction);
    }
  }
}
