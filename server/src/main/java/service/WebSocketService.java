package service;

import chess.*;
import dataaccess.*;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import model.AuthData;
import model.GameData;

/**
 * Service layer dedicated to handling logic specific to WebSocket game
 * interactions.
 */
public class WebSocketService {
  private final AuthDAO authDAO;
  private final GameDAO gameDAO;

  /**
   * Constructor requiring necessary DAO implementations.
   * 
   * @param authDAO Authentication Data Access Object.
   * @param gameDAO Game Data Access Object.
   */
  public WebSocketService(AuthDAO authDAO, GameDAO gameDAO) {
    this.authDAO = authDAO;
    this.gameDAO = gameDAO;
  }

  /**
   * Retrieves authentication data for a given token.
   * 
   * @param authToken The token to validate.
   * @return AuthData if valid, null otherwise.
   * @throws DataAccessException If database error occurs.
   */
  public AuthData getAuth(String authToken) throws DataAccessException {
    if (authToken == null || authToken.isEmpty()) {
      return null;
    }
    return authDAO.getAuth(authToken);
  }

  /**
   * Retrieves game data for a given game ID.
   * 
   * @param gameID The ID of the game to retrieve.
   * @return GameData if found, null otherwise.
   * @throws DataAccessException If database error occurs.
   */
  public GameData getGameData(int gameID) throws DataAccessException {
    return gameDAO.getGame(gameID);
  }

  /**
   * Makes a move in the specified game.
   * Validates the move, updates the game state, and persists the changes.
   * 
   * @param gameID   The game ID.
   * @param move     The move to make.
   * @param username The username of the player making the move (for validation).
   * @return The updated GameData.
   * @throws DataAccessException  If database error occurs.
   * @throws InvalidMoveException If the move is illegal, out of turn, or the game
   *                              is over.
   */
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

  /**
   * Removes a player from a game when they leave.
   * Updates the GameData in the database if a player (not observer) leaves.
   * 
   * @param gameID   The game ID.
   * @param username The username of the user leaving.
   * @throws DataAccessException If database error occurs.
   */
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

  /**
   * Marks a game as over due to resignation.
   * Validates that the resigner is a player and the game is not already over.
   * Updates the game state in the database.
   * 
   * @param gameID   The game ID.
   * @param username The username of the player resigning.
   * @throws DataAccessException  If database error occurs.
   * @throws InvalidMoveException If the user is not a player, game not found, or
   *                              the game is already over.
   */
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