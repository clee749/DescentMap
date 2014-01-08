package mapobject.unit;

import gunner.Gunner;

import java.awt.geom.Point2D;

import mapobject.MapObject;
import mapobject.MovableObject;
import pilot.Pilot;
import structure.Room;
import util.MapUtils;

import common.Constants;
import component.MapEngine;

public abstract class Unit extends MovableObject {
  protected final double cannon_offset;
  protected Gunner gunner;
  protected double reload_time;
  protected int shots_per_volley;
  protected double reload_time_left;
  protected double shots_left_in_volley;
  protected double volley_reload_time;
  protected double volley_reload_time_left;
  protected boolean firing_volley;

  public Unit(Pilot pilot, Gunner gunner, Room room, double x_loc, double y_loc, double direction) {
    super(pilot, room, x_loc, y_loc, direction);
    cannon_offset = Constants.getCannonOffset(type) * radius;
    this.gunner = gunner;
    reload_time = Constants.getReloadTime(type);
    shots_per_volley = Constants.getShotsPerVolley(type);
    volley_reload_time = Constants.getVolleyReloadTime(type);
  }

  @Override
  public void planNextStep(double s_elapsed) {
    super.planNextStep(s_elapsed);
    if (reload_time_left < 0.0) {
      planToFireCannonVolley();
    }
    else {
      handleCannonReload(s_elapsed);
    }
  }

  @Override
  public MapObject doNextStep(MapEngine engine, double s_elapsed) {
    MapObject movement_object_created = super.doNextStep(engine, s_elapsed);
    if (firing_volley) {
      if (volley_reload_time_left < 0.0) {
        --shots_left_in_volley;
        if (shots_left_in_volley < 1) {
          firing_volley = false;
        }
        else {
          volley_reload_time_left = volley_reload_time;
        }
        return fireCannon();
      }
      else {
        handleCannonVolleyReload(s_elapsed);
      }
    }
    return movement_object_created;
  }

  public void handleCannonReload(double s_elapsed) {
    reload_time_left -= s_elapsed;
  }

  public void handleCannonVolleyReload(double s_elapsed) {
    volley_reload_time_left -= s_elapsed;
  }

  public void planToFireCannonVolley() {
    firing_volley = true;
    reload_time_left = reload_time;
    shots_left_in_volley = shots_per_volley;
    volley_reload_time_left = -1.0;
  }

  public Point2D.Double findRightShotAbsOffset(double offset) {
    double axis = direction + MapUtils.PI_OVER_TWO;
    return new Point2D.Double(Math.cos(axis) * offset, Math.sin(axis) * offset);
  }

  public abstract MapObject fireCannon();
}
