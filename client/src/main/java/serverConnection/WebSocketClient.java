package serverConnection;

import chess.ChessMove; // Added import
import com.google.gson.Gson;
import websocket.commands.*;
import websocket.messages.*;

import javax.websocket.*;
import java.net.URI;
import java.util.concurrent.CountDownLatch; // Added import
import java.util.concurrent.TimeUnit; // Added import

/**
 * Manages the WebSocket connection for the chess client.
 * Handles sending UserGameCommands and receiving ServerMessages.
 * Uses the Tyrus JSR 356 WebSocket client implementation.
 */
@ClientEndpoint // Marks this class as a WebSocket endpoint
public class WebSocketClient {

  private final String serverUri; // Renamed from serverUrl for clarity with URI object
  private WebSocketListener messageListener;
  private final Gson gson = new Gson();
  private Session session; // WebSocket session
  private boolean isConnected = false;
  private final CountDownLatch connectionLatch = new CountDownLatch(1); // For connect synchronization

  public WebSocketClient(String serverUrl, WebSocketListener listener) {
    // Ensure the URL starts with ws:// or wss://
    String wsUrl = serverUrl.replaceFirst("^http", "ws");
    if (!wsUrl.endsWith("/ws")) {
      // Ensure the path is correct, handle cases with or without trailing slash
      if (wsUrl.endsWith("/")) {
        wsUrl += "ws";
      } else {
        wsUrl += "/ws";
      }
    }
    this.serverUri = wsUrl;
    this.messageListener = listener;
    System.out.println("WebSocket Client Initialized for URI: " + this.serverUri);
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
      System.out.println("[WebSocketClient] Already connected.");
      return;
    }
    try {
      WebSocketContainer container = ContainerProvider.getWebSocketContainer();
      System.out.println("[WebSocketClient] Attempting to connect to: " + serverUri);
      // Pass 'this' instance so Tyrus uses the annotated methods (@OnOpen, @OnMessage
      // etc.)
      this.session = container.connectToServer(this, URI.create(serverUri));

      // Wait briefly for the @OnOpen callback to fire and set isConnected
      // This helps ensure the connection state is updated before returning
      boolean connected = connectionLatch.await(5, TimeUnit.SECONDS); // Wait up to 5 seconds
      if (!connected || !this.isConnected) {
        throw new Exception("WebSocket connection timed out or failed to open.");
      }
      System.out.println("[WebSocketClient] Connection seems established.");

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
        // Use a specific close code and reason if desired
        session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Client disconnecting normally."));
        System.out.println("[WebSocketClient] Disconnect requested.");
      } catch (Exception e) {
        throw new Exception("Failed to disconnect WebSocket: " + e.getMessage(), e);
      } finally {
        // Ensure state is updated even if close throws an error locally
        this.session = null;
        this.isConnected = false;
      }
    } else {
      System.out.println("[WebSocketClient] Already disconnected or session is null.");
      this.isConnected = false; // Ensure state is correct
    }
  }

  public boolean isConnected() {
    // Also check session state just in case the flag is stale
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
    if (!isConnected()) { // Use the isConnected() method for reliability
      throw new Exception("Not connected to WebSocket server.");
    }
    if (session == null) {
      throw new Exception("WebSocket session is null, cannot send command.");
    }
    try {
      String jsonCommand = gson.toJson(command);
      System.out.println("[WebSocketClient] Sending command: " + jsonCommand); // Log the command being sent
      session.getBasicRemote().sendText(jsonCommand);
    } catch (Exception e) {
      // If send fails, the connection might be broken. Update state.
      this.isConnected = false;
      this.session = null;
      throw new Exception("Failed to send command via WebSocket: " + e.getMessage(), e);
    }
  }

  // --- Command Sending Helper Methods ---

  public void sendConnect(String authToken, Integer gameID) throws Exception {
    // Note: The actual WebSocket connection is established by connect().
    // This sends the *application-level* connect command after the WS is open.
    sendCommand(new ConnectCommand(authToken, gameID));
  }

  public void sendMakeMove(String authToken, Integer gameID, ChessMove move) throws Exception {
    sendCommand(new MakeMoveCommand(authToken, gameID, move));
  }

  public void sendLeave(String authToken, Integer gameID) throws Exception {
    sendCommand(new LeaveCommand(authToken, gameID));
    // Consider disconnecting the websocket *after* sending leave?
    // Or let the server handle the disconnect notification?
    // Current server logic seems to expect the client might stay connected briefly.
    // Let's add a disconnect after sending leave for cleaner client state
    // management.
    // disconnect(); // Optional: Disconnect immediately after sending leave
  }

  public void sendResign(String authToken, Integer gameID) throws Exception {
    sendCommand(new ResignCommand(authToken, gameID));
  }

  // --- WebSocket Lifecycle Callback Methods (Annotated for Tyrus) ---

  /**
   * Called by the WebSocket container when the connection is successfully opened.
   * 
   * @param session The WebSocket session that has just been opened.
   */
  @OnOpen
  public void onWebSocketOpen(Session session) {
    this.session = session;
    this.isConnected = true;
    System.out.println("[WebSocketClient] Connection opened. Session ID: " + session.getId());
    connectionLatch.countDown(); // Signal that connection is open
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
      // Log raw message first for debugging
      System.out.println("[WebSocketClient] Raw message received: " + message);

      // Determine message type
      ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

      // Deserialize to specific type based on the determined type
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
          // Handle unknown type gracefully
          String errorMsg = "Unknown server message type received: " + serverMessage.getServerMessageType();
          System.err.println("[WebSocketClient] " + errorMsg);
          if (messageListener != null) {
            // Send a generic error or a custom message indicating unknown type
            messageListener.onError(errorMsg + " | Raw: " + message);
          }
          return; // Don't process further
      }

      // Pass the correctly deserialized message to the listener
      if (messageListener != null) {
        messageListener.onMessageReceived(specificMessage);
      } else {
        System.err.println("[WebSocketClient] No message listener registered to handle message!");
      }
    } catch (com.google.gson.JsonSyntaxException jsonEx) {
      // Handle cases where the message isn't valid JSON or doesn't match expected
      // structures
      String errorMsg = "Failed to parse server message JSON: " + jsonEx.getMessage() + " | Raw message: " + message;
      System.err.println("[WebSocketClient] " + errorMsg);
      if (messageListener != null) {
        messageListener.onError(errorMsg);
      }
    } catch (Exception e) {
      // Catch any other unexpected errors during message processing
      String errorMsg = "Failed to process server message: " + e.getMessage() + " | Raw message: " + message;
      System.err.println("[WebSocketClient] " + errorMsg);
      e.printStackTrace(); // Log stack trace for debugging
      if (messageListener != null) {
        messageListener.onError(errorMsg);
      }
    }
  }

  /**
   * Called by the WebSocket container when the connection is closed.
   * 
   * @param session     The session that is closing.
   * @param closeReason Contains the close code and reason phrase.
   */
  @OnClose
  public void onWebSocketClose(Session session, CloseReason closeReason) {
    this.isConnected = false;
    this.session = null; // Clear the session reference
    String reason = closeReason.getReasonPhrase() != null && !closeReason.getReasonPhrase().isEmpty()
        ? closeReason.getReasonPhrase()
        : "No reason provided";
    System.out.println(
        "[WebSocketClient] Connection closed. Code: " + closeReason.getCloseCode().getCode() + ", Reason: " + reason);

    // Notify listener about the closure, potentially as an error or distinct event
    if (messageListener != null) {
      // Send a generic error message indicating closure
      if (closeReason.getCloseCode() != CloseReason.CloseCodes.NORMAL_CLOSURE) {
        messageListener.onError("WebSocket connection closed unexpectedly: " + reason + " (Code: "
            + closeReason.getCloseCode().getCode() + ")");
      } else {
        // Optionally notify normal closure differently if needed
        System.out.println("[WebSocketClient] Normal closure initiated.");
        // messageListener.onConnectionClosedNormally(); // If you add such a method to
        // the listener
      }
    }
  }

  /**
   * Called by the WebSocket container when an error occurs.
   * 
   * @param session   The session where the error occurred (may be null).
   * @param throwable The error that occurred.
   */
  @OnError
  public void onWebSocketError(Session session, Throwable throwable) {
    // Error might mean connection is lost or will be closed
    this.isConnected = false;
    this.session = null; // Clear session as it might be unusable

    String errorMsg = "WebSocket error: " + throwable.getMessage();
    System.err.println("[WebSocketClient] " + errorMsg + (session != null ? " on session " + session.getId() : ""));
    throwable.printStackTrace(); // Log the full stack trace

    // Notify the listener
    if (messageListener != null) {
      messageListener.onError(errorMsg);
    }
  }
}