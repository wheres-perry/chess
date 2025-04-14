package client;

import chess.ChessGame;
import serverconnection.ServerFacade;
import ui.PreLoginRepl;
import ui.PostLoginRepl;
import ui.InGameRepl;
import ui.EscapeSequences;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

/**
 * Manages the client-side state and interactions for the chess game,
 * coordinating between the UI (REPLs) and the server (via ServerFacade).
 */
public class ChessClient {
    private final ServerFacade serverFacade;
    private String authToken;
    private String currentUser;
    private Integer currentJoinedGameID = null;

    private final PreLoginRepl preLoginRepl;
    private final PostLoginRepl postLoginRepl;
    private final InGameRepl inGameRepl;

    public ChessClient(String serverUrl) {
        this.serverFacade = createServerFacade(serverUrl);
        this.preLoginRepl = new PreLoginRepl(this);
        this.postLoginRepl = new PostLoginRepl(this);
        this.inGameRepl = new InGameRepl(this, this.serverFacade);
        this.serverFacade.setWebSocketListener(this.inGameRepl);
    }

    protected ServerFacade createServerFacade(String serverUrl) {
        return new ServerFacade(serverUrl);
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
                e.printStackTrace();
                if (isInGameActive()) {
                    System.err.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                            + "Returning to post-login state due to error." + EscapeSequences.RESET_TEXT_COLOR);
                    if (inGameRepl != null) {
                        inGameRepl.setInGame(false);
                    }
                    currentJoinedGameID = null;
                }
            }
        }
        System.out.println(
                EscapeSequences.SET_TEXT_COLOR_YELLOW + "Thanks for playing!" + EscapeSequences.RESET_TEXT_COLOR);
    }

    /**
     * Registers a new user.
     * 
     * @throws Exception If registration fails.
     */
    public void register(String username, String password, String email) throws Exception {
        HashMap<String, Object> response = serverFacade.register(username, password, email);
        authToken = (String) response.get("authToken");
        currentUser = username;
    }

    /**
     * Logs in an existing user.
     * 
     * @throws Exception If login fails.
     */
    public void login(String username, String password) throws Exception {
        HashMap<String, Object> response = serverFacade.login(username, password);
        authToken = (String) response.get("authToken");
        currentUser = username;
    }

    /**
     * Logs out the current user.
     * 
     * @throws Exception If logout fails.
     */
    public void logout() throws Exception {
        if (isLoggedIn()) {
            try {
                serverFacade.logout(authToken);
            } catch (Exception e) {
                System.err.println(EscapeSequences.SET_TEXT_COLOR_RED + "Logout request failed: " + e.getMessage()
                        + EscapeSequences.RESET_TEXT_COLOR);
            } finally {
                authToken = null;
                currentUser = null;
                currentJoinedGameID = null;
                if (inGameRepl != null) {
                    inGameRepl.setInGame(false);
                }
            }
        }
    }

    /**
     * Creates a new game on the server.
     * 
     * @param gameName The name for the new game.
     * @throws Exception If creation fails or user not logged in.
     */
    public void createGame(String gameName) throws Exception {
        if (!isLoggedIn()) {
            throw new Exception("You must be logged in to create a game.");
        }
        HashMap<String, Object> response = serverFacade.createGame(authToken, gameName);
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

    /**
     * Lists all available games from the server.
     * 
     * @return A list of game data HashMaps.
     * @throws Exception If listing fails or user not logged in.
     */
    @SuppressWarnings("unchecked")
    public List<HashMap<String, Object>> listGames() throws Exception {
        if (!isLoggedIn()) {
            throw new Exception("You must be logged in to list games.");
        }
        HashMap<String, Object> response = serverFacade.listGames(authToken);
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

    /**
     * Joins an existing game as a player. Calls HTTP then establishes WebSocket.
     * 
     * @param gameID The ID of the game to join.
     * @param color  The desired player color ("WHITE" or "BLACK").
     * @throws Exception If joining fails.
     */
    public void joinGame(int gameID, String color) throws Exception {
        if (!isLoggedIn()) {
            throw new Exception("You must be logged in to join a game.");
        }
        if (color == null || (!"WHITE".equalsIgnoreCase(color) && !"BLACK".equalsIgnoreCase(color))) {
            throw new IllegalArgumentException("Invalid color ('WHITE' or 'BLACK') specified for joining.");
        }
        if (currentUser == null) {
            throw new Exception("Current user information missing.");
        }

        serverFacade.joinGame(authToken, gameID, color.toUpperCase(), currentUser);

        ChessGame.TeamColor teamColorEnum = ChessGame.TeamColor.valueOf(color.toUpperCase());
        inGameRepl.setPlayerColor(teamColorEnum);
        inGameRepl.setCurrentGameID(gameID);
        inGameRepl.setInGame(true);
        this.currentJoinedGameID = gameID;

        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully joined game " + gameID + " as " + color
                + "." + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "WebSocket connected. Waiting for game data..."
                + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println(
                EscapeSequences.SET_TEXT_COLOR_BLUE + "Type 'help' for commands." + EscapeSequences.RESET_TEXT_COLOR);
    }

    /**
     * Observes an existing game. Calls HTTP then establishes WebSocket.
     * 
     * @param gameID The ID of the game to observe.
     * @throws Exception If observing fails.
     */
    public void observeGame(int gameID) throws Exception {
        if (!isLoggedIn()) {
            throw new Exception("You must be logged in to observe a game.");
        }
        if (currentUser == null) {
            throw new Exception("Current user information missing.");
        }

        serverFacade.observeGame(authToken, gameID, currentUser);

        inGameRepl.setPlayerColor(null);
        inGameRepl.setCurrentGameID(gameID);
        inGameRepl.setInGame(true);
        this.currentJoinedGameID = gameID;

        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully observing game " + gameID + "."
                + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "WebSocket connected. Waiting for game data..."
                + EscapeSequences.RESET_TEXT_COLOR);
        System.out.println(
                EscapeSequences.SET_TEXT_COLOR_BLUE + "Type 'help' for commands." + EscapeSequences.RESET_TEXT_COLOR);
    }

    /**
     * Triggers the server database clear operation.
     * 
     * @throws Exception If the operation fails.
     */
    public void triggerServerClear() throws Exception {
        serverFacade.clearDatabase();
    }

    /**
     * Gets the current logged-in user's name.
     * 
     * @return Username or "Not Logged In".
     */
    public String getCurrentUser() {
        return currentUser != null ? currentUser : "Not Logged In";
    }

    /**
     * Checks if a user is currently logged in.
     * 
     * @return true if logged in, false otherwise.
     */
    public boolean isLoggedIn() {
        return authToken != null;
    }

    /**
     * Checks if the client is currently in an active game state.
     * 
     * @return true if in a game, false otherwise.
     */
    public boolean isInGameActive() {
        return inGameRepl != null && inGameRepl.isInGame() && currentJoinedGameID != null;
    }

    /**
     * Gets the current user's authentication token.
     * 
     * @return The auth token, or null if not logged in.
     */
    public String getAuthToken() {
        return authToken;
    }

    /**
     * Gets the ID of the game the user is currently joined to or observing.
     * 
     * @return The game ID, or null if not in a game.
     */
    public Integer getCurrentGameID() {
        return currentJoinedGameID;
    }
}