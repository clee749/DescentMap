package pilot;

public class PilotAction {
  public static final PilotAction NO_ACTION = new PilotAction();
  public static final PilotAction MOVE_FORWARD = new PilotAction(MoveDirection.FORWARD);

  public final MoveDirection move;
  public final StrafeDirection strafe;
  public final TurnDirection turn;
  public final boolean fire_cannon;
  public final boolean fire_secondary;
  public final boolean drop_bomb;

  public PilotAction() {
    this(MoveDirection.NONE, StrafeDirection.NONE, TurnDirection.NONE, false, false, false);
  }

  public PilotAction(MoveDirection move) {
    this(move, StrafeDirection.NONE, TurnDirection.NONE, false, false, false);
  }

  public PilotAction(StrafeDirection strafe) {
    this(MoveDirection.NONE, strafe, TurnDirection.NONE, false, false, false);
  }

  public PilotAction(MoveDirection move, StrafeDirection strafe) {
    this(move, strafe, TurnDirection.NONE, false, false, false);
  }

  public PilotAction(MoveDirection move, TurnDirection turn) {
    this(move, StrafeDirection.NONE, turn, false, false, false);
  }

  public PilotAction(StrafeDirection strafe, TurnDirection turn) {
    this(MoveDirection.NONE, strafe, turn, false, false, false);
  }

  public PilotAction(MoveDirection move, StrafeDirection strafe, TurnDirection turn) {
    this(move, strafe, turn, false, false, false);
  }

  public PilotAction(MoveDirection move, TurnDirection turn, boolean fire_cannon) {
    this(move, StrafeDirection.NONE, turn, fire_cannon, false, false);
  }

  public PilotAction(StrafeDirection strafe, TurnDirection turn, boolean fire_cannon) {
    this(MoveDirection.NONE, strafe, turn, fire_cannon, false, false);
  }

  public PilotAction(MoveDirection move, StrafeDirection strafe, TurnDirection turn, boolean fire_cannon) {
    this(move, strafe, turn, fire_cannon, false, false);
  }

  public PilotAction(MoveDirection move, StrafeDirection strafe, TurnDirection turn, boolean fire_cannon,
          boolean fire_secondary, boolean drop_bomb) {
    this.move = (move != null ? move : MoveDirection.NONE);
    this.strafe = (strafe != null ? strafe : StrafeDirection.NONE);
    this.turn = (turn != null ? turn : TurnDirection.NONE);
    this.fire_cannon = fire_cannon;
    this.fire_secondary = fire_secondary;
    this.drop_bomb = drop_bomb;
  }
}
