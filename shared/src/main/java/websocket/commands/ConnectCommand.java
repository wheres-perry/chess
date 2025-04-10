package websocket.commands;

/**
 * Represents a command to connect a user to a game.
 */
public class ConnectCommand extends UserGameCommand {
  /**
   * Constructs a ConnectCommand instance.
   *
   * @param authToken the user's authentication token.
   * @param gameID    the game identifier.
   */
  public ConnectCommand(String authToken, Integer gameID) {
    super(CommandType.CONNECT, authToken, gameID);
  }
}