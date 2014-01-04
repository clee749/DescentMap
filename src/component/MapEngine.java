package component;

import java.util.Iterator;
import java.util.LinkedList;

import mapobject.MapObject;
import mapobject.unit.pyro.Pyro;
import structure.DescentMap;
import structure.Room;

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
  private Pyro center_ship;

  public MapEngine(DescentMap map) {
    this.map = map;
    room_changes = new LinkedList<RoomChange>();
  }

  public void setCenterShip(Pyro ship) {
    center_ship = ship;
  }

  public void addObject(MapObject object) {
    object.getRoom().addChild(object);
  }

  public void computeNextStep(double s_elapsed) {
    for (Room room : map.getRooms()) {
      for (MapObject object : room.getChildren()) {
        object.computeNextStep(s_elapsed);
      }
    }
  }

  public void doNextStep(double s_elapsed) {
    room_changes.clear();
    LinkedList<MapObject> created_objects = new LinkedList<MapObject>();
    for (Room room : map.getRooms()) {
      for (Iterator<MapObject> it = room.getChildren().iterator(); it.hasNext();) {
        MapObject object = it.next();
        MapObject created = object.doNextStep(this, s_elapsed);
        if (created != null) {
          created_objects.add(created);
        }
        if (!object.isInMap()) {
          it.remove();
        }
      }
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
    return center_ship.getRoom().equals(map.getExitRoom());
  }
}
