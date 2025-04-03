package client;

import ui.PreLoginRepl;
import ui.PostLoginRepl;
import ui.InGameRepl; // Ensure this is imported

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList; // Import ArrayList if needed for empty list return
import java.util.Map;

public class ChessClient {
    private final ServerFacade server;
    private String authToken;
    private String currentUser;

    private final PreLoginRepl preLoginRepl;
    private final PostLoginRepl postLoginRepl;
    private final InGameRepl inGameRepl; // Instance of InGameRepl

    public ChessClient(String serverUrl) {
        this.server = createServerFacade(serverUrl);
        // Pass 'this' (the ChessClient instance) to the REPLs
        this.preLoginRepl = new PreLoginRepl(this);
        this.postLoginRepl = new PostLoginRepl(this);
        this.inGameRepl = new InGameRepl(this);
    }

    // Add this method for testing
    protected ServerFacade createServerFacade(String serverUrl) {
        return new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println("Welcome to the Chess Game!");
        // The loop logic correctly switches between REPLs based on state
        while (true) {
            if (authToken == null) { // Not logged in
                if (preLoginRepl.run()) {
                    break; // Quit requested
                }
            } else if (isClientInGame()) { // Logged in and in a game
                if (inGameRepl.run()) { // Run InGameRepl
                    break; // Quit requested
                }
                // If inGameRepl.run() returns false, it means the user left the game,
                // so the loop continues, and the next iteration will run PostLoginRepl.
            } else { // Logged in, but not in a game
                if (postLoginRepl.run()) { // Run PostLoginRepl
                    break; // Quit requested
                }
                // If postLoginRepl.run() returns false, it means the user might have
                // joined/observed a game or logged out. The loop continues and checks state
                // again.
            }
        }
        System.out.println("Thanks for playing!");
    }

    // --- Authentication ---
    public void register(String username, String password, String email) throws Exception {
        HashMap<String, Object> response = server.register(username, password, email);
        authToken = (String) response.get("authToken");
        currentUser = username;
        // PostLoginRepl handles success message now
    }

    public void login(String username, String password) throws Exception {
        HashMap<String, Object> response = server.login(username, password);
        authToken = (String) response.get("authToken");
        currentUser = username;
        // PostLoginRepl handles success message now
    }

    public void logout() throws Exception {
        if (authToken != null) {
            server.logout(authToken);
        }
        authToken = null;
        currentUser = null;
        inGameRepl.setInGame(false); // Ensure game state is reset on logout
        // PostLoginRepl handles the success message
    }

    // --- Game Management ---
    public void createGame(String gameName) throws Exception {
        if (authToken == null)
            throw new Exception("You must be logged in to create a game.");
        HashMap<String, Object> response = server.createGame(authToken, gameName);
        // Consider moving success message to PostLoginRepl after call returns
        System.out.println("Game created successfully with ID: " + response.get("gameID"));
    }

    @SuppressWarnings("unchecked")
    public List<HashMap<String, Object>> listGames() throws Exception {
        if (authToken == null)
            throw new Exception("You must be logged in to list games.");

        HashMap<String, Object> response = server.listGames(authToken);
        List<HashMap<String, Object>> result = new ArrayList<>();

        Object gamesObj = response.get("games");

        if (gamesObj == null) {
            return result; // Return empty list if no games key
        }

        if (!(gamesObj instanceof List)) {
            throw new Exception("Received unexpected data format for games list.");
        }

        List<?> gamesList = (List<?>) gamesObj;

        for (Object gameObj : gamesList) {
            // Convert each game object to HashMap
            if (gameObj instanceof Map) {
                // Handle any Map implementation (including LinkedTreeMap from Gson)
                HashMap<String, Object> gameMap = new HashMap<>();
                gameMap.putAll((Map<? extends String, ?>) gameObj);
                result.add(gameMap);
            } else {
                System.out.println("Warning: Skipped non-map game object in response");
            }
        }

        return result;
    }

    // Join/Observe methods now set the in-game state via InGameRepl
    public void joinGame(int gameID, String color) throws Exception {
        if (authToken == null)
            throw new Exception("You must be logged in to join a game.");
        server.joinGame(authToken, gameID, color); // ServerFacade handles potential errors
        System.out.println("Successfully joined game " + gameID + (color != null ? " as " + color : " (joining)"));
        inGameRepl.setInGame(true); // Set the flag AFTER successful server call
        // Board drawing etc. should be triggered by InGameRepl or WebSocket messages
    }

    public void observeGame(int gameID) throws Exception {
        if (authToken == null)
            throw new Exception("You must be logged in to observe a game.");
        server.joinGame(authToken, gameID, null); // null color means observe
        System.out.println("Successfully observing game " + gameID);
        inGameRepl.setInGame(true); // Set the flag AFTER successful server call
        // Board drawing etc. should be triggered by InGameRepl or WebSocket messages
    }

    // --- Getters ---
    public String getCurrentUser() {
        return currentUser != null ? currentUser : "Not Logged In";
    }

    public boolean isLoggedIn() {
        return authToken != null;
    }

    // Added getter for in-game status check needed by PostLoginRepl loop
    public boolean isClientInGame() {
        return inGameRepl.isInGame();
    }

}