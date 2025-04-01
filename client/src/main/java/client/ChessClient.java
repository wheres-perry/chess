package client;

import ui.PreLoginRepl;
import ui.PostLoginRepl;

import java.util.List;
import java.util.HashMap;

import ui.InGameRepl;

public class ChessClient {
    private final ServerFacade server;
    private String authToken;
    private String currentUser;

    private final PreLoginRepl preLoginRepl;
    private final PostLoginRepl postLoginRepl;
    private final InGameRepl inGameRepl;

    public ChessClient(String serverUrl) {
        this.server = new ServerFacade(serverUrl);
        this.preLoginRepl = new PreLoginRepl(this);
        this.postLoginRepl = new PostLoginRepl(this);
        this.inGameRepl = new InGameRepl(this);
    }

    public void run() {
        System.out.println("Welcome to the Chess Game!");
        while (true) {
            try {
                if (authToken == null) {
                    if (preLoginRepl.run()) {
                        break;
                    }
                } else if (inGameRepl.isInGame()) {
                    if (inGameRepl.run()) {
                        break;
                    }
                } else {
                    if (postLoginRepl.run()) {
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Thanks for playing!");
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
        server.logout(authToken);
        authToken = null;
        currentUser = null;
    }

    public void createGame(String gameName) throws Exception {
        HashMap<String, Object> response = server.createGame(authToken, gameName);
        System.out.println("Game created with ID: " + response.get("gameID"));
    }

    public void listGames() throws Exception {
        HashMap<String, Object> response = server.listGames(authToken);
        // Assuming the response contains a "games" key with a list of game objects
        System.out.println("Available games:");
        for (HashMap<String, Object> game : (List<HashMap<String, Object>>) response.get("games")) {
            System.out.println("  ID: " + game.get("gameID") + ", Name: " + game.get("gameName"));
        }
    }

    public void joinGame(int gameID, String color) throws Exception {
        server.joinGame(authToken, gameID, color);
        System.out.println("Successfully joined game " + gameID + " as " + color);
        inGameRepl.setInGame(true);
    }

    public void observeGame(int gameID) throws Exception {
        server.joinGame(authToken, gameID, null); // null color means observe
        System.out.println("Now observing game " + gameID);
        inGameRepl.setInGame(true);
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return authToken != null;
    }
}
