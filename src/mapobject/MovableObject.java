package mapobject;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.HashMap;

import pilot.Pilot;
import pilot.PilotAction;
import resource.ImageHandler;
import structure.Room;
import structure.RoomConnection;
import util.MapUtils;

import common.DescentMapException;
import common.ObjectType;
import common.RoomSide;
import component.MapEngine;

public abstract class MovableObject extends MapObject {
  private static final HashMap<ObjectType, Double> MAX_MOVE_SPEEDS = getMaxMoveSpeeds();
  private static final HashMap<ObjectType, Double> MAX_TURN_SPEEDS = getMaxTurnSpeeds();

  private static HashMap<ObjectType, Double> getMaxMoveSpeeds() {
    HashMap<ObjectType, Double> speeds = new HashMap<ObjectType, Double>();
    speeds.put(ObjectType.HeavyHulk, 0.1);
    speeds.put(ObjectType.MiniBoss, 0.1);
    speeds.put(ObjectType.PlatformMissile, 0.1);
    speeds.put(ObjectType.MediumHulk, 0.2);
    speeds.put(ObjectType.MediumHulkCloaked, 0.2);
    speeds.put(ObjectType.DefenseRobot, 0.3);
    speeds.put(ObjectType.PlatformLaser, 0.3);
    speeds.put(ObjectType.Spider, 0.3);
    speeds.put(ObjectType.HeavyDriller, 0.4);
    speeds.put(ObjectType.LightHulk, 0.4);
    speeds.put(ObjectType.Class1Drone, 0.5);
    speeds.put(ObjectType.Class2Drone, 0.5);
    speeds.put(ObjectType.SecondaryLifter, 0.5);
    speeds.put(ObjectType.BabySpider, 0.6);
    speeds.put(ObjectType.AdvancedLifter, 0.9);
    speeds.put(ObjectType.Bomber, 0.9);
    speeds.put(ObjectType.MediumLifter, 0.9);
    speeds.put(ObjectType.Pyro, 1.0);
    speeds.put(ObjectType.FireballShot, 1.1);
    speeds.put(ObjectType.SmartPlasma, 1.5);
    speeds.put(ObjectType.ConcussionMissile, 2.5);
    speeds.put(ObjectType.HomingMissile, 2.5);
    speeds.put(ObjectType.MegaMissile, 2.5);
    speeds.put(ObjectType.SmartMissile, 2.5);
    speeds.put(ObjectType.LaserShot, 3.0);
    speeds.put(ObjectType.SpreadfireShot, 3.0);
    speeds.put(ObjectType.FusionShot, 3.5);
    speeds.put(ObjectType.PlasmaShot, 3.5);
    return speeds;
  }

  private static HashMap<ObjectType, Double> getMaxTurnSpeeds() {
    HashMap<ObjectType, Double> speeds = new HashMap<ObjectType, Double>();
    speeds.put(ObjectType.HeavyHulk, MapUtils.PI_OVER_FOUR);
    speeds.put(ObjectType.MiniBoss, MapUtils.PI_OVER_FOUR);
    speeds.put(ObjectType.PlatformMissile, MapUtils.PI_OVER_FOUR);
    speeds.put(ObjectType.Pyro, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.Bomber, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.Class1Drone, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.Class2Drone, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.DefenseRobot, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.HeavyDriller, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.LightHulk, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.MediumHulk, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.MediumHulkCloaked, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.PlatformLaser, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.Spider, MapUtils.PI_OVER_TWO);
    speeds.put(ObjectType.AdvancedLifter, Math.PI);
    speeds.put(ObjectType.BabySpider, Math.PI);
    speeds.put(ObjectType.MediumLifter, Math.PI);
    speeds.put(ObjectType.SecondaryLifter, Math.PI);
    speeds.put(ObjectType.HomingMissile, Math.PI);
    speeds.put(ObjectType.MegaMissile, Math.PI);
    speeds.put(ObjectType.SmartPlasma, Math.PI);
    return speeds;
  }

  protected double move_speed;
  protected double turn_speed;
  protected Pilot pilot;
  protected double direction;
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
    move_speed = MAX_MOVE_SPEEDS.get(type);
    Double raw_turn_speed = MAX_TURN_SPEEDS.get(type);
    turn_speed = (raw_turn_speed != null ? raw_turn_speed : 0.0);
    this.pilot = pilot;
    this.direction = direction;
    pilot.bindToObject(this);
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
    pilot.bindToObject(this);
  }

  public void updateRoom(MapEngine engine, Room next_room) {
    engine.changeRooms(this, room, next_room);
    room = next_room;
    pilot.updateCurrentRoom(next_room);
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
    double previous_x_loc = x_loc;
    double previous_y_loc = y_loc;
    applyMovementActions(engine, s_elapsed);
    double unbounded_x_loc = x_loc;
    double unbounded_y_loc = y_loc;
    boundInsideAndUpdateRoom(engine, previous_x_loc, previous_y_loc);
    return unbounded_x_loc == x_loc && unbounded_y_loc == y_loc;
  }

  public void applyMovementActions(MapEngine engine, double s_elapsed) {
    if (next_action == null) {
      next_action = PilotAction.NO_ACTION;
    }
    applyMoveAction(s_elapsed);
    applyStrafeAction(s_elapsed);
    applyTurnAction(s_elapsed);
    next_action = null;
  }

  public void applyMoveAction(double s_elapsed) {
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
  }

  public void applyStrafeAction(double s_elapsed) {
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
  }

  public void applyTurnAction(double s_elapsed) {
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

  public void boundInsideAndUpdateRoom(MapEngine engine, double previous_x_loc, double previous_y_loc) {
    Point nw_corner = room.getNWCorner();
    Point se_corner = room.getSECorner();
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
        // make sure we do not go through the relevant walls in the neighbor Room
        handleHittingNeighborWall(RoomSide.NORTH, connection);
      }
      else {
        // hit the wall
        handleHittingWall(RoomSide.NORTH);
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
        handleHittingNeighborWall(RoomSide.SOUTH, connection);
      }
      else {
        handleHittingWall(RoomSide.SOUTH);
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
        handleHittingNeighborWall(RoomSide.WEST, connection);
      }
      else {
        handleHittingWall(RoomSide.WEST);
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
        handleHittingNeighborWall(RoomSide.EAST, connection);
      }
      else {
        handleHittingWall(RoomSide.EAST);
      }
    }

    if (next_room != null) {
      updateRoom(engine, next_room);
    }
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

  public boolean handleHittingNeighborWall(RoomSide wall_side, RoomConnection connection_to_neighbor) {
    if (wall_side.equals(RoomSide.NORTH) || wall_side.equals(RoomSide.SOUTH)) {
      double unbounded_x_loc = x_loc;
      x_loc =
              Math.min(Math.max(x_loc, connection_to_neighbor.min + radius), connection_to_neighbor.max -
                      radius);
      return unbounded_x_loc == x_loc;
    }
    else {
      double unbounded_y_loc = y_loc;
      y_loc =
              Math.min(Math.max(y_loc, connection_to_neighbor.min + radius), connection_to_neighbor.max -
                      radius);
      return unbounded_y_loc == y_loc;
    }
  }
}
