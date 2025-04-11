package client;

import chess.*;
import websocket.commands.*;
import websocket.messages.*;

import ui.PreLoginRepl;
import ui.PostLoginRepl;
import ui.InGameRepl;
import ui.EscapeSequences;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import com.google.gson.Gson;

/**
 * Manages the client-side state and interactions for the chess game,
 * coordinating between the UI (REPLs) and the server (via ServerFacade and
 * WebSocket).
 */
@ClientEndpoint
public class ChessClient {
    private final ServerFacade server;
    private String authToken;
    private String currentUser;

    private final PreLoginRepl preLoginRepl;
    private final PostLoginRepl postLoginRepl;
    private final InGameRepl inGameRepl;

    private Session websocketSession;
    private final String serverWsUrl;
    private final Gson gson = new Gson();

    public ChessClient(String serverHttpUrl) {
        this.server = createServerFacade(serverHttpUrl);
        this.preLoginRepl = new PreLoginRepl(this);
        this.postLoginRepl = new PostLoginRepl(this);
        this.inGameRepl = new InGameRepl(this);
        this.serverWsUrl = serverHttpUrl.replaceFirst("http", "ws") + "/ws";
    }

    protected ServerFacade createServerFacade(String serverUrl) {
        return new ServerFacade(serverUrl);
    }

    private void connectWebSocket() throws Exception {
        if (websocketSession == null || !websocketSession.isOpen()) {
            try {
                URI uri = new URI(serverWsUrl);
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                this.websocketSession = container.connectToServer(this, uri);
                System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "WebSocket connected."
                        + EscapeSequences.RESET_TEXT_COLOR);
            } catch (URISyntaxException | DeploymentException | IOException e) {
                throw new Exception("Failed to connect to WebSocket server: " + e.getMessage(), e);
            }
        }
    }

    private void disconnectWebSocket() {
        if (websocketSession != null && websocketSession.isOpen()) {
            try {
                websocketSession.close();
                System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "WebSocket disconnected."
                        + EscapeSequences.RESET_TEXT_COLOR);
            } catch (IOException e) {
                System.err.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error disconnecting WebSocket: "
                        + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
            } finally {
                websocketSession = null;
            }
        }
    }

    private void sendWebSocketMessage(UserGameCommand command) throws Exception {
        if (websocketSession == null || !websocketSession.isOpen()) {
            throw new Exception("WebSocket is not connected. Cannot send command.");
        }
        try {
            String msg = gson.toJson(command);
            this.websocketSession.getBasicRemote().sendText(msg);
        } catch (IOException e) {
            throw new Exception("Failed to send WebSocket message: " + e.getMessage(), e);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.websocketSession = session;
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
            if (inGameRepl != null && inGameRepl.isInGame()) {
                inGameRepl.handleServerMessage(message);
            } else {
                if (serverMessage.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
                    ErrorMessage errorMsg = gson.fromJson(message, ErrorMessage.class);
                    System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_RED + "[SERVER ERROR] "
                            + errorMsg.getErrorMessage() + EscapeSequences.RESET_TEXT_COLOR);
                } else {
                    System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "[SERVER MSG] " + message
                            + EscapeSequences.RESET_TEXT_COLOR);
                }
            }
        } catch (Exception e) {
            System.err.println(EscapeSequences.SET_TEXT_COLOR_RED + "Failed to process WebSocket message: "
                    + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
            System.err.println("Received raw message: " + message);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        this.websocketSession = null;
        if (inGameRepl.isInGame()) {
            System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_YELLOW
                    + "WebSocket connection closed unexpectedly. Returning to lobby."
                    + EscapeSequences.RESET_TEXT_COLOR);
            inGameRepl.setInGame(false);
            inGameRepl.setPlayerColor(null);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println(EscapeSequences.SET_TEXT_COLOR_RED + "WebSocket @OnError: " + throwable.getMessage()
                + EscapeSequences.RESET_TEXT_COLOR);
        this.websocketSession = null;
        if (inGameRepl.isInGame()) {
            System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_YELLOW + "WebSocket error. Returning to lobby."
                    + EscapeSequences.RESET_TEXT_COLOR);
            inGameRepl.setInGame(false);
            inGameRepl.setPlayerColor(null);
        }
    }

    public void run() {
        System.out.println(
                EscapeSequences.SET_TEXT_COLOR_BLUE + "♕ Welcome to 240 Chess ♕" + EscapeSequences.RESET_TEXT_COLOR);
        boolean quit = false;
        while (!quit) {
            try {
                if (!isLoggedIn()) {
                    quit = preLoginRepl.run();
                } else if (isInGameActive()) {
                    quit = inGameRepl.run();
                } else {
                    quit = postLoginRepl.run();
                }
            } catch (Exception e) {
                System.err.println(EscapeSequences.SET_TEXT_COLOR_RED + "An unexpected error occurred: "
                        + e.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
                if (!(e instanceof ServerFacade.ServerException)) {
                }
            }
        }
        disconnectWebSocket();
        System.out.println(
                EscapeSequences.SET_TEXT_COLOR_YELLOW + "Thanks for playing!" + EscapeSequences.RESET_TEXT_COLOR);
    }

    public void register(String username, String password, String email) throws Exception {
        HashMap<String, Object> response = server.register(username, password, email);
        authToken = (String) response.get("authToken");
        currentUser = username;
    }

    public void login(String username, String password) throws Exception {
        HashMap<String, Object> response = server.login(username, password);
        authToken = (String) response.get("authToken");
        currentUser = username;
    }

    public void logout() throws Exception {
        if (isLoggedIn()) {
            try {
                if (isInGameActive() && inGameRepl.getCurrentGameID() != null) {
                    sendLeaveCommand();
                } else {
                    disconnectWebSocket();
                }
                server.logout(authToken);
            } catch (Exception e) {
                System.err.println(EscapeSequences.SET_TEXT_COLOR_RED + "Logout request failed: " + e.getMessage()
                        + EscapeSequences.RESET_TEXT_COLOR);
            } finally {
                authToken = null;
                currentUser = null;
                if (inGameRepl != null) {
                    inGameRepl.setInGame(false);
                    inGameRepl.setPlayerColor(null);
                }
            }
        }
    }

    public void createGame(String gameName) throws Exception {
        if (!isLoggedIn())
            throw new Exception("You must be logged in to create a game.");
        HashMap<String, Object> response = server.createGame(authToken, gameName);
        Object gameIdObj = response.get("gameID");

        String gameIdStr = "Unknown";
        if (gameIdObj instanceof Number) {
            gameIdStr = String.format("%.0f", ((Number) gameIdObj).doubleValue());
        } else if (gameIdObj != null) {
            gameIdStr = gameIdObj.toString();
        }
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Game '" + gameName
                + "' created successfully (Internal ID: " + gameIdStr + ")" + EscapeSequences.RESET_TEXT_COLOR);
    }

    @SuppressWarnings("unchecked")
    public List<HashMap<String, Object>> listGames() throws Exception {
        if (!isLoggedIn())
            throw new Exception("You must be logged in to list games.");
        HashMap<String, Object> response = server.listGames(authToken);
        Object gamesObj = response.get("games");

        if (gamesObj instanceof List) {
            List<?> rawList = (List<?>) gamesObj;
            List<HashMap<String, Object>> result = new ArrayList<>();
            for (Object item : rawList) {
                if (item instanceof Map) {
                    result.add(new HashMap<>((Map<String, ?>) item));
                } else {
                    System.err.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                            + "Warning: Found non-Map item in games list: " + item
                            + EscapeSequences.RESET_TEXT_COLOR);
                }
            }
            return result;
        } else if (gamesObj == null) {
            return new ArrayList<>();
        } else {
            throw new Exception("Received unexpected data format for games list: " + gamesObj.getClass().getName());
        }
    }

    public void joinGame(int gameID, String color) throws Exception {
        if (!isLoggedIn())
            throw new Exception("You must be logged in to join a game.");
        if (color == null || (!"WHITE".equalsIgnoreCase(color) && !"BLACK".equalsIgnoreCase(color))) {
            throw new IllegalArgumentException("Invalid color ('WHITE' or 'BLACK') specified for joining.");
        }

        server.joinGame(authToken, gameID, color.toUpperCase());

        connectWebSocket();
        sendWebSocketMessage(new ConnectCommand(authToken, gameID));

        ChessGame.TeamColor teamColorEnum = ChessGame.TeamColor.valueOf(color.toUpperCase());
        inGameRepl.setPlayerColor(teamColorEnum);
        inGameRepl.setInGame(true);

        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully requested to join game " + gameID
                + " as " + color + ". Waiting for game state..."
                + EscapeSequences.RESET_TEXT_COLOR);
    }

    public void observeGame(int gameID) throws Exception {
        if (!isLoggedIn())
            throw new Exception("You must be logged in to observe a game.");

        connectWebSocket();
        sendWebSocketMessage(new ConnectCommand(authToken, gameID));

        inGameRepl.setPlayerColor(null);
        inGameRepl.setInGame(true);

        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully requested to observe game " + gameID
                + ". Waiting for game state..."
                + EscapeSequences.RESET_TEXT_COLOR);
    }

    public void sendLeaveCommand() throws Exception {
        if (!isLoggedIn() || !isInGameActive())
            throw new Exception("Must be logged in and in a game to leave.");
        Integer gameID = inGameRepl.getCurrentGameID();
        if (gameID == null)
            throw new Exception("Cannot leave, game ID is unknown.");

        LeaveCommand command = new LeaveCommand(authToken, gameID);
        sendWebSocketMessage(command);

        disconnectWebSocket();
        inGameRepl.setInGame(false);
        inGameRepl.setPlayerColor(null);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Left game. Returning to lobby."
                + EscapeSequences.RESET_TEXT_COLOR);
    }

    public void sendMakeMoveCommand(ChessMove move) throws Exception {
        if (!isLoggedIn() || !isInGameActive())
            throw new Exception("Must be logged in and in a game to make a move.");
        Integer gameID = inGameRepl.getCurrentGameID();
        if (gameID == null)
            throw new Exception("Cannot make move, game ID is unknown.");
        if (move == null)
            throw new Exception("Move cannot be null.");

        MakeMoveCommand command = new MakeMoveCommand(authToken, gameID, move);
        sendWebSocketMessage(command);
    }

    public void sendResignCommand() throws Exception {
        if (!isLoggedIn() || !isInGameActive())
            throw new Exception("Must be logged in and in a game to resign.");
        Integer gameID = inGameRepl.getCurrentGameID();
        if (gameID == null)
            throw new Exception("Cannot resign, game ID is unknown.");

        ResignCommand command = new ResignCommand(authToken, gameID);
        sendWebSocketMessage(command);
    }

    public void triggerServerClear() throws Exception {
        server.clearDatabase();
    }

    public String getCurrentUser() {
        return currentUser != null ? currentUser : "Not Logged In";
    }

    public boolean isLoggedIn() {
        return authToken != null;
    }

    public boolean isInGameActive() {
        return inGameRepl != null && inGameRepl.isInGame();
    }

    public String getAuthToken() {
        return authToken;
    }
}