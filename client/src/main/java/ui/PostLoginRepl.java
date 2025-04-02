package ui;

import client.ChessClient;
import java.util.Scanner;
import java.util.Arrays;
import java.util.List; // Imported List
import java.util.HashMap; // Imported HashMap
import java.util.ArrayList; // Imported ArrayList

public class PostLoginRepl {
    private final ChessClient client;
    private final Scanner scanner;
    // Field added to store the last fetched game list
    private List<HashMap<String, Object>> currentGameList;

    public PostLoginRepl(ChessClient client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
        this.currentGameList = new ArrayList<>(); // Initialize the list
    }

    /**
     * Runs the post-login command loop.
     *
     * @return true if the user wants to quit the application, false otherwise.
     */
    public boolean run() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Logged in. Type 'help' for options.");
        // Loop condition uses the new client method to check game state
        while (client.isLoggedIn() && !client.isClientInGame()) {
            System.out.print(EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR
                    + EscapeSequences.SET_TEXT_COLOR_WHITE + "LOBBY "
                    + EscapeSequences.SET_TEXT_COLOR_DARK_GREY
                    + EscapeSequences.SET_TEXT_BLINKING + "> "
                    + EscapeSequences.SET_TEXT_COLOR_GREEN);
            String line = scanner.nextLine().trim();
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);
            String[] args = line.split("\\s+");
            if (args.length == 0)
                continue;
            String command = args[0].toLowerCase();

            // Use try-with-resources or explicit handling for Scanner if needed elsewhere
            // For this REPL loop, leaving scanner open is typical.

            try {
                switch (command) {
                    case "help":
                        displayHelp();
                        break;
                    case "logout":
                        handleLogout();
                        // Loop condition client.isLoggedIn() will become false
                        break;
                    case "create":
                        handleCreateGame(Arrays.copyOfRange(args, 1, args.length));
                        break;
                    case "list":
                        handleListGames(); // This now stores the list
                        break;
                    case "join":
                        handleJoinGame(Arrays.copyOfRange(args, 1, args.length));
                        // Loop condition !client.isClientInGame() will become false if join succeeds
                        break;
                    case "observe":
                        handleObserveGame(Arrays.copyOfRange(args, 1, args.length));
                        // Loop condition !client.isClientInGame() will become false if observe succeeds
                        break;
                    case "quit":
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Exiting application."
                                + EscapeSequences.RESET_TEXT_COLOR);
                        return true; // Signal exit
                    default:
                        printError("Unknown command. Type 'help' for options.");
                }
            } catch (Exception e) {
                // General error handling for commands
                printError("Command failed: " + e.getMessage());
            }
        }
        // Return true if the loop exited because the user logged out (and didn't quit
        // directly)
        // Return false if the loop exited because the user entered a game
        // (isClientInGame() became true)
        return !client.isLoggedIn();
    }

    // Helper is removed as the check is now done via client.isClientInGame() in the
    // loop condition

    private void displayHelp() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Available commands:");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  help" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "          - Show this help message");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  logout" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "        - Log out");
        System.out.println(
                EscapeSequences.SET_TEXT_COLOR_YELLOW + "  create <NAME>" + EscapeSequences.SET_TEXT_COLOR_WHITE
                        + " - Create a new game");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  list" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "          - List all available games");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  join <#> [WHITE|BLACK|<empty>]"
                + EscapeSequences.SET_TEXT_COLOR_WHITE
                + " - Join game number # as player (WHITE/BLACK) or observer (empty color)");
        System.out
                .println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  observe <#>" + EscapeSequences.SET_TEXT_COLOR_WHITE
                        + "  - Observe game number #");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  quit" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "          - Exit the program");
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }

    private void handleLogout() {
        try {
            client.logout();
            System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Logged out successfully."
                    + EscapeSequences.RESET_TEXT_COLOR);
        } catch (Exception e) {
            printError("Logout failed: " + e.getMessage());
        }
    }

    private void handleCreateGame(String[] args) {
        if (args.length < 1) {
            printError("Usage: create <NAME>");
            return;
        }
        String gameName = String.join(" ", args);
        try {
            client.createGame(gameName);
            // Success message is now printed by ChessClient upon successful creation
        } catch (Exception e) {
            printError("Failed to create game: " + e.getMessage());
        }
    }

    private void handleListGames() {
        try {
            // Store the fetched list
            this.currentGameList = client.listGames();

            // Print the list with 1-based numbering
            System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Available games:");
            if (currentGameList == null || currentGameList.isEmpty()) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  No games available."
                        + EscapeSequences.RESET_TEXT_COLOR);
            } else {
                for (int i = 0; i < currentGameList.size(); i++) {
                    HashMap<String, Object> game = currentGameList.get(i);
                    // Safely get properties, handle potential missing keys or wrong types
                    String name = game.getOrDefault("gameName", "Unnamed Game").toString();
                    Object idObj = game.get("gameID"); // ID might be Double due to JSON parsing
                    String idStr = (idObj != null) ? String.format("%.0f", Double.parseDouble(idObj.toString()))
                            : "N/A";
                    String whiteUser = game.getOrDefault("whiteUsername", "<available>").toString();
                    String blackUser = game.getOrDefault("blackUsername", "<available>").toString();

                    System.out.printf(
                            EscapeSequences.SET_TEXT_COLOR_YELLOW + "  %d: " + EscapeSequences.SET_TEXT_COLOR_WHITE
                                    + "%s (ID: %s) - White: %s, Black: %s%n",
                            i + 1, name, idStr, whiteUser, blackUser);
                }
                System.out.print(EscapeSequences.RESET_TEXT_COLOR);
            }
        } catch (Exception e) {
            printError("Failed to list games: " + e.getMessage());
            this.currentGameList = new ArrayList<>(); // Reset list on error
        }
    }

    // Handles both joining as player and joining as observer
    private void handleJoinGame(String[] args) {
        if (args.length < 1 || args.length > 2) {
            printError("Usage: join <GameNumber> [WHITE|BLACK|<leave empty to observe>]");
            return;
        }
        int gameNumber;
        try {
            gameNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            printError("Invalid game number: " + args[0]);
            return;
        }

        String playerColor = null; // Default to observer
        if (args.length == 2) {
            playerColor = args[1].toUpperCase();
            if (!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) {
                printError("Invalid color specified. Use WHITE or BLACK, or leave empty to observe.");
                return;
            }
        }

        try {
            int actualGameID = mapGameNumberToID(gameNumber); // Get the real ID
            // Call the appropriate client method based on color
            if (playerColor != null) {
                client.joinGame(actualGameID, playerColor);
            } else {
                client.observeGame(actualGameID); // Observing is joining with null color
            }
            // Success messages and state change (inGameRepl.setInGame(true)) are handled by
            // ChessClient
            // The loop condition will detect the state change on the next iteration
        } catch (NumberFormatException e) {
            printError("Invalid game ID format in stored list for game number " + gameNumber);
        } catch (Exception e) {
            printError("Failed to join/observe game: " + e.getMessage());
        }
    }

    // Observe is now handled by 'join' with no color specified.
    // This method can be kept for explicit observe command if desired.
    private void handleObserveGame(String[] args) {
        if (args.length != 1) {
            printError("Usage: observe <GameNumber>");
            return;
        }
        // Reuse the join logic with null color
        handleJoinGame(new String[] { args[0] }); // Pass game number, color will be null
    }

    // Implemented mapping using the stored game list
    private int mapGameNumberToID(int gameNumber) throws Exception {
        if (currentGameList == null || currentGameList.isEmpty()) {
            throw new Exception("No game list available. Use 'list' command first.");
        }
        if (gameNumber < 1 || gameNumber > currentGameList.size()) {
            throw new Exception(
                    "Invalid game number: " + gameNumber + ". Must be between 1 and " + currentGameList.size());
        }
        // Get the game map using 0-based index
        HashMap<String, Object> game = currentGameList.get(gameNumber - 1);
        Object gameIDObj = game.get("gameID");
        if (gameIDObj == null) {
            throw new Exception("Game data is missing the game ID for selected game number.");
        }
        // Game ID often comes back as Double from JSON parsing, convert carefully
        try {
            return (int) Double.parseDouble(gameIDObj.toString());
        } catch (NumberFormatException | ClassCastException e) {
            throw new Exception("Invalid game ID format in stored data: " + gameIDObj);
        }
    }

    // Helper method for printing errors consistently
    private void printError(String message) {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: " + message + EscapeSequences.RESET_TEXT_COLOR);
    }
}