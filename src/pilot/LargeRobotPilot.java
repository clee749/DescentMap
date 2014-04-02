package pilot;

import java.util.ArrayList;
import java.util.Map.Entry;

import structure.RoomConnection;

import common.RoomSide;

public class LargeRobotPilot extends RobotPilot {
  private final int min_room_dim;

  public LargeRobotPilot(int min_room_dim) {
    this.min_room_dim = min_room_dim;
  }

  @Override
  public ArrayList<Entry<RoomSide, RoomConnection>> findPossibleNextRoomInfos() {
    ArrayList<Entry<RoomSide, RoomConnection>> possible_next_infos =
            new ArrayList<Entry<RoomSide, RoomConnection>>();
    for (Entry<RoomSide, RoomConnection> entry : current_room.getNeighbors().entrySet()) {
      RoomConnection connection = entry.getValue();
      if (!connection.neighbor.equals(previous_exploration_room) &&
              connection.max - connection.min >= min_room_dim &&
              connection.neighbor.getHeight() >= min_room_dim &&
              connection.neighbor.getWidth() >= min_room_dim) {
        possible_next_infos.add(entry);
      }
    }
    return possible_next_infos;
  }
}
