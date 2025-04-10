package websocket;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.ChessService;
import websocket.commands.*;
import websocket.messages.*;
import chess.*;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

  private final ConnectionManager connections = new ConnectionManager();
  private final ChessService chessService;
  private final Gson gson = new Gson();

  public WebSocketHandler(ChessService chessService) {
    this.chessService = chessService;
  }

  @OnWebSocketMessage
  public void onMessage(Session session, String message) throws IOException {
    UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

    String authToken = command.getAuthToken();
    Integer gameID = command.getGameID();

    if (authToken == null || authToken.isEmpty()) {
      sendError(session, "Error: Authentication token missing.");
      return;
    }

    AuthData authData;
    try {
      authData = chessService.getAuth(authToken);
      if (authData == null) {
        sendError(session, "Error: Unauthorized - Invalid auth token.");
        return;
      }
    } catch (DataAccessException e) {
      sendError(session, "Error: Server error during authentication.");
      return;
    }
    String username = authData.username();

    try {
      switch (command.getCommandType()) {
        case CONNECT -> connect(session, gson.fromJson(message, ConnectCommand.class), username);
        case MAKE_MOVE -> makeMove(session, gson.fromJson(message, MakeMoveCommand.class), username);
        case LEAVE -> leave(session, gson.fromJson(message, LeaveCommand.class), username);
        case RESIGN -> resign(session, gson.fromJson(message, ResignCommand.class), username);
        default -> sendError(session, "Error: Unknown command type.");
      }
    } catch (Exception e) {
      sendError(session, "Error: " + e.getMessage());
    }
  }

  private void connect(Session session, ConnectCommand command, String username)
      throws IOException, DataAccessException {
    connections.add(username, session, command.getGameID());

    GameData game = chessService.getGameData(command.getGameID());
    if (game == null) {
      sendError(session, "Error: Game not found.");
      connections.remove(username, command.getGameID());
      return;
    }

    String role = "observer";
    ChessGame.TeamColor playerColor = null;
    if (username.equals(game.whiteUsername())) {
      role = "player (WHITE)";
      playerColor = ChessGame.TeamColor.WHITE;
    } else if (username.equals(game.blackUsername())) {
      role = "player (BLACK)";
      playerColor = ChessGame.TeamColor.BLACK;
    }

    LoadGameMessage loadGameMsg = new LoadGameMessage(game);
    connections.sendToOne(username, gson.toJson(loadGameMsg));

    String notificationText = String.format("%s connected as %s.", username, role);
    NotificationMessage notificationMsg = new NotificationMessage(notificationText);
    connections.broadcast(username, command.getGameID(), gson.toJson(notificationMsg));
  }

  private void makeMove(Session session, MakeMoveCommand command, String username)
      throws IOException, DataAccessException, InvalidMoveException {
    GameData game = chessService.getGameData(command.getGameID());
    if (game == null) {
      throw new InvalidMoveException("Game not found.");
    }
    if (game.game().getTeamTurn() == null) {
      throw new InvalidMoveException("Game is already over.");
    }
    if (!username.equals(game.whiteUsername()) && !username.equals(game.blackUsername())) {
      throw new InvalidMoveException("Observers cannot make moves.");
    }

    GameData updatedGame = chessService.makeMove(command.getGameID(), command.getMove(), username);

    LoadGameMessage loadGameMsg = new LoadGameMessage(updatedGame);
    connections.broadcast("", command.getGameID(), gson.toJson(loadGameMsg));

    String moveNotation = formatMove(command.getMove());
    String notificationText = String.format("%s made move: %s", username, moveNotation);
    NotificationMessage notificationMsg = new NotificationMessage(notificationText);
    connections.broadcast(username, command.getGameID(), gson.toJson(notificationMsg));

    ChessGame.TeamColor opponentColor = updatedGame.game().getTeamTurn();
    if (opponentColor != null) {
      if (updatedGame.game().isInCheckmate(opponentColor)) {
        String checkmateMsgText = String.format("%s is in checkmate! %s wins!",
            opponentColor, username);
        NotificationMessage checkmateMsg = new NotificationMessage(checkmateMsgText);
        connections.broadcast("", command.getGameID(), gson.toJson(checkmateMsg));
      } else if (updatedGame.game().isInCheck(opponentColor)) {
        String checkMsgText = String.format("%s is in check!", opponentColor);
        NotificationMessage checkMsg = new NotificationMessage(checkMsgText);
        connections.broadcast("", command.getGameID(), gson.toJson(checkMsg));
      } else if (updatedGame.game().isInStalemate(opponentColor)) {
        String stalemateMsgText = String.format("Stalemate! The game is a draw.");
        NotificationMessage stalemateMsg = new NotificationMessage(stalemateMsgText);
        connections.broadcast("", command.getGameID(), gson.toJson(stalemateMsg));
      }
    }
  }

  private void leave(Session session, LeaveCommand command, String username) throws IOException, DataAccessException {
    chessService.leaveGame(command.getGameID(), username);

    connections.remove(username, command.getGameID());

    String notificationText = String.format("%s left the game.", username);
    NotificationMessage notificationMsg = new NotificationMessage(notificationText);
    connections.broadcast(username, command.getGameID(), gson.toJson(notificationMsg));
  }

  private void resign(Session session, ResignCommand command, String username)
      throws IOException, DataAccessException, InvalidMoveException {
    chessService.resignGame(command.getGameID(), username);

    String notificationText = String.format("%s resigned. The game is over.", username);
    NotificationMessage notificationMsg = new NotificationMessage(notificationText);
    connections.broadcast("", command.getGameID(), gson.toJson(notificationMsg));
  }

  private void sendError(Session session, String errorMessage) throws IOException {
    ErrorMessage errorMsg = new ErrorMessage(errorMessage);
    if (session.isOpen()) {
      session.getRemote().sendString(gson.toJson(errorMsg));
    }
  }

  private String formatMove(ChessMove move) {
    return String.format("%s to %s", move.getStartPosition().toString(), move.getEndPosition().toString());
  }
}