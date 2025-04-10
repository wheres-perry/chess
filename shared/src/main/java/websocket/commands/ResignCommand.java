package websocket.commands;

/**
 * Represents a command for a user to resign from a game.
 */
public class ResignCommand extends UserGameCommand {
  /**
   * Constructs a ResignCommand instance.
   *
   * @param authToken the user's authentication token.
   * @param gameID    the game identifier.
   */
  public ResignCommand(String authToken, Integer gameID) {
    super(CommandType.RESIGN, authToken, gameID);
  }
}