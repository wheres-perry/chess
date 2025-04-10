package websocket;

import com.google.gson.Gson;

import chess.InvalidMoveException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import exception.ResponseException;
import service.ChessService;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import webSocketMessages.Action;
import webSocketMessages.Notification;

import java.io.IOException;
import java.util.Timer;

@WebSocket
public class WebSocketHandler {

  private final ChessService chessService;
  private final Gson gson = new Gson();

  private final ConnectionManager connections = new ConnectionManager();

  public WebSocketHandler(ChessService chessService) {
    this.chessService = chessService;
  }

  @OnWebSocketMessage
  public void onMessage(Session session, String message) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  private void enter(String visitorName, Session session) throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  private void exit(String visitorName) throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  private void connect(Session session, ConnectCommand command, String username) throws DataAccessException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  private void makeMove(Session session, MakeMoveCommand command, String username)
      throws DataAccessException, InvalidMoveException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  private void resign(Session session, ResignCommand command, String username)
      throws DataAccessException, InvalidMoveException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  private void leave(Session session, LeaveCommand command, String username) throws DataAccessException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  private void sendError(Session session, String errorMessage) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  private void loadGame(Session session, String errorMessage) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  private void sendNotification(Session session, Notification notification) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}