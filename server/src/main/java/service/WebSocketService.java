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
  }

  public AuthData getAuth(String authToken) throws DataAccessException {
    return null;
  }

  public GameData getGameData(int gameID) throws DataAccessException {
    return null;
  }

  public GameData makeMove(int gameID, ChessMove move, String username)
      throws DataAccessException, InvalidMoveException {
    return null;
  }

  public void leaveGame(int gameID, String username) throws DataAccessException {
  }

  public void resignGame(int gameID, String username) throws DataAccessException, InvalidMoveException {
  }
}