package websocket;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.*;
import websocket.messages.*;

import java.io.IOException;

/**
 * Handles WebSocket connections and routes chess game commands between clients
 * and the server.
 */
@WebSocket
public class WebSocketHandler {

  private final GameDAO gameDAO;
  private final ClientManager clientManager = new ClientManager();
  private final Gson serializer = new Gson();

  /**
   * Constructs a WebSocketHandler with the given DAOs.
   */
  public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
    this.gameDAO = gameDAO;
  }

  /**
   * Called when a new WebSocket connection is established.
   */
  @OnWebSocketConnect
  public void onConnect(Session session) {
    System.out.println("WebSocket connected: " + session.getRemoteAddress());
  }

  /**
   * Handles a client request to connect to a game.
   */
  private void handleConnect(Session session, ConnectCommand command, AuthData authData)
      throws DataAccessException, IOException {
    GameData gameData = gameDAO.getGame(command.getGameID());
    if (gameData == null) {
      sendError(session, "Error: Invalid Game ID " + command.getGameID());
      return;
    }
    clientManager.register(command.getGameID(), authData.username(), session);
    LoadGameMessage loadGameMsg = new LoadGameMessage(gameData);
    sendDirectMessage(session, loadGameMsg);
    String role = "observer";
    if (authData.username().equals(gameData.whiteUsername())) {
      role = "WHITE player";
    } else if (authData.username().equals(gameData.blackUsername())) {
      role = "BLACK player";
    }
    String notificationText = String.format("%s joined the game as %s.", authData.username(), role);
    NotificationMessage notificationMsg = new NotificationMessage(notificationText);
    clientManager.notifyMatch(command.getGameID(), authData.username(), notificationMsg);
  }

  /**
   * Handles a client request to make a move in a game.
   */
  private void handleMakeMove(Session session, MakeMoveCommand command, AuthData authData)
      throws DataAccessException, IOException {
    GameData gameData = gameDAO.getGame(command.getGameID());
    if (gameData == null) {
      sendError(session, "Error: Invalid Game ID.");
      return;
    }
    ChessGame game = gameData.game();
    if (game.getTeamTurn() == null) {
      sendError(session, "Error: Game is already over.");
      return;
    }
    ChessGame.TeamColor playerColor = null;
    if (authData.username().equals(gameData.whiteUsername())) {
      playerColor = ChessGame.TeamColor.WHITE;
    } else if (authData.username().equals(gameData.blackUsername())) {
      playerColor = ChessGame.TeamColor.BLACK;
    }
    if (playerColor == null) {
      sendError(session, "Error: Observers cannot make moves.");
      return;
    }
    if (game.getTeamTurn() != playerColor) {
      sendError(session, "Error: It's not your turn.");
      return;
    }
    try {
      game.makeMove(command.getMove());
      gameDAO.updateGame(gameData.gameID(), gameData);
      LoadGameMessage loadGameMsg = new LoadGameMessage(gameData);
      String moveNotation = command.getMove().toString();
      String notificationText = String.format("%s played %s.", authData.username(), moveNotation);
      NotificationMessage notificationMsg = new NotificationMessage(notificationText);
      clientManager.notifyMatch(command.getGameID(), null, loadGameMsg);
      clientManager.notifyMatch(command.getGameID(), authData.username(), notificationMsg);
      handlePostMoveChecks(command.getGameID(), game);
    } catch (InvalidMoveException e) {
      sendError(session, "Error: Invalid move - " + e.getMessage());
    }
  }

  /**
   * Checks the game state after a move and notifies players if the game is in
   * check, checkmate, or stalemate.
   */
  private void handlePostMoveChecks(Integer gameID, ChessGame game) throws IOException {
    ChessGame.TeamColor currentTurn = game.getTeamTurn();
    if (currentTurn == null)
      return;
    String notificationText = null;
    if (game.isInCheckmate(currentTurn)) {
      notificationText = String.format("Checkmate! %s wins.", currentTurn.not());
    } else if (game.isInStalemate(currentTurn)) {
      notificationText = "Stalemate! The game is a draw.";
    } else if (game.isInCheck(currentTurn)) {
      notificationText = String.format("%s is in check.", currentTurn);
    }
    if (notificationText != null) {
      NotificationMessage checkNotification = new NotificationMessage(notificationText);
      clientManager.notifyMatch(gameID, null, checkNotification);
    }
  }

  /**
   * Handles a client request to leave a game.
   */
  private void handleLeave(Session session, LeaveCommand command, AuthData authData)
      throws DataAccessException, IOException {
    GameData gameData = gameDAO.getGame(command.getGameID());
    if (gameData == null) {
      clientManager.unregister(authData.username());
      return;
    }
    String username = authData.username();
    GameData updatedGameData = gameData;
    if (username.equals(gameData.whiteUsername())) {
      updatedGameData = new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(),
          gameData.game());
      gameDAO.updateGame(updatedGameData.gameID(), updatedGameData);
    } else if (username.equals(gameData.blackUsername())) {
      updatedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(),
          gameData.game());
      gameDAO.updateGame(updatedGameData.gameID(), updatedGameData);
    }
    String notificationText = String.format("%s left the game.", username);
    NotificationMessage notificationMsg = new NotificationMessage(notificationText);
    clientManager.notifyMatch(command.getGameID(), username, notificationMsg);
    clientManager.unregister(username);
  }

  /**
   * Handles a client request to resign from a game.
   */
  private void handleResign(Session session, ResignCommand command, AuthData authData)
      throws DataAccessException, IOException {
    GameData gameData = gameDAO.getGame(command.getGameID());
    if (gameData == null) {
      sendError(session, "Error: Game not found.");
      return;
    }
    ChessGame game = gameData.game();
    if (game.getTeamTurn() == null) {
      sendError(session, "Error: Cannot resign, game is already over.");
      return;
    }
    String username = authData.username();
    if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
      sendError(session, "Error: Observers cannot resign.");
      return;
    }
    game.setTeamTurn(null);
    gameDAO.updateGame(command.getGameID(), gameData);
    String notificationText = String.format("%s resigned. The game is over.", username);
    NotificationMessage notificationMsg = new NotificationMessage(notificationText);
    clientManager.notifyMatch(command.getGameID(), null, notificationMsg);
  }

  /**
   * Handles cleanup and notifications when a client disconnects.
   */
  private void handleDisconnect(Session session) {
    ClientLink removedLink = clientManager.unregisterBySession(session);
    if (removedLink != null) {
      String username = removedLink.participantName;
      Integer gameID = removedLink.matchID;
      if (gameID != null && username != null) {
        try {
          GameData gameData = gameDAO.getGame(gameID);
          if (gameData != null) {
            GameData updatedGameData = gameData;
            if (username.equals(gameData.whiteUsername())) {
              updatedGameData = new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(),
                  gameData.game());
              gameDAO.updateGame(updatedGameData.gameID(), updatedGameData);
            } else if (username.equals(gameData.blackUsername())) {
              updatedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(),
                  gameData.game());
              gameDAO.updateGame(updatedGameData.gameID(), updatedGameData);
            }
            String notificationText = String.format("%s disconnected.", username);
            NotificationMessage notificationMsg = new NotificationMessage(notificationText);
            clientManager.notifyMatch(gameID, username, notificationMsg);
          }
        } catch (DataAccessException | IOException e) {
          System.err.println(
              "Error during disconnect handling for user " + username + " in game " + gameID + ": " + e.getMessage());
          e.printStackTrace();
        }
      }
    } else {
      System.out.println("Disconnected session was not registered in ClientManager.");
    }
  }

  /**
   * Sends a message directly to a client session.
   */
  private void sendDirectMessage(Session session, ServerMessage message) throws IOException {
    if (session.isOpen()) {
      String messageJson = serializer.toJson(message);
      session.getRemote().sendString(messageJson);
    }
  }

  /**
   * Sends an error message to a client session.
   */
  private void sendError(Session session, String errorMessage) throws IOException {
    ErrorMessage error = new ErrorMessage(errorMessage);
    sendDirectMessage(session, error);
  }
}