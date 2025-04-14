package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Handles active WebSocket client links, mapping them to users and matches.
 */
public class ClientManager {
  public final ConcurrentHashMap<String, ClientLink> userLinks = new ConcurrentHashMap<>();
  private static final Gson serializer = new Gson();

  /**
   * Registers a new client link for a match and user.
   * 
   * @param matchId     The ID of the game the user is connecting to.
   * @param participant The username of the connecting user.
   * @param wsSession   The new WebSocket session.
   */
  public void register(Integer matchId, String participant, Session wsSession) {
    var link = new ClientLink(matchId, participant, wsSession);
    userLinks.put(participant, link);
    System.out.println("[ClientManager] Registered link for participant: " + participant + " in match: " + matchId);
  }

  /**
   * Removes a client link by participant name.
   * 
   * @param participant The username whose connection to remove.
   */
  public void unregister(String participant) {
    if (participant != null) {
      ClientLink removed = userLinks.remove(participant);
      if (removed != null) {
        System.out.println("[ClientManager] Unregistered link for participant: " + participant);
      }
    }
  }

  /**
   * Removes a client link by WebSocket session and returns the removed link.
   * 
   * @param wsSession The session to remove.
   * @return The ClientLink that was removed, or null if no link matched the
   *         session.
   */
  public ClientLink unregisterBySession(Session wsSession) {
    String participantToRemove = null;
    ClientLink removedLink = null;
    for (Map.Entry<String, ClientLink> entry : userLinks.entrySet()) {
      if (entry.getValue().matchSession.equals(wsSession)) {
        participantToRemove = entry.getKey();
        removedLink = entry.getValue(); // Get the link before removing
        break;
      }
    }
    if (participantToRemove != null) {
      userLinks.remove(participantToRemove);
      System.out.println("[ClientManager] Unregistered link via session for participant: " + participantToRemove);
    }
    return removedLink; // Return the link object (or null)
  }

  /**
   * Sends a message to all clients connected to a specific match, optionally
   * excluding one participant.
   * 
   * @param matchId         The ID of the match to broadcast to.
   * @param skipParticipant The username to exclude from the broadcast (can be
   *                        null).
   * @param msg             The ServerMessage object to send.
   * @throws IOException If sending fails for any client (first encountered error
   *                     is thrown, requires better handling in prod).
   */
  public void notifyMatch(Integer matchId, String skipParticipant, ServerMessage msg) throws IOException {
    var toRemove = new ArrayList<ClientLink>();
    String json = serializer.toJson(msg);

    if (matchId == null) {
      System.err.println("[ClientManager] Broadcast attempted with null matchId.");
      return;
    }

    System.out
        .println("[ClientManager] Broadcasting to match " + matchId + " (excluding " + skipParticipant + "): " + json);

    for (ClientLink link : userLinks.values()) {
      if (link.matchSession.isOpen()) {
        if (matchId.equals(link.matchID)) {
          if (skipParticipant == null || !Objects.equals(link.participantName, skipParticipant)) {
            try {
              // System.out.println("Sending to: " + link.participantName);
              link.transmit(json);
            } catch (IOException e) {
              System.err.println(
                  "Failed broadcast to " + link.participantName + " in match " + matchId + ": " + e.getMessage());
              toRemove.add(link);
              // Consider collecting errors instead of throwing the first one
              // throw e; // Potentially stops broadcasting to others
            }
          } else {
            // System.out.println("Skipping excluded participant: " + skipParticipant);
          }
        }
      } else {
        toRemove.add(link);
      }
    }

    for (var link : toRemove) {
      unregisterBySession(link.matchSession);
    }
  }

  /**
   * Sends a message directly to a specific participant if they are connected.
   * 
   * @param participant The target username.
   * @param msg         The message to send.
   * @throws IOException If the user is found but sending fails.
   */
  public void notifyUser(String participant, ServerMessage msg) throws IOException {
    ClientLink link = userLinks.get(participant);
    if (link != null && link.matchSession.isOpen()) {
      String json = serializer.toJson(msg);
      System.out.println("[ClientManager] Sending direct message to " + participant + ": " + json);
      link.transmit(json);
    } else if (link != null) {
      System.err.println("[ClientManager] Attempted to notify " + participant + " but session is closed.");
      unregister(participant);
    } else {
      System.err.println("[ClientManager] Attempted to notify " + participant + " but user link not found.");
    }
  }
}