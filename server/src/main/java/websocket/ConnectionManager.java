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
    throw new RuntimeException("Stub error throws");
  }

  public void add(String username, Session session, Integer gameID) {
    throw new RuntimeException("Stub error throws");
  }

  public void remove(String username, Integer gameID) {
    throw new RuntimeException("Stub error throws");
  }

  public void sendToOne(String username, String message) throws IOException {
    throw new RuntimeException("Stub error throws");
  }

  public void broadcast(String excludeUsername, Integer gameID, String message) throws IOException {
    throw new RuntimeException("Stub error throws");
  }

  private Connection findConnection(String username) {
    throw new RuntimeException("Stub error throws");
  }
}