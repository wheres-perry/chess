package websocket.commands;

/**
 * Represents a command for a user to leave a game.
 */
public class LeaveCommand extends UserGameCommand {
  /**
   * Constructs a LeaveCommand instance.
   *
   * @param authToken the user's authentication token.
   * @param gameID    the game identifier.
   */
  public LeaveCommand(String authToken, Integer gameID) {
    super(CommandType.LEAVE, authToken, gameID);
  }
}