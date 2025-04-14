package serverconnection;

import java.util.HashMap;

/**
 * Facade for the Chess server API. Coordinates HTTP and WebSocket communication
 * using dedicated clients.
 */
public class ServerFacade {
    private final String serverBaseUrl;
    private final HttpClient httpClient;
    private WebSocketClient webSocketClient;
    private WebSocketClient.WebSocketListener webSocketListener;

    /**
     * Custom exception class for facade-level errors.
     */
    public static class ServerFacadeException extends Exception {
        public ServerFacadeException(String message) {
            super(message);
        }

        public ServerFacadeException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public ServerFacade(String url) {
        this.serverBaseUrl = url;
        this.httpClient = new HttpClient(url);
    }

    /**
     * Registers the listener that will receive WebSocket messages.
     *
     * @param listener The listener implementation (typically InGameRepl).
     */
    public void setWebSocketListener(WebSocketClient.WebSocketListener listener) {
        this.webSocketListener = listener;
    }

    /**
     * Clears the server database via HTTP.
     *
     * @throws ServerFacadeException If the HTTP request fails.
     */
    public void clearDatabase() throws ServerFacadeException {
        try {
            httpClient.sendRequest("DELETE", "/db", null, null);
        } catch (HttpClient.HttpException e) {
            throw new ServerFacadeException("Failed to clear database: " + e.getMessage(), e);
        }
    }

    /**
     * Registers a new user via HTTP.
     *
     * @return A HashMap containing the server's response (authToken, username).
     * @throws ServerFacadeException If the HTTP request fails.
     */
    public HashMap<String, Object> register(String username, String password, String email)
            throws ServerFacadeException {
        try {
            HashMap<String, String> requestBody = new HashMap<>();
            requestBody.put("username", username);
            requestBody.put("password", password);
            requestBody.put("email", email);
            return httpClient.sendRequest("POST", "/user", null, requestBody);
        } catch (HttpClient.HttpException e) {
            throw new ServerFacadeException("Registration failed: " + e.getMessage(), e);
        }
    }

    /**
     * Logs in an existing user via HTTP.
     *
     * @return A HashMap containing the server's response (authToken, username).
     * @throws ServerFacadeException If the HTTP request fails.
     */
    public HashMap<String, Object> login(String username, String password) throws ServerFacadeException {
        try {
            HashMap<String, String> requestBody = new HashMap<>();
            requestBody.put("username", username);
            requestBody.put("password", password);
            return httpClient.sendRequest("POST", "/session", null, requestBody);
        } catch (HttpClient.HttpException e) {
            throw new ServerFacadeException("Login failed: " + e.getMessage(), e);
        }
    }

    /**
     * Logs out the current user via HTTP and disconnects WebSocket if active.
     *
     * @param authToken The authentication token of the user to log out.
     * @throws ServerFacadeException If the HTTP request fails.
     */
    public void logout(String authToken) throws ServerFacadeException {
        if (webSocketClient != null && webSocketClient.isConnected()) {
            try {
                webSocketClient.disconnect();
            } catch (Exception e) {
                System.err.println("Warning: Error disconnecting WebSocket during logout: " + e.getMessage());
            } finally {
                webSocketClient = null;
            }
        }
        try {
            httpClient.sendRequest("DELETE", "/session", authToken, null);
        } catch (HttpClient.HttpException e) {
            throw new ServerFacadeException("Logout failed: " + e.getMessage(), e);
        }
    }

    /**
     * Lists all available games via HTTP.
     *
     * @param authToken The user's authentication token.
     * @return A HashMap containing the server's response ("games" list).
     * @throws ServerFacadeException If the HTTP request fails.
     */
    public HashMap<String, Object> listGames(String authToken) throws ServerFacadeException {
        try {
            return httpClient.sendRequest("GET", "/game", authToken, null);
        } catch (HttpClient.HttpException e) {
            throw new ServerFacadeException("Failed to list games: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new game via HTTP.
     *
     * @param authToken The user's authentication token.
     * @param gameName  The desired name for the new game.
     * @return A HashMap containing the server's response ("gameID").
     * @throws ServerFacadeException If the HTTP request fails.
     */
    public HashMap<String, Object> createGame(String authToken, String gameName) throws ServerFacadeException {
        try {
            HashMap<String, String> requestBody = new HashMap<>();
            requestBody.put("gameName", gameName);
            return httpClient.sendRequest("POST", "/game", authToken, requestBody);
        } catch (HttpClient.HttpException e) {
            throw new ServerFacadeException("Failed to create game: " + e.getMessage(), e);
        }
    }

    /**
     * Joins an existing game: performs HTTP request then connects WebSocket.
     *
     * @param authToken   The user's authentication token.
     * @param gameID      The ID of the game to join.
     * @param playerColor The desired color ("WHITE" or "BLACK").
     * @param username    The username joining (needed for initial WS connect logic,
     *                    though not sent directly)
     * @throws ServerFacadeException If the HTTP request or WebSocket connection
     *                               fails.
     */
    public void joinGame(String authToken, int gameID, String playerColor, String username)
            throws ServerFacadeException {
        try {
            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("gameID", gameID);
            if (playerColor != null
                    && (playerColor.equalsIgnoreCase("WHITE") || playerColor.equalsIgnoreCase("BLACK"))) {
                requestBody.put("playerColor", playerColor.toUpperCase());
            } else {
                throw new IllegalArgumentException("Player color (WHITE or BLACK) is required to join as player.");
            }
            httpClient.sendRequest("PUT", "/game", authToken, requestBody);

            connectWebSocketAndSendConnectCommand(authToken, gameID); // Pass only necessary info

        } catch (HttpClient.HttpException e) {
            throw new ServerFacadeException("HTTP request to join game failed: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ServerFacadeException(e.getMessage(), e);
        } catch (Exception e) {
            throw new ServerFacadeException(
                    "WebSocket connection or command failed after joining game: " + e.getMessage(), e);
        }
    }

    /**
     * Observes an existing game: performs HTTP request then connects WebSocket.
     * Note: The HTTP PUT /game request *without* playerColor signifies observation.
     *
     * @param authToken The user's authentication token.
     * @param gameID    The ID of the game to observe.
     * @param username  The username observing (needed for initial WS connect logic,
     *                  though not sent directly)
     * @throws ServerFacadeException If the HTTP request or WebSocket connection
     *                               fails.
     */
    public void observeGame(String authToken, int gameID, String username) throws ServerFacadeException {
        try {

            HashMap<String, Object> requestBody = new HashMap<>();
            requestBody.put("gameID", gameID);

            httpClient.sendRequest("PUT", "/game", authToken, requestBody);

            connectWebSocketAndSendConnectCommand(authToken, gameID);

        } catch (HttpClient.HttpException e) {
            throw new ServerFacadeException("HTTP request to observe game failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ServerFacadeException(
                    "WebSocket connection or command failed after observing game: " + e.getMessage(), e);
        }
    }

    /**
     * Helper to initialize, connect the WebSocket client, and send the
     * application-level CONNECT command.
     *
     * @param authToken Auth token for WS connection command.
     * @param gameID    Game ID for WS connection command.
     * @throws Exception If connection or sending the command fails.
     */
    private void connectWebSocketAndSendConnectCommand(String authToken, Integer gameID) throws Exception {
        // Disconnect existing connection if any
        if (webSocketClient != null && webSocketClient.isConnected()) {
            try {
                System.out.println("[ServerFacade] WebSocket already connected, disconnecting before reconnecting.");
                webSocketClient.disconnect();
            } catch (Exception e) {
                System.err.println("Warning: Error disconnecting existing WebSocket: " + e.getMessage());
            }
        }
        if (this.webSocketListener == null) {
            throw new IllegalStateException("WebSocketListener must be set on ServerFacade before connecting.");
        }

        webSocketClient = new WebSocketClient(serverBaseUrl, this.webSocketListener);
        webSocketClient.connect();
        if (!webSocketClient.isConnected()) {
            throw new Exception("Failed to establish WebSocket connection.");
        }

        // Send the application-level CONNECT command
        webSocketClient.sendConnect(authToken, gameID);
        System.out.println("[ServerFacade] WebSocket CONNECT command sent for gameID: " + gameID);
    }

    /**
     * Sends a MAKE_MOVE command via WebSocket.
     *
     * @throws ServerFacadeException If WebSocket is not connected or send fails.
     */
    public void sendMakeMoveCommand(String authToken, Integer gameID, chess.ChessMove move)
            throws ServerFacadeException {
        if (webSocketClient == null || !webSocketClient.isConnected()) {
            throw new ServerFacadeException("WebSocket not connected. Cannot send move.");
        }
        try {
            webSocketClient.sendMakeMove(authToken, gameID, move);
        } catch (Exception e) {
            throw new ServerFacadeException("Failed to send MakeMove command: " + e.getMessage(), e);
        }
    }

    /**
     * Sends a LEAVE command via WebSocket.
     * Also disconnects the WebSocket connection after sending the command.
     *
     * @throws ServerFacadeException If WebSocket is not connected or send fails.
     */
    public void sendLeaveCommand(String authToken, Integer gameID) throws ServerFacadeException {
        if (webSocketClient == null) {
            System.err.println(
                    "[ServerFacade] Warning: Attempting to leave game, but WebSocket was never connected or already cleaned up.");
            return;
        }
        if (!webSocketClient.isConnected()) {

            System.err.println(
                    "[ServerFacade] Warning: WebSocket not connected. Cannot send leave command. Cleaning up local reference.");
            webSocketClient = null;
            return;
        }

        try {
            webSocketClient.sendLeave(authToken, gameID);
            System.out.println("[ServerFacade] LEAVE command sent for gameID: " + gameID);
        } catch (Exception e) {
            System.err.println("[ServerFacade] Error sending Leave command: " + e.getMessage());
        } finally {
            try {
                if (webSocketClient != null && webSocketClient.isConnected()) {
                    webSocketClient.disconnect();
                    System.out.println("[ServerFacade] WebSocket disconnected after sending leave.");
                }
            } catch (Exception disconnectEx) {
                System.err.println("[ServerFacade] Error disconnecting WebSocket after sending leave: "
                        + disconnectEx.getMessage());
            }
            webSocketClient = null;
        }
    }

    /**
     * Sends a RESIGN command via WebSocket.
     *
     * @throws ServerFacadeException If WebSocket is not connected or send fails.
     */
    public void sendResignCommand(String authToken, Integer gameID) throws ServerFacadeException {
        if (webSocketClient == null || !webSocketClient.isConnected()) {
            throw new ServerFacadeException("WebSocket not connected. Cannot send resign.");
        }
        try {
            webSocketClient.sendResign(authToken, gameID);
        } catch (Exception e) {
            throw new ServerFacadeException("Failed to send Resign command: " + e.getMessage(), e);
        }
    }
}