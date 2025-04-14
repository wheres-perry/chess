package websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.Objects; // Retained for equals/hashCode

/**
 * Represents a WebSocket client connection tied to a participant and a match.
 */
public class ClientLink {
  public final String participantName;
  public final Session matchSession;
  public final Integer matchID;

  /**
   * Initializes a new ClientLink instance.
   * 
   * @param matchID         The unique identifier for the match.
   * @param participantName The name of the participant (e.g., player name).
   * @param session         The WebSocket session object.
   */
  public ClientLink(Integer matchID, String participantName, Session session) {
    this.matchID = matchID;
    this.participantName = participantName;
    this.matchSession = session;
  }

  /**
   * Transmits a message to the client over the WebSocket connection.
   * 
   * @param message The message to transmit (expected to be a JSON string).
   * @throws IOException If an error occurs during transmission.
   */
  public void transmit(String message) throws IOException {
    if (matchSession != null && matchSession.isOpen()) {
      matchSession.getRemote().sendString(message);
    } else {
      throw new IOException("Session is closed or null.");
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ClientLink other = (ClientLink) obj;
    return Objects.equals(matchSession, other.matchSession)
        && Objects.equals(participantName, other.participantName)
        && Objects.equals(matchID, other.matchID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(participantName, matchSession, matchID);
  }
}