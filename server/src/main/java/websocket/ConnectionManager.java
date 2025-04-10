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

public class ConnectionManager {
  public final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> gameConnections = new ConcurrentHashMap<>();

  public void add(String visitorName, Session session) {
    var connection = new Connection(visitorName, session);
  }

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

  public void add(String username, Session session, Integer gameID) {
    var connection = new Connection(username, session);
    gameConnections.computeIfAbsent(gameID, k -> new ConcurrentHashMap<>()).put(username, connection);
  }

  public void remove(String username, Integer gameID) {
    ConcurrentHashMap<String, Connection> gameMap = gameConnections.get(gameID);
    if (gameMap != null) {
      gameMap.remove(username);
      if (gameMap.isEmpty()) {
        gameConnections.remove(gameID);
      }
    }
  }

  public void sendToOne(String username, String message) throws IOException {
    Connection connection = findConnection(username);
    if (connection != null && connection.session.isOpen()) {
      connection.send(message);
    }
  }

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

  private Connection findConnection(String username) {
    for (ConcurrentHashMap<String, Connection> gameMap : gameConnections.values()) {
      if (gameMap.containsKey(username)) {
        return gameMap.get(username);
      }
    }
    return null;
  }
}