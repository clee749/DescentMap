package mapobject;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Point2D;

import pilot.Pilot;
import pilot.PilotAction;
import structure.Room;
import structure.RoomConnection;
import util.MapUtils;

import common.Constants;
import common.DescentMapException;
import common.RoomSide;
import component.MapEngine;

import external.ImageHandler;

public abstract class MovableObject extends MapObject {
  protected double move_speed;
  protected double turn_speed;
  protected Pilot pilot;
  protected double direction;
  protected double previous_x_loc;
  protected double previous_y_loc;
  protected PilotAction next_action;

  public MovableObject(double radius, Pilot pilot, Room room, double x_loc, double y_loc, double direction,
          double move_speed, double turn_speed) {
    super(radius, room, x_loc, y_loc);
    this.move_speed = move_speed;
    this.turn_speed = turn_speed;
    this.pilot = pilot;
    this.direction = direction;
    pilot.bindToObject(this);
  }

  public MovableObject(double radius, Pilot pilot, Room room, double x_loc, double y_loc, double direction) {
    super(radius, room, x_loc, y_loc);
    move_speed = Constants.getMaxMoveSpeed(type);
    Double raw_turn_speed = Constants.getMaxTurnSpeed(type);
    turn_speed = (raw_turn_speed != null ? raw_turn_speed : 0.0);
    this.pilot = pilot;
    this.direction = direction;
    pilot.bindToObject(this);
  }

  public MovableObject(Pilot pilot, Room room, double x_loc, double y_loc, double direction) {
    super(room, x_loc, y_loc);
    move_speed = Constants.getMaxMoveSpeed(type);
    Double raw_turn_speed = Constants.getMaxTurnSpeed(type);
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

  public double getMoveSpeed() {
    return move_speed;
  }

  public double getTurnSpeed() {
    return turn_speed;
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

  public Image getImage(ImageHandler images) {
    return images.getImage(image_name, direction);
  }

  @Override
  public void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell) {
    Point center_pixel = MapUtils.coordsToPixel(x_loc, y_loc, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    Image image = getImage(images);
    g.drawImage(image, center_pixel.x - image.getWidth(null) / 2, center_pixel.y - image.getHeight(null) / 2,
            null);
  }

  @Override
  public void planNextAction(double s_elapsed) {
    next_action = pilot.findNextAction(s_elapsed);
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
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
    if (next_action == null) {
      return;
    }

    switch (next_action.move) {
      case NONE:
        break;
      case FORWARD:
        x_loc += move_speed * Math.cos(direction) * s_elapsed;
        y_loc += move_speed * Math.sin(direction) * s_elapsed;
        break;
      case BACKWARD:
        x_loc -= move_speed * Math.cos(direction) * s_elapsed;
        y_loc -= move_speed * Math.sin(direction) * s_elapsed;
        break;
      default:
        throw new DescentMapException("Unexpected MoveDirection: " + next_action.move);
    }

    switch (next_action.strafe) {
      case NONE:
        break;
      case LEFT:
        Point2D.Double strafe_dxdy = MapUtils.perpendicularVector(move_speed * s_elapsed, direction);
        x_loc -= strafe_dxdy.x;
        y_loc -= strafe_dxdy.y;
        break;
      case RIGHT:
        strafe_dxdy = MapUtils.perpendicularVector(move_speed * s_elapsed, direction);
        x_loc += strafe_dxdy.x;
        y_loc += strafe_dxdy.y;
        break;
      default:
        throw new DescentMapException("Unexpected StrafeDirection: " + next_action.strafe);
    }

    switch (next_action.turn) {
      case NONE:
        break;
      case COUNTER_CLOCKWISE:
        direction = MapUtils.normalizeAngle(direction + turn_speed * s_elapsed);
        break;
      case CLOCKWISE:
        direction = MapUtils.normalizeAngle(direction - turn_speed * s_elapsed);
        break;
      default:
        throw new DescentMapException("Unexpected TurnDirection: " + next_action.turn);
    }
  }

  public boolean boundInsideAndUpdateRoom(MapEngine engine) {
    Point nw_corner = room.getNWCorner();
    Point se_corner = room.getSECorner();
    boolean location_accepted = true;
    Room next_room = null;

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
          // mark that we need to update our current Room
          // we need to wait to actually update room because we use it in the checks below
          next_room = connection.neighbor;
        }
      }
      else {
        // hit the wall
        handleHittingWall(RoomSide.NORTH);
        location_accepted = false;
      }
    }

    // south wall
    else if (y_loc + radius > se_corner.y) {
      RoomConnection connection = room.getConnectionInDirection(RoomSide.SOUTH);
      if (connection != null && connection.min <= previous_x_loc - radius &&
              previous_x_loc + radius <= connection.max) {
        if (y_loc > se_corner.y) {
          next_room = connection.neighbor;
        }
      }
      else {
        handleHittingWall(RoomSide.SOUTH);
        location_accepted = false;
      }
    }

    // west wall
    if (x_loc - radius < nw_corner.x) {
      RoomConnection connection = room.getConnectionInDirection(RoomSide.WEST);
      if (connection != null && connection.min <= previous_y_loc - radius &&
              previous_y_loc + radius <= connection.max) {
        if (x_loc < nw_corner.x) {
          next_room = connection.neighbor;
        }
      }
      else {
        handleHittingWall(RoomSide.WEST);
        location_accepted = false;
      }
    }

    // east wall
    else if (x_loc + radius > se_corner.x) {
      RoomConnection connection = room.getConnectionInDirection(RoomSide.EAST);
      if (connection != null && connection.min <= previous_y_loc - radius &&
              previous_y_loc + radius <= connection.max) {
        if (x_loc > se_corner.x) {
          next_room = connection.neighbor;
        }
      }
      else {
        handleHittingWall(RoomSide.EAST);
        location_accepted = false;
      }
    }

    if (next_room != null) {
      updateRoom(engine, room, next_room);
    }

    return location_accepted;
  }

  public void handleHittingWall(RoomSide wall_side) {
    switch (wall_side) {
      case NORTH:
        y_loc = room.getNWCorner().y + radius;
        break;
      case SOUTH:
        y_loc = room.getSECorner().y - radius;
        break;
      case WEST:
        x_loc = room.getNWCorner().x + radius;
        break;
      case EAST:
        x_loc = room.getSECorner().x - radius;
        break;
      default:
        throw new DescentMapException("Unexpected RoomSide: " + wall_side);
    }
  }
}
