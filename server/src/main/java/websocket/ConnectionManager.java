package websocket;

import com.google.gson.Gson; // Import Gson for serialization
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage; // Use the base class for messages

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map; // Import Map for entrySet iteration

/**
 * Manages active WebSocket connections, associating them with users and games.
 * Provides methods for broadcasting messages to relevant clients.
 */
public class ConnectionManager {
  // Map username (visitorName) to their connection details
  public final ConcurrentHashMap<String, ClientLink> connections = new ConcurrentHashMap<>();
  private static final Gson gson = new Gson(); // Gson for serializing messages

  /**
   * Adds a new connection associated with a game and user.
   * If a user already has a connection (e.g., reconnecting), replaces the old
   * session.
   * 
   * @param gameID   The ID of the game the user is connecting to.
   * @param username The username of the connecting user.
   * @param session  The new WebSocket session.
   */
  public void add(Integer gameID, String username, Session session) {
    var connection = new ClientLink(gameID, username, session);
    connections.put(username, connection);
    System.out.println("Added connection for user: " + username + " in game: " + gameID); // Logging
  }

  /**
   * Removes a connection based on the username.
   * 
   * @param username The username whose connection to remove.
   */
  public void remove(String username) {
    if (username != null) {
      ClientLink removed = connections.remove(username);
      if (removed != null) {
        System.out.println("Removed connection for user: " + username); // Logging
      }
    }
  }

  /**
   * Removes a connection based on the WebSocket session object.
   * Useful for cleanup during onClose/onError events.
   * 
   * @param session The session to remove.
   * @return The username associated with the removed session, or null if not
   *         found.
   */
  public String removeBySession(Session session) {
    String userToRemove = null;
    for (Map.Entry<String, ClientLink> entry : connections.entrySet()) {
      if (entry.getValue().gameSession.equals(session)) {
        userToRemove = entry.getKey();
        break;
      }
    }
    if (userToRemove != null) {
      connections.remove(userToRemove);
      System.out.println("Removed connection via session for user: " + userToRemove); // Logging
    }
    return userToRemove;
  }

  /**
   * Sends a message to all clients connected to a specific game, optionally
   * excluding one user.
   * 
   * @param gameID          The ID of the game to broadcast to.
   * @param excludeUsername The username to exclude from the broadcast (can be
   *                        null).
   * @param message         The ServerMessage object to send.
   * @throws IOException If sending fails for any client (first encountered error
   *                     is thrown).
   */
  public void broadcast(Integer gameID, String excludeUsername, ServerMessage message) throws IOException {
    var removeList = new ArrayList<ClientLink>();
    String messageJson = gson.toJson(message); // Serialize the message once

    if (gameID == null) {
      System.err.println("Broadcast attempted with null gameID. Message: " + messageJson);
      return; // Cannot broadcast without a game ID
    }

    System.out.println("Broadcasting to game " + gameID + " (excluding " + excludeUsername + "): " + messageJson); // Logging

    for (ClientLink c : connections.values()) {
      if (c.gameSession.isOpen()) {
        // Check if connection belongs to the target game
        if (gameID.equals(c.gameID)) {
          // Check if this connection should be excluded
          if (excludeUsername == null || !Objects.equals(c.visitorName, excludeUsername)) {
            try {
              // System.out.println("Sending to: " + c.visitorName); // More detailed logging
              // if needed
              c.send(messageJson);
            } catch (IOException e) {
              System.err.println("Failed broadcast to " + c.visitorName + " in game " + gameID + ": " + e.getMessage());
              // Decide on error handling: remove immediately, add to list, or just log?
              removeList.add(c); // Add to list for removal after iteration
              // Optionally re-throw or collect errors: throw e;
            }
          } else {
            // System.out.println("Skipping excluded user: " + excludeUsername); // Logging
          }
        }
      } else {
        removeList.add(c);
      }
    }

    // Clean up connections that were closed or failed during broadcast
    for (var c : removeList) {
      removeBySession(c.gameSession); // Use removeBySession to ensure correct removal
    }
  }

  /**
   * Sends a message directly to a specific user if they are connected.
   * 
   * @param username The target username.
   * @param message  The message to send.
   * @throws IOException If the user is found but sending fails.
   */
  public void sendMessageToUser(String username, ServerMessage message) throws IOException {
    ClientLink targetConnection = connections.get(username);
    if (targetConnection != null && targetConnection.gameSession.isOpen()) {
      String messageJson = gson.toJson(message);
      System.out.println("Sending direct message to " + username + ": " + messageJson); // Logging
      targetConnection.send(messageJson);
    } else if (targetConnection != null) {
      System.err.println("Attempted to send direct message to " + username + " but session is closed.");
      remove(username); // Clean up closed connection
    } else {
      System.err.println("Attempted to send direct message to " + username + " but user not found.");
    }
  }
}