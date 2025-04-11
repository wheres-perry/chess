package client;

import chess.ChessGame;
import serverConnection.ServerFacade;
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
    private final ServerFacade server;
    private String authToken;
    private String currentUser;

    private final PreLoginRepl preLoginRepl;
    private final PostLoginRepl postLoginRepl;
    private final InGameRepl inGameRepl;

    public ChessClient(String serverUrl) {
        this.server = createServerFacade(serverUrl);
        this.preLoginRepl = new PreLoginRepl(this);
        this.postLoginRepl = new PostLoginRepl(this);
        this.inGameRepl = new InGameRepl(this);
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
                // Only print stack trace if it's not a known client/server communication error
                if (!(e instanceof ServerFacade.ServerException)) {
                    e.printStackTrace(); // For debugging purposes
                }
            }
        }
        System.out.println(
                EscapeSequences.SET_TEXT_COLOR_YELLOW + "Thanks for playing!" + EscapeSequences.RESET_TEXT_COLOR);
    }

    public void register(String username, String password, String email) throws Exception {
        HashMap<String, Object> response = server.register(username, password, email);
        authToken = (String) response.get("authToken");
        currentUser = username;
        // Success message printed by PreLoginRepl
    }

    public void login(String username, String password) throws Exception {
        HashMap<String, Object> response = server.login(username, password);
        authToken = (String) response.get("authToken");
        currentUser = username;
        // Success message printed by PreLoginRepl
    }

    public void logout() throws Exception {
        if (isLoggedIn()) {
            try {
                server.logout(authToken);
                // Success message printed by PostLoginRepl
            } catch (Exception e) {
                System.err.println(EscapeSequences.SET_TEXT_COLOR_RED + "Logout request failed: " + e.getMessage()
                        + EscapeSequences.RESET_TEXT_COLOR);
                throw e;
            } finally {
                authToken = null;
                currentUser = null;
                if (inGameRepl != null) {
                    inGameRepl.setPlayerColor(null); // Reset player color on logout
                    inGameRepl.setInGame(false);
                }
            }
        }
    }

    public void createGame(String gameName) throws Exception {
        if (!isLoggedIn())
            throw new Exception("You must be logged in to create a game.");
        HashMap<String, Object> response = server.createGame(authToken, gameName);
        Object gameIdObj = response.get("gameID"); // Spec says NewGameResult returns "gameID"

        String gameIdStr = "Unknown";
        // Handle potential number format (like Double) coming from JSON
        if (gameIdObj instanceof Number) {
            gameIdStr = String.format("%.0f", ((Number) gameIdObj).doubleValue());
        } else if (gameIdObj != null) {
            gameIdStr = gameIdObj.toString();
        }
        // should maybe be deleted in the future
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
                    // Ensure mutable HashMap for potential modifications if needed later
                    result.add(new HashMap<>((Map<String, ?>) item));
                } else {
                    System.err.println(EscapeSequences.SET_TEXT_COLOR_YELLOW
                            + "Warning: Found non-Map item in games list: " + item
                            + EscapeSequences.RESET_TEXT_COLOR);
                }
            }
            return result;
        } else if (gamesObj == null) {
            return new ArrayList<>(); // Return empty list if "games" key is missing or null
        } else {
            // Somethign is really wrong
            throw new Exception("Received unexpected data format for games list: " + gamesObj.getClass().getName());
        }
    }

    /**
     * Joins an existing game as a player (WHITE or BLACK).
     * Sets the client state to 'in game' and updates the InGameRepl.
     *
     * @param gameID The actual ID of the game to join.
     * @param color  The desired player color ("WHITE" or "BLACK").
     * @throws Exception If the request fails or the user is not logged in.
     */
    public void joinGame(int gameID, String color) throws Exception {
        if (!isLoggedIn())
            throw new Exception("You must be logged in to join a game.");
        if (color == null || (!"WHITE".equalsIgnoreCase(color) && !"BLACK".equalsIgnoreCase(color))) {
            throw new IllegalArgumentException("Invalid color ('WHITE' or 'BLACK') specified for joining.");
        }

        server.joinGame(authToken, gameID, color.toUpperCase()); // Send request to server

        // Update client state upon successful server response
        ChessGame.TeamColor teamColorEnum = ChessGame.TeamColor.valueOf(color.toUpperCase());
        inGameRepl.setPlayerColor(teamColorEnum); // Inform REPL of the player's color
        inGameRepl.setInGame(true); // Set client state to in-game

        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully joined game " + gameID + " as " + color
                + EscapeSequences.RESET_TEXT_COLOR);
        // Draw the initial board state immediately
        inGameRepl.drawBoard();
        System.out.println(
                EscapeSequences.SET_TEXT_COLOR_BLUE + "Type 'help' for commands." + EscapeSequences.RESET_TEXT_COLOR);
    }

    /**
     * Observes an existing game.
     * Sets the client state to 'in game' (observing) and updates the InGameRepl.
     *
     * @param gameID The actual ID of the game to observe.
     * @throws Exception If the request fails or the user is not logged in.
     */
    public void observeGame(int gameID) throws Exception {
        if (!isLoggedIn())
            throw new Exception("You must be logged in to observe a game.");

        // Call server facade to join as observer (no color specified)
        server.observeGame(authToken, gameID);

        // Update client state upon successful server response
        inGameRepl.setPlayerColor(null); // Set color to null for observers
        inGameRepl.setInGame(true); // Set client state to in-game (observing)

        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Successfully observing game " + gameID
                + EscapeSequences.RESET_TEXT_COLOR);
        // Draw the initial board state immediately (from White's perspective for
        // observers)
        inGameRepl.drawBoard();
        System.out.println(
                EscapeSequences.SET_TEXT_COLOR_BLUE + "Type 'help' for commands." + EscapeSequences.RESET_TEXT_COLOR);
    }

    /**
     * Triggers the server facade to clear the database.
     * Intended for debugging access from REPLs. Does not reset client state.
     */
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
        // Check the InGameRepl's state directly
        return inGameRepl != null && inGameRepl.isInGame();
    }

    public String getAuthToken() {
        return authToken;
    }
}