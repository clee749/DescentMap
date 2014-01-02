package pilot;

public class PilotMove {
  private MoveDirection move;
  private TurnDirection turn;

  public PilotMove() {
    this(null, null);
  }

  public PilotMove(MoveDirection move, TurnDirection turn) {
    this.move = move;
    this.turn = turn;
  }

  public MoveDirection getMove() {
    return move;
  }

  public void setMove(MoveDirection move) {
    this.move = move;
  }

  public TurnDirection getTurn() {
    return turn;
  }

  public void setTurn(TurnDirection turn) {
    this.turn = turn;
  }
}
