package component;

import java.util.LinkedList;

import mapobject.MapObject;
import mapobject.MultipleObject;
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
  private final DescentMap map;
  private final LinkedList<RoomChange> room_changes;
  private MapObject center_object;

  public MapEngine(DescentMap map) {
    this.map = map;
    room_changes = new LinkedList<RoomChange>();
  }

  public void setCenterObject(MapObject center_object) {
    this.center_object = center_object;
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
    return center_object.getRoom().equals(map.getExitRoom()) || !center_object.isInMap();
  }
}
