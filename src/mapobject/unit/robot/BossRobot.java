package mapobject.unit.robot;

import java.awt.Image;

import mapobject.MapObject;
import pilot.BossRobotPilot;
import pilot.TeleportLocation;
import resource.ImageHandler;
import structure.LockedDoor;
import structure.Room;
import cannon.Cannon;

import common.DescentMapException;
import component.MapEngine;

enum BossRobotState {
  NORMAL,
  PREPARE_TO_TELEPORT,
  RECOVER_FROM_TELEPORT;
}


public abstract class BossRobot extends Robot {
  public static final double TELEPORT_CLOAK_TIME = 2.0;
  public static final double TELEPORT_COOLDOWN = 30.0;

  protected final String cloaked_image_name;
  protected final LockedDoor exit_door;
  protected BossRobotState state;
  protected double state_time_left;
  protected boolean init_teleport;

  public BossRobot(double radius, Cannon cannon, LockedDoor exit_door, Room room, double x_loc, double y_loc,
          double direction) {
    super(radius, new BossRobotPilot(), cannon, room, x_loc, y_loc, direction);
    cloaked_image_name = image_name + "Cloaked";
    this.exit_door = exit_door;
    state = BossRobotState.NORMAL;
    enableDeathSpin();
  }

  @Override
  public boolean isHomable() {
    return false;
  }

  @Override
  public Image getImage(ImageHandler images) {
    if (state.equals(BossRobotState.NORMAL)) {
      return super.getImage(images);
    }
    return images.getImage(cloaked_image_name, direction);
  }

  public void startTeleportSequence() {
    if (state.equals(BossRobotState.NORMAL) && state_time_left < 0.0) {
      init_teleport = true;
    }
  }

  @Override
  public MapObject doNextAction(MapEngine engine, double s_elapsed) {
    updateState(engine, s_elapsed);
    return super.doNextAction(engine, s_elapsed);
  }

  public void updateState(MapEngine engine, double s_elapsed) {
    switch (state) {
      case NORMAL:
        if (init_teleport) {
          init_teleport = false;
          state = BossRobotState.PREPARE_TO_TELEPORT;
          state_time_left = TELEPORT_CLOAK_TIME;
        }
        break;
      case PREPARE_TO_TELEPORT:
        if (state_time_left < 0.0) {
          TeleportLocation location = ((BossRobotPilot) pilot).findTeleportLocation(engine.getMap());
          if (location != null) {
            x_loc = location.x_loc;
            y_loc = location.y_loc;
            updateRoom(engine, location.room);
          }
          state = BossRobotState.RECOVER_FROM_TELEPORT;
          state_time_left = TELEPORT_CLOAK_TIME;
        }
        break;
      case RECOVER_FROM_TELEPORT:
        if (state_time_left < 0.0) {
          state = BossRobotState.NORMAL;
          state_time_left = TELEPORT_COOLDOWN;
        }
        break;
      default:
        throw new DescentMapException("Unexpected BossRobotState: " + state);
    }
  }

  @Override
  public MapObject doNextDeathSpinAction(MapEngine engine, double s_elapsed) {
    if (!death_spin_started) {
      state = BossRobotState.NORMAL;
    }
    return super.doNextDeathSpinAction(engine, s_elapsed);
  }

  @Override
  public void handleCooldowns(double s_elapsed) {
    super.handleCooldowns(s_elapsed);
    state_time_left -= s_elapsed;
  }

  @Override
  public void tempDisableAfterDamage(int amount) {

  }

  @Override
  public MapObject handleDeath(MapEngine engine, double s_elapsed) {
    MapObject created_object = super.handleDeath(engine, s_elapsed);
    if (!is_in_map) {
      exit_door.base_room.addNeighbor(exit_door.direction_from_base, exit_door.connection);
    }
    return created_object;
  }
}
