package pilot;

import java.util.ArrayList;
import java.util.Map.Entry;

import structure.RoomConnection;

import common.RoomSide;

public class LargeRobotPilot extends RobotPilot {
  private final int min_room_connection_width;

  public LargeRobotPilot(int min_room_connection_width) {
    this.min_room_connection_width = min_room_connection_width;
  }

  @Override
  public ArrayList<Entry<RoomSide, RoomConnection>> findPossibleNextRoomInfos() {
    ArrayList<Entry<RoomSide, RoomConnection>> possible_next_infos =
            new ArrayList<Entry<RoomSide, RoomConnection>>();
    for (Entry<RoomSide, RoomConnection> entry : current_room.getNeighbors().entrySet()) {
      RoomConnection connection = entry.getValue();
      if (!connection.neighbor.equals(previous_exploration_room) &&
              connection.max - connection.min >= min_room_connection_width) {
        possible_next_infos.add(entry);
      }
    }
    return possible_next_infos;
  }
}
