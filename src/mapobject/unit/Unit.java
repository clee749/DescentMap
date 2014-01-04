package mapobject.unit;

import gunner.Gunner;
import mapobject.MapObject;
import mapobject.TurnableMapObject;
import mapobject.shot.LaserShot;
import mapobject.shot.Shot;
import pilot.Pilot;
import structure.Room;

import common.Constants;
import common.ObjectType;
import component.MapEngine;

public abstract class Unit extends TurnableMapObject {
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
    this.gunner = gunner;
    reload_time = Constants.getReloadTime(type);
    shots_per_volley = Constants.getShotsPerVolley(type);
    volley_reload_time = Constants.getVolleyReloadTime(type);
  }

  @Override
  public void computeNextStep(double s_elapsed) {
    super.computeNextStep(s_elapsed);
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

  public Shot fireCannon() {
    return new LaserShot(this, Constants.getRadius(ObjectType.LaserShot), room, x_loc, y_loc, direction, 1);
  }
}
