package websocket.messages;

import model.GameData;

/**
 * Represents a message that loads a game state from the server.
 */
public class LoadGameMessage extends ServerMessage {
  private final GameData game;

  /**
   * Constructs a LoadGameMessage instance.
   *
   * @param game the GameData object containing the game state to be loaded.
   */
  public LoadGameMessage(GameData game) {
    super(ServerMessageType.LOAD_GAME);
    this.game = game;
  }

  /**
   * Retrieves the game data.
   *
   * @return the GameData object associated with this message.
   */
  public GameData getGame() {
    return game;
  }
}