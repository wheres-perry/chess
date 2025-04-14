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
   */
  public void register(Integer matchId, String participant, Session wsSession) {
    var link = new ClientLink(matchId, participant, wsSession);
    userLinks.put(participant, link);
  }

  /**
   * Removes a client link by participant name.
   */
  public void unregister(String participant) {
    if (participant != null) {
      userLinks.remove(participant);
    }
  }

  /**
   * Removes a client link by WebSocket session.
   */
  public String unregisterBySession(Session wsSession) {
    String found = null;
    for (Map.Entry<String, ClientLink> entry : userLinks.entrySet()) {
      if (entry.getValue().matchSession.equals(wsSession)) {
        found = entry.getKey();
        break;
      }
    }
    if (found != null) {
      userLinks.remove(found);
    }
    return found;
  }

  /**
   * Sends a message to all clients in a match, optionally skipping one
   * participant.
   */
  public void notifyMatch(Integer matchId, String skipParticipant, ServerMessage msg) throws IOException {
    var toRemove = new ArrayList<ClientLink>();
    String json = serializer.toJson(msg);

    if (matchId == null) {
      return;
    }

    for (ClientLink link : userLinks.values()) {
      if (link.matchSession.isOpen()) {
        if (matchId.equals(link.matchID)) {
          if (skipParticipant == null || !Objects.equals(link.participantName, skipParticipant)) {
            try {
              link.transmit(json);
            } catch (IOException e) {
              toRemove.add(link);
            }
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
   * Sends a message to a specific participant if connected.
   */
  public void notifyUser(String participant, ServerMessage msg) throws IOException {
    ClientLink link = userLinks.get(participant);
    if (link != null && link.matchSession.isOpen()) {
      String json = serializer.toJson(msg);
      link.transmit(json);
    } else if (link != null) {
      unregister(participant);
    }
  }
}