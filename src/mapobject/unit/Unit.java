package mapobject.unit;

import java.awt.Graphics2D;
import java.awt.Point;

import mapobject.MapObject;
import pilot.MoveDirection;
import pilot.Pilot;
import pilot.PilotMove;
import pilot.TurnDirection;
import structure.Room;
import structure.RoomConnection;

import common.DescentMapException;
import common.MapUtils;
import common.RoomSide;

public abstract class Unit extends MapObject {
  protected final double move_speed;
  protected final double turn_speed;
  protected double previous_x_loc;
  protected double previous_y_loc;
  protected double direction;
  protected Pilot pilot;
  // protected Gunner gunner;
  protected PilotMove next_move;
  protected boolean fire_cannon;

  public Unit(double move_speed, double turn_speed, double radius, Room room, double x_loc, double y_loc) {
    super(radius, room, x_loc, y_loc);
    this.move_speed = move_speed;
    this.turn_speed = turn_speed;
    direction = 0.0;
  }

  public double getDirection() {
    return direction;
  }

  public void setRoom(Room room) {
    this.room = room;
    pilot.updateCurrentRoom(room);
  }

  @Override
  public void paint(Graphics2D g, Point ref_cell, Point ref_cell_corner_pixel, int pixels_per_cell) {

  }

  @Override
  public void computeNextStep() {
    next_move = pilot.findNextMove();
  }

  @Override
  public void doNextStep(long ms_elapsed) {
    previous_x_loc = x_loc;
    previous_y_loc = y_loc;
    executePilotMove(ms_elapsed);
    boundInsideAndUpdateRoom();
  }

  public void executePilotMove(long ms_elapsed) {
    if (next_move == null) {
      return;
    }

    double s_elapsed = ms_elapsed / 1000.0;

    MoveDirection move = next_move.getMove();
    if (move != null) {
      switch (move) {
        case FORWARD:
          x_loc += move_speed * Math.cos(direction) * s_elapsed;
          y_loc += move_speed * Math.sin(direction) * s_elapsed;
          break;
        default:
          throw new DescentMapException("Unexpected MoveDirection: " + move);
      }
    }

    TurnDirection turn = next_move.getTurn();
    if (turn != null) {
      switch (turn) {
        case COUNTER_CLOCKWISE:
          direction = MapUtils.normalizeAngle(direction + turn_speed * s_elapsed);
          break;
        case CLOCKWISE:
          direction = MapUtils.normalizeAngle(direction - turn_speed * s_elapsed);
          break;
        default:
          throw new DescentMapException("Unexpected TurnDirection: " + turn);
      }
    }
  }

  public void boundInsideAndUpdateRoom() {
    Point nw_corner = room.getNWCorner();
    Point se_corner = room.getSECorner();

    // north wall
    // Are we outside the Room bounds?
    if (y_loc - radius < nw_corner.y) {
      // Does the Room have a neighbor in this direction?
      RoomConnection connection = room.getConnectionInDirection(RoomSide.NORTH);
      // Are we within the connection to the neighbor Room?
      // we need to use previous_x_loc because we have not yet bounded x_loc
      if (connection != null && connection.min <= previous_x_loc - radius &&
              previous_x_loc + radius <= connection.max) {
        // Is our center in the neighbor Room?
        if (y_loc < nw_corner.y) {
          // we are no longer in the same Room
          room = connection.neighbor;
          // make sure the Pilot knows about the new Room
          pilot.updateCurrentRoom(room);
        }
      }
      else {
        // hit the wall
        y_loc = nw_corner.y + radius;
      }
    }

    // south wall
    else if (y_loc + radius > se_corner.y) {
      RoomConnection connection = room.getConnectionInDirection(RoomSide.SOUTH);
      if (connection != null && connection.min <= previous_x_loc - radius &&
              previous_x_loc + radius <= connection.max) {
        if (y_loc > se_corner.y) {
          room = connection.neighbor;
          pilot.updateCurrentRoom(room);
        }
      }
      else {
        y_loc = se_corner.y - radius;
      }
    }

    // west wall
    if (x_loc - radius < nw_corner.x) {
      RoomConnection connection = room.getConnectionInDirection(RoomSide.WEST);
      if (connection != null && connection.min <= previous_y_loc - radius &&
              previous_y_loc + radius <= connection.max) {
        if (x_loc < nw_corner.x) {
          room = connection.neighbor;
          pilot.updateCurrentRoom(room);
        }
      }
      else {
        x_loc = nw_corner.x + radius;
      }
    }

    // east wall
    else if (x_loc + radius > se_corner.x) {
      RoomConnection connection = room.getConnectionInDirection(RoomSide.EAST);
      if (connection != null && connection.min <= previous_y_loc - radius &&
              previous_y_loc + radius <= connection.max) {
        if (x_loc > se_corner.x) {
          room = connection.neighbor;
          pilot.updateCurrentRoom(room);
        }
      }
      else {
        x_loc = se_corner.x - radius;
      }
    }
  }
}
