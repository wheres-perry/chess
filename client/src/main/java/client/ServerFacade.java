package client;

import java.util.Map;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public void clear() throws Exception {
        // Stub implementation
    }

    public Map<String, Object> register(String username, String password, String email)
            throws Exception {
        // Stub implementation
        return null;
    }

    public Map<String, Object> login(String username, String password) throws Exception {
        // Stub implementation
        return null;
    }

    public void logout(String authToken) throws Exception {
        // Stub implementation
    }

    public Map<String, Object> listGames(String authToken) throws Exception {
        // Stub implementation
        return null;
    }

    public Map<String, Object> createGame(String authToken, String gameName) throws Exception {
        // Stub implementation
        return null;
    }

    public Map<String, Object> joinGame(String authToken, int gameID, String playerColor)
            throws Exception {
        // Stub implementation
        return null;
    }
}
