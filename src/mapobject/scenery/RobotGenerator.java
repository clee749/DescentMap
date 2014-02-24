package mapobject.scenery;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;

import mapobject.MapObject;
import mapobject.ephemeral.Zunggg;
import mapobject.unit.Pyro;
import resource.ImageHandler;
import structure.Room;
import util.RobotFactory;

import common.DescentMapException;
import common.ObjectType;
import common.RoomSide;
import component.MapEngine;

enum RobotGeneratorState {
  INACTIVE,
  WAIT_FOR_TRIGGER,
  SPAWN,
  COOLDOWN;
}


public class RobotGenerator extends Scenery {
  public static final int NUM_SPAWN_VOLLEYS = 3;
  public static final int NUM_ROBOTS_PER_VOLLEY = 5;
  public static final double SPAWN_COOLDOWN_TIME = 5.0;
  public static final double TIME_TO_SPAWN = Entrance.TIME_TO_SPAWN;
  public static final double ZUNGGG_TIME = Entrance.ZUNGGG_TIME;
  public static final double ROBOT_INACTIVE_TIME = ZUNGGG_TIME - TIME_TO_SPAWN;

  private final ObjectType robot_type;
  private final double spawn_direction;
  private final HashSet<Pyro> ignored_pyros;
  private int num_spawn_volleys_left;
  private int num_robots_left_in_volley;
  private RobotGeneratorState state;
  private double state_time_left;

  public RobotGenerator(Room room, double x_loc, double y_loc, ObjectType robot_type, RoomSide spawn_direction) {
    super(room, x_loc, y_loc);
    this.robot_type = robot_type;
    this.spawn_direction = RoomSide.directionToRadians(spawn_direction);
    ignored_pyros = new HashSet<Pyro>();
    num_spawn_volleys_left = NUM_SPAWN_VOLLEYS;
    state = RobotGeneratorState.WAIT_FOR_TRIGGER;
  }

  @Override
  public ObjectType getType() {
    return ObjectType.RobotGenerator;
  }

  @Override
  public void paint(Graphics2D g, ImageHandler images, Point ref_cell, Point ref_cell_nw_pixel,
          int pixels_per_cell) {
    super.paint(g, images, ref_cell, ref_cell_nw_pixel, pixels_per_cell);
    g.setColor(Color.magenta);
    g.drawString(String.valueOf(num_spawn_volleys_left * 10 + num_robots_left_in_volley), 200, 20);
    if (state_time_left > 0.0) {
      g.drawString(String.valueOf((int) state_time_left), 200, 40);
    }
  }

  @Override
  public void planNextAction(double s_elapsed) {
    state_time_left -= s_elapsed;
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    switch (state) {
      case INACTIVE:
        break;
      case WAIT_FOR_TRIGGER:
        HashSet<Pyro> pyros_in_room = room.getPyros();
        Iterator<Pyro> ignored_pyro_it = ignored_pyros.iterator();
        while (ignored_pyro_it.hasNext()) {
          Pyro ignored_pyro = ignored_pyro_it.next();
          if (!pyros_in_room.contains(ignored_pyro)) {
            ignored_pyro_it.remove();
          }
        }
        for (Pyro pyro_in_room : pyros_in_room) {
          if (ignored_pyros.contains(pyro_in_room)) {
            continue;
          }
          state = RobotGeneratorState.SPAWN;
          state_time_left = TIME_TO_SPAWN;
          num_robots_left_in_volley = NUM_ROBOTS_PER_VOLLEY;
          --num_spawn_volleys_left;
          return createZunggg(engine);
        }
        break;
      case SPAWN:
        if (state_time_left < 0.0) {
          state = RobotGeneratorState.COOLDOWN;
          state_time_left = SPAWN_COOLDOWN_TIME;
          --num_robots_left_in_volley;
          return RobotFactory.newRobot(robot_type, room, x_loc, y_loc, spawn_direction, ROBOT_INACTIVE_TIME);
        }
        break;
      case COOLDOWN:
        if (state_time_left < 0.0) {
          if (num_robots_left_in_volley < 1) {
            if (num_spawn_volleys_left < 1) {
              state = RobotGeneratorState.INACTIVE;
            }
            else {
              ignored_pyros.clear();
              for (Pyro pyro : room.getPyros()) {
                ignored_pyros.add(pyro);
              }
              state = RobotGeneratorState.WAIT_FOR_TRIGGER;
            }
          }
          else {
            state = RobotGeneratorState.SPAWN;
            state_time_left = TIME_TO_SPAWN;
            return createZunggg(engine);
          }
        }
        break;
      default:
        throw new DescentMapException("Unexpected RobotGeneratorState: " + state);
    }
    return null;
  }

  public Zunggg createZunggg(MapEngine engine) {
    playSound(engine, "effects/mtrl01.wav");
    return new Zunggg(room, x_loc, y_loc, ZUNGGG_TIME);
  }
}
