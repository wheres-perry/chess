package ui;

import client.ChessClient;

import java.util.Scanner;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Handles user interactions after they have successfully logged in,
 * but before they have joined or started observing a game.
 */
public class PostLoginRepl {
    private final ChessClient client;
    private final Scanner scanner;
    private List<HashMap<String, Object>> currentGameList;

    public PostLoginRepl(ChessClient client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
        this.currentGameList = new ArrayList<>();
    }

    /**
     * Runs the post-login command loop.
     * 
     * @return true if the user enters the 'quit' command, false otherwise.
     */
    public boolean run() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Logged in. Type 'help' for options."
                + EscapeSequences.RESET_TEXT_COLOR);

        while (client.isLoggedIn() && !client.isInGameActive()) {
            System.out.print(EscapeSequences.RESET
                    + EscapeSequences.SET_TEXT_COLOR_WHITE + "[LOGGED_IN] "
                    + EscapeSequences.SET_TEXT_COLOR_DARK_GREY + EscapeSequences.SET_TEXT_BLINKING + ">> "
                    + EscapeSequences.SET_TEXT_COLOR_GREEN);

            String line = scanner.nextLine().trim();
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);
            String[] args = line.split("\\s+");
            if (args.length == 0 || args[0].isEmpty())
                continue;
            String command = args[0].toLowerCase();

            try {
                switch (command) {
                    case "help":
                        displayHelp();
                        break;
                    case "logout":
                        handleLogout();
                        break;
                    case "create":
                        handleCreateGame(Arrays.copyOfRange(args, 1, args.length));
                        break;
                    case "list":
                        handleListGames();
                        break;
                    case "join":
                        handleJoinGame(Arrays.copyOfRange(args, 1, args.length));
                        break;
                    case "observe":
                        handleObserveGame(Arrays.copyOfRange(args, 1, args.length));
                        break;
                    case "quit":
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Exiting application."
                                + EscapeSequences.RESET_TEXT_COLOR);
                        return true;
                    default:
                        printError("Unknown command. Type 'help' for options.");
                }
            } catch (Exception e) {
                printError("Command failed (Client): " + e.getMessage());
                e.printStackTrace();
            }
        }
        return !client.isLoggedIn();
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
        String gameName = String.join(" ", args);
        try {
            client.createGame(gameName);
        } catch (Exception e) {
            printError("Failed to create game: " + e.getMessage());
        }
    }

    /**
     * Fetches and displays the list of games.
     */
    private void handleListGames() {
        try {
            this.currentGameList = client.listGames();

            System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Available games:");
            if (currentGameList == null || currentGameList.isEmpty()) {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  No games available."
                        + EscapeSequences.RESET_TEXT_COLOR);
            } else {
                for (int i = 0; i < currentGameList.size(); i++) {
                    HashMap<String, Object> game = currentGameList.get(i);
                    String name = game.getOrDefault("gameName", "Unnamed Game").toString();
                    String whiteUser = game.get("whiteUsername") != null ? game.get("whiteUsername").toString()
                            : "<available>";
                    String blackUser = game.get("blackUsername") != null ? game.get("blackUsername").toString()
                            : "<available>";

                    System.out.printf(
                            EscapeSequences.SET_TEXT_COLOR_YELLOW + "  %d: " + EscapeSequences.SET_TEXT_COLOR_WHITE
                                    + "%s (White: %s, Black: %s)%n",
                            i + 1,
                            name,
                            whiteUser,
                            blackUser);
                }
                System.out.print(EscapeSequences.RESET_TEXT_COLOR);
            }
        } catch (Exception e) {
            printError("Failed to list games: " + e.getMessage());
            this.currentGameList = new ArrayList<>();
        }
    }

    /**
     * Handles the 'join' command.
     */
    private void handleJoinGame(String[] args) {
        if (args.length != 2) {
            printError("Usage: join <GameNumber> [WHITE|BLACK]");
            printError("  (Use 'list' command to see game numbers)");
            return;
        }
        int gameNumber;
        try {
            gameNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            printError("Invalid game number: '" + args[0] + "'. Must be an integer.");
            return;
        }

        String playerColor = args[1].toUpperCase();
        if (!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) {
            printError("Invalid color: '" + args[1] + "'. Use WHITE or BLACK.");
            return;
        }

        try {
            int actualGameID = mapGameNumberToID(gameNumber);
            client.joinGame(actualGameID, playerColor);
        } catch (IndexOutOfBoundsException | IllegalStateException e) {
            printError(e.getMessage());
            printError("  Use 'list' to see current game numbers.");
        } catch (NumberFormatException e) {
            printError("Internal error: Invalid game ID format stored for game number " + gameNumber);
        } catch (Exception e) {
            printError("Failed to join game: " + e.getMessage());
        }
    }

    /**
     * Handles the 'observe' command.
     */
    private void handleObserveGame(String[] args) {
        if (args.length != 1) {
            printError("Usage: observe <GameNumber>");
            printError("  (Use 'list' command to see game numbers)");
            return;
        }
        int gameNumber;
        try {
            gameNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            printError("Invalid game number: '" + args[0] + "'. Must be an integer.");
            return;
        }

        try {
            int actualGameID = mapGameNumberToID(gameNumber);
            client.observeGame(actualGameID);
        } catch (IndexOutOfBoundsException | IllegalStateException e) {
            printError(e.getMessage());
            printError("  Use 'list' to see current game numbers.");
        } catch (NumberFormatException e) {
            printError("Internal error: Invalid game ID format stored for game number " + gameNumber);
        } catch (Exception e) {
            printError("Failed to observe game: " + e.getMessage());
        }
    }

    /**
     * Maps user's 1-based game number to the actual gameID.
     * 
     * @param gameNumber The 1-based number from user input.
     * @return The actual integer gameID.
     * @throws IndexOutOfBoundsException If number is invalid.
     * @throws NumberFormatException     If stored ID is invalid.
     * @throws IllegalStateException     If list is empty/null.
     */
    private int mapGameNumberToID(int gameNumber)
            throws IndexOutOfBoundsException, NumberFormatException, IllegalStateException {
        if (currentGameList == null || currentGameList.isEmpty()) {
            throw new IllegalStateException("No game list available. Use 'list' command first.");
        }
        int listIndex = gameNumber - 1;

        if (listIndex < 0 || listIndex >= currentGameList.size()) {
            throw new IndexOutOfBoundsException(
                    "Game number " + gameNumber + " is out of range (1-" + currentGameList.size() + ").");
        }

        HashMap<String, Object> game = currentGameList.get(listIndex);
        Object gameIDObj = game.get("gameID");

        if (gameIDObj == null) {
            throw new NumberFormatException("Game data is missing the game ID for selected game number " + gameNumber);
        }

        try {
            if (gameIDObj instanceof Number) {
                return ((Number) gameIDObj).intValue();
            } else {
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