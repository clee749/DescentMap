package pilot;

public class PilotMove {
  public final MoveDirection move;
  public final TurnDirection turn;

  public PilotMove() {
    this(null, null);
  }

  public PilotMove(MoveDirection move, TurnDirection turn) {
    this.move = move;
    this.turn = turn;
  }
}
