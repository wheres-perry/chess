// client/src/main/java/ui/PostLoginRepl.java
package ui;

import client.ChessClient;
import client.ServerFacade; // Import for exception handling

import java.util.Scanner;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Handles user interactions after they have successfully logged in,
 * but before they have joined or started observing a game.
 * Allows users to create, list, join, or observe games, or logout.
 */
public class PostLoginRepl {
    private final ChessClient client;
    private final Scanner scanner;
    // Stores the last fetched game list to map user input numbers to actual game
    // IDs
    private List<HashMap<String, Object>> currentGameList;

    public PostLoginRepl(ChessClient client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
        this.currentGameList = new ArrayList<>(); // Initialize empty list
    }

    /**
     * Runs the post-login command loop. This loop continues as long as the user is
     * logged in
     * and not actively in a game.
     *
     * @return true if the user enters the 'quit' command, false otherwise (e.g., if
     *         they log out
     *         or successfully join/observe a game, transitioning to another state).
     */
    public boolean run() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Logged in. Type 'help' for options."
                + EscapeSequences.RESET_TEXT_COLOR);

        // Loop continues as long as the client is logged in AND not in an active game
        while (client.isLoggedIn() && !client.isInGameActive()) {
            System.out.print(EscapeSequences.RESET // Reset all formatting before the prompt
                    + EscapeSequences.SET_TEXT_COLOR_WHITE + "[LOGGED_IN] "
                    + EscapeSequences.SET_TEXT_COLOR_DARK_GREY + EscapeSequences.SET_TEXT_BLINKING + ">> " // Lobby
                                                                                                           // prompt
                                                                                                           // indicator
                    + EscapeSequences.SET_TEXT_COLOR_GREEN); // Color for user input

            String line = scanner.nextLine().trim();
            System.out.print(EscapeSequences.RESET_TEXT_COLOR); // Reset color after input
            String[] args = line.split("\\s+");
            if (args.length == 0 || args[0].isEmpty()) // Handle empty input line
                continue;
            String command = args[0].toLowerCase();

            try {
                switch (command) {
                    case "help":
                        displayHelp();
                        break;
                    case "logout":
                        handleLogout();
                        // Loop condition client.isLoggedIn() will become false, loop terminates
                        break;
                    case "create":
                        handleCreateGame(Arrays.copyOfRange(args, 1, args.length));
                        break;
                    case "list":
                        handleListGames();
                        break;
                    case "join":
                        handleJoinGame(Arrays.copyOfRange(args, 1, args.length));
                        // If join succeeds, client.isInGameActive() becomes true, loop terminates
                        break;
                    case "observe":
                        handleObserveGame(Arrays.copyOfRange(args, 1, args.length));
                        // If observe succeeds, client.isInGameActive() becomes true, loop terminates
                        break;
                    case "quit":
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Exiting application."
                                + EscapeSequences.RESET_TEXT_COLOR);
                        return true; // Signal to ChessClient to terminate
                    default:
                        printError("Unknown command. Type 'help' for options.");
                }
            } catch (Exception e) {
                // Catch exceptions from handlers (including ServerException)
                printError("Command failed: " + e.getMessage());
                // Optionally print stack trace for debugging REPL command errors
                if (!(e instanceof ServerFacade.ServerException)) {
                    e.printStackTrace();
                }
            }
        }
        // Loop terminated either by logging out (returns false) or entering a game
        // (returns false)
        // or quitting (returned true inside loop)
        return !client.isLoggedIn(); // Return true only if loop exited due to quit command handled above
    }

    private void displayHelp() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Available commands:");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  help" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "                  - Show this help message");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  logout" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "                - Log out and return to the pre-login menu");
        System.out.println(
                EscapeSequences.SET_TEXT_COLOR_YELLOW + "  create <NAME>" + EscapeSequences.SET_TEXT_COLOR_WHITE
                        + "           - Create a new game with the given NAME");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  list" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "                  - List all available games on the server");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  join <#> [WHITE|BLACK]"
                + EscapeSequences.SET_TEXT_COLOR_WHITE
                + " - Join game number # (from 'list') as WHITE or BLACK player");
        System.out
                .println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  observe <#>" + EscapeSequences.SET_TEXT_COLOR_WHITE
                        + "           - Observe game number # (from 'list')");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  quit" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "                  - Exit the program");
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }

    private void handleLogout() {
        try {
            client.logout();
            // Success message printed here for immediate feedback in this REPL
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Logged out successfully."
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
        // Join arguments to allow game names with spaces
        String gameName = String.join(" ", args);
        try {
            client.createGame(gameName);
            // Success message is printed by ChessClient upon successful API call
        } catch (Exception e) {
            printError("Failed to create game: " + e.getMessage());
        }
    }

    /**
     * Fetches and displays the list of games from the server. Stores the list for
     * join/observe commands.
     */
    private void handleListGames() {
        try {
            // Fetch the latest list from the client (which calls the server)
            this.currentGameList = client.listGames();

            System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Available games:");
            if (currentGameList == null || currentGameList.isEmpty()) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  No games available."
                        + EscapeSequences.RESET_TEXT_COLOR);
            } else {
                // Iterate through the stored list and display user-friendly info
                for (int i = 0; i < currentGameList.size(); i++) {
                    HashMap<String, Object> game = currentGameList.get(i);
                    // Safely extract game data with defaults
                    String name = game.getOrDefault("gameName", "Unnamed Game").toString();
                    // White and Black usernames might be null if spot is empty
                    String whiteUser = game.get("whiteUsername") != null ? game.get("whiteUsername").toString()
                            : "<available>";
                    String blackUser = game.get("blackUsername") != null ? game.get("blackUsername").toString()
                            : "<available>";

                    // Display format: #. Name (White: userW, Black: userB)
                    // Game ID is kept internal, not shown to the user
                    System.out.printf(
                            EscapeSequences.SET_TEXT_COLOR_YELLOW + "  %d: " + EscapeSequences.SET_TEXT_COLOR_WHITE
                                    + "%s (White: %s, Black: %s)%n",
                            i + 1, // User-friendly number starting from 1
                            name,
                            whiteUser,
                            blackUser);
                }
                System.out.print(EscapeSequences.RESET_TEXT_COLOR);
            }
        } catch (Exception e) {
            printError("Failed to list games: " + e.getMessage());
            this.currentGameList = new ArrayList<>(); // Reset list on error to prevent using stale data
        }
    }

    /**
     * Handles the 'join' command, parsing arguments and calling the client method.
     */
    private void handleJoinGame(String[] args) {
        // Expecting: join <GameNumber> <COLOR>
        if (args.length != 2) {
            printError("Usage: join <GameNumber> [WHITE|BLACK]");
            printError("  (Use 'list' command to see game numbers)");
            return;
        }
        int gameNumber;
        try {
            // Parse the user-provided number (1-based index)
            gameNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            printError("Invalid game number: '" + args[0] + "'. Must be an integer.");
            return;
        }

        String playerColor = args[1].toUpperCase(); // Convert color to uppercase for comparison
        if (!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) {
            printError("Invalid color: '" + args[1] + "'. Use WHITE or BLACK.");
            return;
        }

        try {
            // Map the user's 1-based number to the actual game ID from the stored list
            int actualGameID = mapGameNumberToID(gameNumber);
            client.joinGame(actualGameID, playerColor);
            // Success messages and state changes (including drawing the board) are handled
            // by ChessClient/InGameRepl
        } catch (IndexOutOfBoundsException e) {
            printError("Invalid game number: " + gameNumber + ". No game found with that number in the last list.");
            printError("  Use 'list' to see current game numbers.");
        } catch (NumberFormatException e) {
            // This can happen if the gameID in the stored map isn't a valid number
            printError("Internal error: Invalid game ID format stored for game number " + gameNumber);
        } catch (Exception e) {
            // Catch other errors from mapGameNumberToID or client.joinGame
            printError("Failed to join game: " + e.getMessage());
        }
    }

    /**
     * Handles the 'observe' command, parsing arguments and calling the client
     * method.
     */
    private void handleObserveGame(String[] args) {
        // Expecting: observe <GameNumber>
        if (args.length != 1) {
            printError("Usage: observe <GameNumber>");
            printError("  (Use 'list' command to see game numbers)");
            return;
        }
        int gameNumber;
        try {
            // Parse the user-provided number (1-based index)
            gameNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            printError("Invalid game number: '" + args[0] + "'. Must be an integer.");
            return;
        }

        try {
            // Map the user's 1-based number to the actual game ID from the stored list
            int actualGameID = mapGameNumberToID(gameNumber);
            client.observeGame(actualGameID);
        } catch (IndexOutOfBoundsException e) {
            printError("Invalid game number: " + gameNumber + ". No game found with that number in the last list.");
            printError("  Use 'list' to see current game numbers.");
        } catch (NumberFormatException e) {
            printError("Internal error: Invalid game ID format stored for game number " + gameNumber);
        } catch (Exception e) {
            printError("Failed to observe game: " + e.getMessage());
        }
    }

    /**
     * Maps the 1-based game number entered by the user to the actual gameID stored
     * in the currentGameList.
     * Requires that 'list' command was run previously to populate currentGameList.
     *
     * @param gameNumber The 1-based number entered by the user.
     * @return The actual integer gameID.
     * @throws IndexOutOfBoundsException If the gameNumber is out of the bounds of
     *                                   the stored list.
     * @throws NumberFormatException     If the stored gameID is not a valid integer
     *                                   format.
     * @throws IllegalStateException     If the game list is empty or null (list
     *                                   command wasn't run).
     */
    private int mapGameNumberToID(int gameNumber)
            throws IndexOutOfBoundsException, NumberFormatException, IllegalStateException {
        if (currentGameList == null || currentGameList.isEmpty()) {
            throw new IllegalStateException("No game list available. Use 'list' command first.");
        }
        // Adjust 1-based user input to 0-based list index
        int listIndex = gameNumber - 1;

        // Check if the index is valid for the current list size
        if (listIndex < 0 || listIndex >= currentGameList.size()) {
            throw new IndexOutOfBoundsException(
                    "Game number " + gameNumber + " is out of range (1-" + currentGameList.size() + ").");
        }

        // Get the game map from the list
        HashMap<String, Object> game = currentGameList.get(listIndex);
        Object gameIDObj = game.get("gameID"); // Key from GameData record

        if (gameIDObj == null) {
            throw new NumberFormatException("Game data is missing the game ID for selected game number " + gameNumber);
        }

        try {
            // Game ID often comes back as Double from JSON parsing, handle robustly
            if (gameIDObj instanceof Number) {
                return ((Number) gameIDObj).intValue();
            } else {
                // Try parsing as string if it's not directly a number
                return Integer.parseInt(gameIDObj.toString());
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid game ID format (" + gameIDObj.getClass().getSimpleName()
                    + ") in stored data: " + gameIDObj);
        }
    }

    /** Prints an error message to the console in red. */
    private void printError(String message) {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: " + message + EscapeSequences.RESET_TEXT_COLOR);
    }
}