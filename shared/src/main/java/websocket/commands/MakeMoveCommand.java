package websocket.commands;

import chess.ChessMove;

/**
 * Represents a command for a user to make a move in a game.
 */
public class MakeMoveCommand extends UserGameCommand {
  private final ChessMove move;

  /**
   * Constructs a MakeMoveCommand instance.
   *
   * @param authToken the user's authentication token.
   * @param gameID    the game identifier.
   * @param move      the chess move to be made.
   */
  public MakeMoveCommand(String authToken, Integer gameID, ChessMove move) {
    super(CommandType.MAKE_MOVE, authToken, gameID);
    this.move = move;
  }

  /**
   * Retrieves the chess move.
   *
   * @return the ChessMove object.
   */
  public ChessMove getMove() {
    return move;
  }
}