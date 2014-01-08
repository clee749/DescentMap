package pilot;

public class PilotAction {
  public final MoveDirection move;
  public final TurnDirection turn;
  public final boolean fire_cannon;

  public PilotAction() {
    this(MoveDirection.NONE, TurnDirection.NONE, false);
  }

  public PilotAction(MoveDirection move) {
    this(move, TurnDirection.NONE, false);
  }

  public PilotAction(TurnDirection turn) {
    this(MoveDirection.NONE, turn, false);
  }

  public PilotAction(MoveDirection move, TurnDirection turn) {
    this(move, turn, false);
  }

  public PilotAction(MoveDirection move, TurnDirection turn, boolean fire_cannon) {
    this.move = move;
    this.turn = turn;
    this.fire_cannon = fire_cannon;
  }
}
