package ui;

import client.ChessClient;
import java.util.Scanner;
import java.util.Arrays;
// TODO: Import necessary classes for game list storage if done here

public class PostLoginRepl {
    private final ChessClient client;
    private final Scanner scanner;
    // TODO: Add a field to store the last fetched game list for mapping numbers to
    // IDs
    // private List<HashMap<String, Object>> currentGameList;

    public PostLoginRepl(ChessClient client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Runs the post-login command loop.
     * 
     * @return true if the user wants to quit the application, false otherwise.
     */
    public boolean run() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Logged in. Type 'help' for options.");
        while (client.isLoggedIn() && !isCurrentlyInGame()) { // Check if still logged in and not in a game
            System.out.print(EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR + "["
                    + client.getCurrentUser() + "] >>> " + EscapeSequences.SET_TEXT_COLOR_GREEN);
            String line = scanner.nextLine().trim();
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);
            String[] args = line.split("\\s+");
            if (args.length == 0)
                continue;
            String command = args[0].toLowerCase();

            try {
                switch (command) {
                    case "help":
                        displayHelp();
                        break;
                    case "logout":
                        handleLogout();
                        // Loop condition client.isLoggedIn() will become false, breaking loop
                        break;
                    case "create": // Assuming "create game"
                        handleCreateGame(Arrays.copyOfRange(args, 1, args.length));
                        break;
                    case "list": // Assuming "list games"
                        handleListGames();
                        break;
                    case "join": // Assuming "play game"
                        handleJoinGame(Arrays.copyOfRange(args, 1, args.length));
                        // If join successful, isCurrentlyInGame() should become true, breaking loop
                        break;
                    case "observe": // Assuming "observe game"
                        handleObserveGame(Arrays.copyOfRange(args, 1, args.length));
                        // If observe successful, isCurrentlyInGame() should become true, breaking loop
                        break;
                    case "quit": // Allow quit from post-login as well
                        return true;
                    default:
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED
                                + "Unknown command. Type 'help' for options." + EscapeSequences.RESET_TEXT_COLOR);
                }
            } catch (Exception e) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: " + e.getMessage()
                        + EscapeSequences.RESET_TEXT_COLOR);
            }
        }
        return !client.isLoggedIn(); // Return true only if user logged out and intends to quit
    }

    // Helper to check if the client transitioned to in-game state
    private boolean isCurrentlyInGame() {
        return false; // Placeholder
    }

    private void displayHelp() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Available commands:");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  help" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "          - Show this help message");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  logout" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "        - Log out");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  create <NAME>"
                + EscapeSequences.SET_TEXT_COLOR_WHITE + " - Create a new game");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  list" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "          - List all available games");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  join <ID> [WHITE|BLACK]"
                + EscapeSequences.SET_TEXT_COLOR_WHITE + " - Join a game as a player (specify color)");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  observe <ID>"
                + EscapeSequences.SET_TEXT_COLOR_WHITE + "  - Observe a game");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  quit" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "          - Exit the program");
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }

    private void handleLogout() throws Exception {
        client.logout();
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Logged out." + EscapeSequences.RESET_TEXT_COLOR);
    }

    private void handleCreateGame(String[] args) throws Exception {
        if (args.length < 1) {
            throw new Exception("Usage: create <NAME>");
        }
        String gameName = String.join(" ", args); // Allow spaces in game name
        client.createGame(gameName);
        // Success message printed by ChessClient (or could be moved here)
    }

    private void handleListGames() throws Exception {
        // TODO: Store the fetched list here (`currentGameList`) to map numbers to IDs
        // later.
        client.listGames();
    }

    private void handleJoinGame(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) {
            throw new Exception("Usage: join <ID> [WHITE|BLACK]");
        }
        int gameNumber;
        try {
            gameNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new Exception("Invalid game ID number: " + args[0]);
        }

        // TODO: Map gameNumber (1, 2, ...) back to the actual gameID using
        // `currentGameList`
        int actualGameID = mapGameNumberToID(gameNumber); // Need to implement this mapping

        String color = null;
        if (args.length == 2) {
            color = args[1].toUpperCase();
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                throw new Exception("Invalid color. Choose WHITE or BLACK.");
            }
        }

        client.joinGame(actualGameID, color);
        // Success message and board drawing handled by ChessClient/InGameRepl
    }

    private void handleObserveGame(String[] args) throws Exception {
        if (args.length != 1) {
            throw new Exception("Usage: observe <ID>");
        }
        int gameNumber;
        try {
            gameNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new Exception("Invalid game ID number: " + args[0]);
        }

        // TODO: Map gameNumber (1, 2, ...) back to the actual gameID using
        // `currentGameList`
        int actualGameID = mapGameNumberToID(gameNumber); // Need to implement this mapping

        client.observeGame(actualGameID);
        // Success message and board drawing handled by ChessClient/InGameRepl
    }

    // TODO: Implement this method using the stored game list
    private int mapGameNumberToID(int gameNumber) throws Exception {
        System.out.println("stub");
        return gameNumber;
    }
}