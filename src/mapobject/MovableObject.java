package mapobject;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;

import pilot.MoveDirection;
import pilot.Pilot;
import pilot.PilotMove;
import pilot.TurnDirection;
import structure.Room;
import structure.RoomConnection;
import util.MapUtils;

import common.Constants;
import common.RoomSide;
import component.MapEngine;

import external.ImageHandler;

public abstract class MovableObject extends MapObject {
  protected final double move_speed;
  protected final double turn_speed;
  protected Pilot pilot;
  protected double direction;
  protected double previous_x_loc;
  protected double previous_y_loc;
  protected PilotMove next_movement;

  public MovableObject(Pilot pilot, Room room, double x_loc, double y_loc, double direction) {
    super(room, x_loc, y_loc);
    move_speed = Constants.getMoveSpeed(type);
    Double raw_turn_speed = Constants.getTurnSpeed(type);
    turn_speed = (raw_turn_speed != null ? raw_turn_speed : 0.0);
    this.pilot = pilot;
    this.direction = direction;
    pilot.bindToObject(this);
  }

  @Override
  public double getRadius() {
    return Constants.getRadius(type);
  }

  public double getDirection() {
    return direction;
  }

  public Pilot getPilot() {
    return pilot;
  }

  public void setPilot(Pilot pilot) {
    this.pilot = pilot;
  }

  public void updateRoom(MapEngine engine, Room previous_room, Room next_room) {
    room = next_room;
    pilot.updateCurrentRoom(next_room);
    engine.changeRooms(this, previous_room, next_room);
  }

  @Override
  public void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell) {
    Point center_pixel = MapUtils.coordsToPixel(x_loc, y_loc, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    Image image = images.getImage(image_name, direction);
    g.drawImage(image, center_pixel.x - image.getWidth(null) / 2, center_pixel.y - image.getHeight(null) / 2,
            null);
  }

  @Override
  public void computeNextStep(double s_elapsed) {
    next_movement = pilot.findNextMove(s_elapsed);
  }

  @Override
  public MapObject doNextStep(MapEngine engine, double s_elapsed) {
    doNextMovement(engine, s_elapsed);
    return null;
  }

  public boolean doNextMovement(MapEngine engine, double s_elapsed) {
    previous_x_loc = x_loc;
    previous_y_loc = y_loc;
    computeNextLocation(s_elapsed);
    return boundInsideAndUpdateRoom(engine);
  }

  public void computeNextLocation(double s_elapsed) {
    if (next_movement == null) {
      return;
    }

    MoveDirection move = next_movement.move;
    if (move != null) {
      switch (move) {
        case FORWARD:
          x_loc += move_speed * Math.cos(direction) * s_elapsed;
          y_loc += move_speed * Math.sin(direction) * s_elapsed;
          break;
      }
    }

    TurnDirection turn = next_movement.turn;
    if (turn != null) {
      switch (turn) {
        case COUNTER_CLOCKWISE:
          direction = MapUtils.normalizeAngle(direction + turn_speed * s_elapsed);
          break;
        case CLOCKWISE:
          direction = MapUtils.normalizeAngle(direction - turn_speed * s_elapsed);
          break;
      }
    }
  }

  public boolean boundInsideAndUpdateRoom(MapEngine engine) {
    Point nw_corner = room.getNWCorner();
    Point se_corner = room.getSECorner();
    boolean location_accepted = true;

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
          // update our current Room
          updateRoom(engine, room, connection.neighbor);
        }
      }
      else {
        // hit the wall
        y_loc = nw_corner.y + radius;
        location_accepted = false;
      }
    }

    // south wall
    else if (y_loc + radius > se_corner.y) {
      RoomConnection connection = room.getConnectionInDirection(RoomSide.SOUTH);
      if (connection != null && connection.min <= previous_x_loc - radius &&
              previous_x_loc + radius <= connection.max) {
        if (y_loc > se_corner.y) {
          updateRoom(engine, room, connection.neighbor);
        }
      }
      else {
        y_loc = se_corner.y - radius;
        location_accepted = false;
      }
    }

    // west wall
    if (x_loc - radius < nw_corner.x) {
      RoomConnection connection = room.getConnectionInDirection(RoomSide.WEST);
      if (connection != null && connection.min <= previous_y_loc - radius &&
              previous_y_loc + radius <= connection.max) {
        if (x_loc < nw_corner.x) {
          updateRoom(engine, room, connection.neighbor);
        }
      }
      else {
        x_loc = nw_corner.x + radius;
        location_accepted = false;
      }
    }

    // east wall
    else if (x_loc + radius > se_corner.x) {
      RoomConnection connection = room.getConnectionInDirection(RoomSide.EAST);
      if (connection != null && connection.min <= previous_y_loc - radius &&
              previous_y_loc + radius <= connection.max) {
        if (x_loc > se_corner.x) {
          updateRoom(engine, room, connection.neighbor);
        }
      }
      else {
        x_loc = se_corner.x - radius;
        location_accepted = false;
      }
    }

    return location_accepted;
  }
}
