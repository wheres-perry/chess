package websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.Objects; // Keep for equals/hashCode

/**
 * Represents a single WebSocket connection associated with a user and a game.
 */
public class Connection {
  public final String visitorName; // Typically the username
  public final Session session;
  public final Integer gameID; // Added game ID

  /**
   * Constructs a new Connection.
   * 
   * @param gameID      The ID of the game this connection is associated with.
   * @param visitorName The identifier for the user (e.g., username).
   * @param session     The WebSocket session object.
   */
  public Connection(Integer gameID, String visitorName, Session session) {
    this.gameID = gameID;
    this.visitorName = visitorName;
    this.session = session;
  }

  /**
   * Sends a text message over the WebSocket connection.
   * 
   * @param msg The message to send (expected to be JSON string).
   * @throws IOException If sending fails.
   */
  public void send(String msg) throws IOException {
    if (session.isOpen()) {
      session.getRemote().sendString(msg);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Connection that = (Connection) o;
    // Primarily identify connection by session, but include others for completeness
    return Objects.equals(session, that.session) && Objects.equals(visitorName, that.visitorName)
        && Objects.equals(gameID, that.gameID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(visitorName, session, gameID);
  }
}