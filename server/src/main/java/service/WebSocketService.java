package service;

import chess.*;
import dataaccess.*;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import model.AuthData;
import model.GameData;

public class WebSocketService {
  private final AuthDAO authDAO;
  private final GameDAO gameDAO;

  public WebSocketService(AuthDAO authDAO, GameDAO gameDAO) {
    this.authDAO = authDAO;
    this.gameDAO = gameDAO;
  }

  public AuthData getAuth(String authToken) throws DataAccessException {
    if (authToken == null || authToken.isEmpty()) {
      return null;
    }
    return authDAO.getAuth(authToken);
  }

  public GameData getGameData(int gameID) throws DataAccessException {
    return gameDAO.getGame(gameID);
  }

  public GameData makeMove(int gameID, ChessMove move, String username)
      throws DataAccessException, InvalidMoveException {
    GameData gameData = gameDAO.getGame(gameID);
    if (gameData == null)
      throw new InvalidMoveException("Error: Game not found.");

    ChessGame game = gameData.game();
    if (game == null)
      throw new InvalidMoveException("Error: Game object is missing.");
    if (game.getTeamTurn() == null)
      throw new InvalidMoveException("Error: Game is already over.");

    ChessGame.TeamColor expectedColor = game.getTeamTurn();
    String expectedUsername = (expectedColor == ChessGame.TeamColor.WHITE) ? gameData.whiteUsername()
        : gameData.blackUsername();

    if (!username.equals(expectedUsername)) {
      throw new InvalidMoveException("Error: It's not your turn or you are not a player in this game.");
    }

    game.makeMove(move);

    gameDAO.updateGame(gameID, gameData);
    return gameData;
  }

  public void leaveGame(int gameID, String username) throws DataAccessException {
    GameData gameData = gameDAO.getGame(gameID);
    if (gameData != null) {
      GameData updatedGame = gameData;
      boolean changed = false;
      if (username.equals(gameData.whiteUsername())) {
        updatedGame = new GameData(gameID, null, gameData.blackUsername(), gameData.gameName(), gameData.game());
        changed = true;
      } else if (username.equals(gameData.blackUsername())) {
        updatedGame = new GameData(gameID, gameData.whiteUsername(), null, gameData.gameName(), gameData.game());
        changed = true;
      }

      if (changed) {
        gameDAO.updateGame(gameID, updatedGame);
      }
    }
  }

  public void resignGame(int gameID, String username) throws DataAccessException, InvalidMoveException {
    GameData gameData = gameDAO.getGame(gameID);
    if (gameData == null)
      throw new InvalidMoveException("Error: Game not found.");

    ChessGame game = gameData.game();
    if (game == null)
      throw new InvalidMoveException("Error: Game object is missing.");
    if (game.getTeamTurn() == null)
      throw new InvalidMoveException("Error: Game is already over.");

    if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
      throw new InvalidMoveException("Error: Observers cannot resign.");
    }

    game.setTeamTurn(null);

    gameDAO.updateGame(gameID, gameData);
  }
}