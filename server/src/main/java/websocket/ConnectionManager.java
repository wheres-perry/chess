package websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages WebSocket connections, associating users with games and handling
 * message broadcasting.
 */
public class ConnectionManager {
  /**
   * Maps game IDs to a map of usernames to Connection objects.
   */
  public final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> gameConnections = new ConcurrentHashMap<>();

  /**
   * Adds a new connection, associating a visitor name with a WebSocket session.
   * 
   * @param visitorName The name of the visitor.
   * @param session     The WebSocket session.
   */
  public void add(String visitorName, Session session) {
    var connection = new Connection(visitorName, session);
  }

  /**
   * Removes a connection based on the visitor name.
   * 
   * @param visitorName The name of the visitor to remove.
   */
  public void remove(String visitorName) {
    for (Map.Entry<Integer, ConcurrentHashMap<String, Connection>> entry : gameConnections.entrySet()) {
      if (entry.getValue().containsKey(visitorName)) {
        entry.getValue().remove(visitorName);
        if (entry.getValue().isEmpty()) {
          gameConnections.remove(entry.getKey());
        }
        break;
      }
    }
  }

  /**
   * Adds a new connection, associating a username, WebSocket session, and game
   * ID.
   * 
   * @param username The username of the connected user.
   * @param session  The WebSocket session.
   * @param gameID   The ID of the game the user is joining.
   */
  public void add(String username, Session session, Integer gameID) {
    var connection = new Connection(username, session);
    gameConnections.computeIfAbsent(gameID, k -> new ConcurrentHashMap<>()).put(username, connection);
  }

  /**
   * Removes a connection based on username and game ID.
   * 
   * @param username The username of the user to remove.
   * @param gameID   The ID of the game to remove the user from.
   */
  public void remove(String username, Integer gameID) {
    ConcurrentHashMap<String, Connection> gameMap = gameConnections.get(gameID);
    if (gameMap != null) {
      gameMap.remove(username);
      if (gameMap.isEmpty()) {
        gameConnections.remove(gameID);
      }
    }
  }

  /**
   * Sends a message to a specific user.
   * 
   * @param username The username of the recipient.
   * @param message  The message to send.
   * @throws IOException If an I/O error occurs while sending the message.
   */
  public void sendToOne(String username, String message) throws IOException {
    Connection connection = findConnection(username);
    if (connection != null && connection.session.isOpen()) {
      connection.send(message);
    }
  }

  /**
   * Broadcasts a message to all users in a specific game, excluding a specified
   * user.
   * 
   * @param excludeUsername The username to exclude from the broadcast.
   * @param gameID          The ID of the game to broadcast to.
   * @param message         The message to broadcast.
   * @throws IOException If an I/O error occurs while sending the message.
   */
  public void broadcast(String excludeUsername, Integer gameID, String message) throws IOException {
    var gameMap = gameConnections.get(gameID);
    if (gameMap == null)
      return;

    var removeList = new ArrayList<String>();
    for (var connection : gameMap.values()) {
      if (connection.session.isOpen()) {
        if (!connection.visitorName.equals(excludeUsername)) {
          connection.send(message);
        }
      } else {
        removeList.add(connection.visitorName);
      }
    }
    for (var usernameToRemove : removeList) {
      gameMap.remove(usernameToRemove);
    }
    if (gameMap.isEmpty()) {
      gameConnections.remove(gameID);
    }
  }

  /**
   * Finds a connection by username.
   * 
   * @param username The username to search for.
   * @return The Connection object if found, otherwise null.
   */
  private Connection findConnection(String username) {
    for (ConcurrentHashMap<String, Connection> gameMap : gameConnections.values()) {
      if (gameMap.containsKey(username)) {
        return gameMap.get(username);
      }
    }
    return null;
  }
}