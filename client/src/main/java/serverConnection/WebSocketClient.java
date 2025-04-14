package serverconnection;

import chess.ChessMove;
import com.google.gson.Gson;
import websocket.commands.*;
import websocket.messages.*;

import javax.websocket.*;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Manages the WebSocket connection for the chess client.
 * Handles sending UserGameCommands and receiving ServerMessages.
 * Uses the Tyrus JSR 356 WebSocket client implementation.
 */
@ClientEndpoint
public class WebSocketClient {

  private final String serverUri;
  private WebSocketListener messageListener;
  private final Gson gson = new Gson();
  private Session session;
  private boolean isConnected = false;
  private final CountDownLatch connectionLatch = new CountDownLatch(1);

  public WebSocketClient(String serverUrl, WebSocketListener listener) {

    String wsUrl = serverUrl.replaceFirst("^http", "ws");
    if (!wsUrl.endsWith("/ws")) {
      if (wsUrl.endsWith("/")) {
        wsUrl += "ws";
      } else {
        wsUrl += "/ws";
      }
    }
    this.serverUri = wsUrl;
    this.messageListener = listener;
  }

  /**
   * Interface for components that need to react to WebSocket messages.
   */
  public interface WebSocketListener {
    void onMessageReceived(ServerMessage message);

    void onError(String errorMessage);
  }

  /**
   * Connects to the WebSocket server.
   * This method blocks until the connection is established or times out.
   * The initial `ConnectCommand` is sent *after* the WebSocket handshake
   * completes
   * (usually triggered by the user action via InGameRepl).
   *
   * @throws Exception If connection fails or times out.
   */
  public void connect() throws Exception {
    if (isConnected) {
      return;
    }
    try {
      WebSocketContainer container = ContainerProvider.getWebSocketContainer();
      this.session = container.connectToServer(this, URI.create(serverUri));

      boolean connected = connectionLatch.await(5, TimeUnit.SECONDS);
      if (!connected || !this.isConnected) {
        throw new Exception("WebSocket connection timed out or failed to open.");
      }

    } catch (DeploymentException e) {
      throw new Exception("Failed to deploy WebSocket endpoint: " + e.getMessage() + " | URI: " + serverUri, e);
    } catch (Exception e) {
      throw new Exception("Failed to connect to WebSocket server: " + e.getMessage() + " | URI: " + serverUri, e);
    }
  }

  /**
   * Disconnects from the WebSocket server.
   *
   * @throws Exception If disconnection fails.
   */
  public void disconnect() throws Exception {
    if (session != null && session.isOpen()) {
      try {
        session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Client disconnecting normally."));
      } catch (Exception e) {
        throw new Exception("Failed to disconnect WebSocket: " + e.getMessage(), e);
      } finally {
        this.session = null;
        this.isConnected = false;
      }
    } else {
      this.isConnected = false;
    }
  }

  public boolean isConnected() {
    return isConnected && session != null && session.isOpen();
  }

  /**
   * Sends a command object to the server over the WebSocket connection.
   * The command object is serialized to JSON before sending.
   *
   * @param command The UserGameCommand to send.
   * @throws Exception If not connected or if sending fails.
   */
  public void sendCommand(UserGameCommand command) throws Exception {
    if (!isConnected()) {
      throw new Exception("Not connected to WebSocket server.");
    }
    if (session == null) {
      throw new Exception("WebSocket session is null, cannot send command.");
    }
    try {
      String jsonCommand = gson.toJson(command);
      session.getBasicRemote().sendText(jsonCommand);
    } catch (Exception e) {
      this.isConnected = false;
      this.session = null;
      throw new Exception("Failed to send command via WebSocket: " + e.getMessage(), e);
    }
  }

  // --- Command Sending Helper Methods ---

  public void sendConnect(String authToken, Integer gameID) throws Exception {
    sendCommand(new ConnectCommand(authToken, gameID));
  }

  public void sendMakeMove(String authToken, Integer gameID, ChessMove move) throws Exception {
    sendCommand(new MakeMoveCommand(authToken, gameID, move));
  }

  public void sendLeave(String authToken, Integer gameID) throws Exception {
    sendCommand(new LeaveCommand(authToken, gameID));
  }

  public void sendResign(String authToken, Integer gameID) throws Exception {
    sendCommand(new ResignCommand(authToken, gameID));
  }

  /**
   * Called by the WebSocket container when the connection is successfully opened.
   * 
   * @param session The WebSocket session that has just been opened.
   */
  @OnOpen
  public void onWebSocketOpen(Session session) {
    this.session = session;
    this.isConnected = true;
    connectionLatch.countDown();
  }

  /**
   * Called by the WebSocket container when a text message is received.
   * 
   * @param message The message received from the server.
   * @param session The session through which the message was received.
   */
  @OnMessage
  public void onWebSocketMessage(String message, Session session) {
    try {

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
          String errorMsg = "Unknown server message type received: " + serverMessage.getServerMessageType();
          System.err.println(errorMsg);
          if (messageListener != null) {

            messageListener.onError(errorMsg + " | Raw: " + message);
          }
          return;
      }

      if (messageListener != null) {
        messageListener.onMessageReceived(specificMessage);
      } else {
        System.err.println("No message listener registered to handle message!");
      }
    } catch (com.google.gson.JsonSyntaxException jsonEx) {
      // Handle cases where the message isn't valid JSON
      String errorMsg = "Failed to parse server message JSON: " + jsonEx.getMessage() + " | Raw message: " + message;
      System.err.println(errorMsg);
      if (messageListener != null) {
        messageListener.onError(errorMsg);
      }
    } catch (Exception e) {
      String errorMsg = "Failed to process server message: " + e.getMessage() + " | Raw message: " + message;
      System.err.println(errorMsg);
      e.printStackTrace();
      if (messageListener != null) {
        messageListener.onError(errorMsg);
      }
    }
  }
}
