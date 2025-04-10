package websocket.messages;

import model.GameData; // Or just chess.ChessGame if you only send the game object

public class LoadGameMessage extends ServerMessage {
  private final GameData game; // Or just ChessGame game;

  public LoadGameMessage(GameData game) {
    super(ServerMessageType.LOAD_GAME);
    this.game = game;
  }

  public GameData getGame() {
    return game;
  }
}