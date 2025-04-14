package serverConnection;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import model.GameData;
import websocket.commands.*;
import websocket.messages.*;

/**
 * Manages the WebSocket connection for the chess client.
 * Handles sending UserGameCommands and receiving ServerMessages.
 * Includes a dummy implementation for local testing.
 */
public class WebSocketClient {

  private final String serverUrl;
  private WebSocketListener messageListener;
  private final Gson gson = new Gson();
  private boolean isConnected = false;

  private boolean simulateServer = true;
  private String dummyAuthToken = null;
  private Integer dummyGameID = null;
  private String dummyUsername = null;
  private ChessGame dummyGame = null;

  public WebSocketClient(String serverUrl, WebSocketListener listener) {
    this.serverUrl = serverUrl.replaceFirst("^http", "ws") + "/ws";
    this.messageListener = listener;
    System.out.println("WebSocket Client Initialized for URL (dummy): " + this.serverUrl);
  }

  /**
   * Interface for components that need to react to WebSocket messages.
   */
  public interface WebSocketListener {
    void onMessageReceived(ServerMessage message);

    void onError(String errorMessage);
  }

  public void connect(String authToken, Integer gameID, String username) throws Exception {
    if (simulateServer) {
      this.isConnected = true;
      this.dummyAuthToken = authToken;
      this.dummyGameID = gameID;
      this.dummyUsername = username;
      this.dummyGame = new ChessGame();
      this.dummyGame.getBoard().resetBoard();
      System.out.println("[WebSocketClient-Dummy] Simulating connection for game " + gameID);
      sendCommand(new ConnectCommand(authToken, gameID));
    } else {
      throw new UnsupportedOperationException("Actual WebSocket connection not implemented yet.");
    }
  }

  public void disconnect() throws Exception {
    if (simulateServer) {
      if (isConnected) {
        this.isConnected = false;
        this.dummyAuthToken = null;
        this.dummyGameID = null;
        this.dummyUsername = null;
        this.dummyGame = null;
        System.out.println("[WebSocketClient-Dummy] Simulating disconnection.");
      }
    } else {
      throw new UnsupportedOperationException("Actual WebSocket disconnection not implemented yet.");
    }
  }

  public boolean isConnected() {
    return isConnected;
  }

  public void sendCommand(UserGameCommand command) throws Exception {
    if (!isConnected) {
      throw new Exception("Not connected to WebSocket server.");
    }
    if (simulateServer && command.getAuthToken() != null && dummyAuthToken != null
        && !command.getAuthToken().equals(dummyAuthToken)) {
      System.err.println("[WebSocketClient-Dummy] Auth token mismatch! Command: " + command.getCommandType()
          + ", Expected: " + dummyAuthToken + ", Got: " + command.getAuthToken());
      messageListener.onError("Simulated Error: Invalid authentication token.");
      return;
    }
    if (simulateServer && command.getGameID() != null && dummyGameID != null
        && !command.getGameID().equals(dummyGameID)) {
      System.err.println("[WebSocketClient-Dummy] Game ID mismatch! Command: " + command.getCommandType()
          + ", Expected: " + dummyGameID + ", Got: " + command.getGameID());
      messageListener.onError("Simulated Error: Invalid game ID.");
      return;
    }

    String jsonCommand = gson.toJson(command);
    if (simulateServer) {
      System.out.println("[WebSocketClient-Dummy] Sending command: " + jsonCommand);
      simulateServerResponse(command);
    } else {
      throw new UnsupportedOperationException("Actual WebSocket send not implemented yet.");
    }
  }

  public void sendConnect(String authToken, Integer gameID) throws Exception {
    sendCommand(new ConnectCommand(authToken, gameID));
  }

  public void sendMakeMove(String authToken, Integer gameID, chess.ChessMove move) throws Exception {
    sendCommand(new MakeMoveCommand(authToken, gameID, move));
  }

  public void sendLeave(String authToken, Integer gameID) throws Exception {
    sendCommand(new LeaveCommand(authToken, gameID));
  }

  public void sendResign(String authToken, Integer gameID) throws Exception {
    sendCommand(new ResignCommand(authToken, gameID));
  }

  /** Simulate receiving a message from the server (for testing UI) */
  public void simulateReceiveMessage(String jsonMessage) {
    if (simulateServer && messageListener != null) {
      try {
        System.out.println("[WebSocketClient-Dummy] Simulating received message: " + jsonMessage);
        ServerMessage genericMessage = gson.fromJson(jsonMessage, ServerMessage.class);
        ServerMessage specificMessage = null;

        switch (genericMessage.getServerMessageType()) {
          case LOAD_GAME:
            specificMessage = gson.fromJson(jsonMessage, LoadGameMessage.class);
            if (specificMessage instanceof LoadGameMessage) {
              GameData loadedGameData = ((LoadGameMessage) specificMessage).getGame();
              if (loadedGameData != null && loadedGameData.game() != null) {
                this.dummyGame = loadedGameData.game();
                System.out.println("[WebSocketClient-Dummy] Updated local dummy game state.");
              }
            }
            break;
          case ERROR:
            specificMessage = gson.fromJson(jsonMessage, ErrorMessage.class);
            break;
          case NOTIFICATION:
            specificMessage = gson.fromJson(jsonMessage, NotificationMessage.class);
            break;
          default:
            System.err.println("[WebSocketClient-Dummy] Unknown server message type in simulateReceiveMessage: "
                + genericMessage.getServerMessageType());
            messageListener.onError("Unknown server message type received: " + genericMessage.getServerMessageType());
            return;
        }
        messageListener.onMessageReceived(specificMessage);
      } catch (Exception e) {
        System.err.println("[WebSocketClient-Dummy] Error simulating received message: " + e.getMessage());
        e.printStackTrace();
        if (messageListener != null) {
          messageListener.onError("Failed to parse simulated server message: " + e.getMessage());
        }
      }
    }
  }

  /** Simulate server responses based on the command sent */
  private void simulateServerResponse(UserGameCommand command) {
    if (dummyGame == null && command.getCommandType() != UserGameCommand.CommandType.CONNECT) {
      System.err.println("[WebSocketClient-Dummy] Cannot simulate response, dummy game state is null.");
      simulateReceiveMessage(gson.toJson(new ErrorMessage("Internal simulation error: Game not initialized")));
      return;
    }

    switch (command.getCommandType()) {
      case CONNECT:
        GameData initialGameData = new GameData(dummyGameID, "dummyWhite", "dummyBlack", "Dummy Game " + dummyGameID,
            dummyGame);
        LoadGameMessage loadMsg = new LoadGameMessage(initialGameData);
        simulateReceiveMessage(gson.toJson(loadMsg));

        String connectNotificationText = String.format("%s connected to the game.",
            dummyUsername != null ? dummyUsername : "A user");
        NotificationMessage connectNotifyMsg = new NotificationMessage(connectNotificationText);
        simulateReceiveMessage(gson.toJson(connectNotifyMsg));
        break;

      case MAKE_MOVE:
        MakeMoveCommand moveCmd = (MakeMoveCommand) command;
        ChessMove move = moveCmd.getMove();
        String moveStr = formatMove(move);

        try {
          ChessPiece piece = dummyGame.getBoard().getPiece(move.getStartPosition());
          if (piece == null)
            throw new InvalidMoveException("No piece at start");
          if (piece.getTeamColor() != dummyGame.getTeamTurn())
            throw new InvalidMoveException("Not your turn");

          if (!isValidDummyMove(move)) {
            throw new InvalidMoveException("Simulated invalid move");
          }

          dummyGame.makeMove(move);

          GameData updatedGameData = new GameData(dummyGameID, "dummyWhite", "dummyBlack", "Dummy Game " + dummyGameID,
              dummyGame);
          LoadGameMessage updateLoadMsg = new LoadGameMessage(updatedGameData);
          simulateReceiveMessage(gson.toJson(updateLoadMsg));

          String moveNotificationText = String.format("%s made move %s",
              dummyUsername != null ? dummyUsername : "A player", moveStr);
          NotificationMessage moveNotifyMsg = new NotificationMessage(moveNotificationText);
          simulateReceiveMessage(gson.toJson(moveNotifyMsg));

          if (dummyGame.isInCheckmate(dummyGame.getTeamTurn())) {
            String checkmateText = String.format("%s is in checkmate! %s wins!", dummyGame.getTeamTurn(),
                dummyGame.getTeamTurn().not());
            simulateReceiveMessage(gson.toJson(new NotificationMessage(checkmateText)));
          } else if (dummyGame.isInCheck(dummyGame.getTeamTurn())) {
            String checkText = String.format("%s is in check!", dummyGame.getTeamTurn());
            simulateReceiveMessage(gson.toJson(new NotificationMessage(checkText)));
          }

        } catch (InvalidMoveException e) {
          String errorText = "Invalid Move: " + e.getMessage();
          ErrorMessage errorMsg = new ErrorMessage(errorText);
          simulateReceiveMessage(gson.toJson(errorMsg));
        }
        break;

      case LEAVE:
        String leaveNotificationText = String.format("%s left the game.",
            dummyUsername != null ? dummyUsername : "A player");
        NotificationMessage leaveNotifyMsg = new NotificationMessage(leaveNotificationText);
        simulateReceiveMessage(gson.toJson(leaveNotifyMsg));

        try {
          disconnect();
        } catch (Exception e) {
          /* ignore */ }
        break;

      case RESIGN:
        if (dummyGame.getTeamTurn() == null) {
          simulateReceiveMessage(gson.toJson(new ErrorMessage("Game has already concluded.")));
          return;
        }

        String resignNotificationText = String.format("%s resigned the game. %s wins!",
            dummyUsername != null ? dummyUsername : "A player",
            dummyGame.getTeamTurn().not());
        NotificationMessage resignNotifyMsg = new NotificationMessage(resignNotificationText);
        simulateReceiveMessage(gson.toJson(resignNotifyMsg));

        dummyGame.setTeamTurn(null);

        GameData finalGameData = new GameData(dummyGameID, "dummyWhite", "dummyBlack", "Dummy Game " + dummyGameID,
            dummyGame);
        LoadGameMessage finalLoadMsg = new LoadGameMessage(finalGameData);
        simulateReceiveMessage(gson.toJson(finalLoadMsg));

        break;

      default:
        System.out
            .println("[WebSocketClient-Dummy] No specific simulation for command type: " + command.getCommandType());
        simulateReceiveMessage(
            gson.toJson(new ErrorMessage("Command type " + command.getCommandType() + " not fully simulated.")));
    }
  }

  private String formatMove(ChessMove move) {
    return positionToString(move.getStartPosition()) + positionToString(move.getEndPosition()) +
        (move.getPromotionPiece() != null ? move.getPromotionPiece().toString().substring(0, 1) : "");
  }

  private String positionToString(ChessPosition pos) {
    if (pos == null)
      return "";
    return "" + (char) ('a' + pos.getColumn() - 1) + (char) ('1' + pos.getRow() - 1);
  }

  private boolean isValidDummyMove(ChessMove move) {
    if (!move.getStartPosition().inBounds() || !move.getEndPosition().inBounds()) {
      return false;
    }
    if (move.getEndPosition().equals(move.getStartPosition())) {
      return false;
    }
    return true;
  }

  protected void onWebSocketOpen() {
    this.isConnected = true;
    System.out.println("[WebSocketClient] Connection opened.");
  }

  protected void onWebSocketMessage(String message) {
    try {
      System.out.println("[WebSocketClient] Message received: " + message);
      ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
      ServerMessage specificMessage;
      switch (serverMessage.getServerMessageType()) {
        case LOAD_GAME:
          specificMessage = gson.fromJson(message, LoadGameMessage.class);
          break;
        case ERROR:
          specificMessage = gson.fromJson(message, ErrorMessage.class);
          break;
        case NOTIFICATION:
          specificMessage = gson.fromJson(message, NotificationMessage.class);
          break;
        default:
          throw new Exception("Unknown server message type received: " + serverMessage.getServerMessageType());
      }

      if (messageListener != null) {
        messageListener.onMessageReceived(specificMessage);
      } else {
        System.err.println("[WebSocketClient] No message listener registered to handle message!");
      }
    } catch (Exception e) {
      String errorMsg = "Failed to process server message: " + e.getMessage() + " | Raw message: " + message;
      System.err.println("[WebSocketClient] " + errorMsg);
      if (messageListener != null) {
        messageListener.onError(errorMsg);
      }
    }
  }

  protected void onWebSocketClose(int code, String reason) {
    this.isConnected = false;
    System.out.println(
        "[WebSocketClient] Connection closed. Code: " + code + ", Reason: " + (reason != null ? reason : "N/A"));
    if (messageListener != null) {
      messageListener.onError("WebSocket connection closed: " + (reason != null ? reason : "Code " + code));
    }
  }

  protected void onWebSocketError(Throwable cause) {
    String errorMsg = "WebSocket error: " + cause.getMessage();
    System.err.println("[WebSocketClient] " + errorMsg);
    cause.printStackTrace();
    if (messageListener != null) {
      messageListener.onError(errorMsg);
    }
    this.isConnected = false;
  }
}