package ui;

import client.ChessClient;
import java.util.Scanner;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class PostLoginRepl {
    private final ChessClient client;
    private final Scanner scanner;
    private List<HashMap<String, Object>> currentGameList; // Stores the last fetched game list

    public PostLoginRepl(ChessClient client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
        this.currentGameList = new ArrayList<>();
    }

    /**
     * Runs the post-login command loop.
     *
     * @return true if the user wants to quit the application, false otherwise.
     */
    public boolean run() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + "Logged in. Type 'help' for options."
                + EscapeSequences.RESET_TEXT_COLOR);

        while (client.isLoggedIn() && !client.isInGameActive()) {
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

            try {
                switch (command) {
                    case "help":
                        displayHelp();
                        break;
                    case "logout":
                        handleLogout();
                        break; // Loop condition client.isLoggedIn() will become false
                    case "create":
                        handleCreateGame(Arrays.copyOfRange(args, 1, args.length));
                        break;
                    case "list":
                        handleListGames();
                        break;
                    case "join":
                        handleJoinGame(Arrays.copyOfRange(args, 1, args.length));
                        break; // Loop condition !client.isInGameActive() will become false if join succeeds
                    case "observe":
                        handleObserveGame(Arrays.copyOfRange(args, 1, args.length));
                        break; // Loop condition !client.isInGameActive() will become false if observe succeeds
                    case "quit":
                        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Exiting application."
                                + EscapeSequences.RESET_TEXT_COLOR);
                        return true;
                    default:
                        printError("Unknown command. Type 'help' for options.");
                }
            } catch (Exception e) {
                printError("Command failed: " + e.getMessage());
            }
        }
        return !client.isLoggedIn(); // Return true if quit via logout, false if entered game
    }

    private void displayHelp() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Available commands:");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  help" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "                            - Show this help message");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  logout" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "                          - Log out");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  create <NAME>"
                + EscapeSequences.SET_TEXT_COLOR_WHITE + "                 - Create a new game");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  list" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "                            - List all available games");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  join <#> [WHITE|BLACK]"
                + EscapeSequences.SET_TEXT_COLOR_WHITE + "        - Join game number # as a player");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  observe <#>"
                + EscapeSequences.SET_TEXT_COLOR_WHITE + "                   - Observe game number #");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  quit" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "                            - Exit the program");
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
    }

    private void handleLogout() {
        try {
            client.logout();
            // Use GREEN for successful logout message
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
            // Success message printed by ChessClient
        } catch (Exception e) {
            printError("Failed to create game: " + e.getMessage());
        }
    }

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
                    Object idObj = game.get("gameID");
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

    private void handleJoinGame(String[] args) {
        // Updated help: join takes color, observe is separate command
        if (args.length != 2) {
            printError("Usage: join <GameNumber> [WHITE|BLACK]");
            return;
        }
        int gameNumber;
        try {
            gameNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            printError("Invalid game number: " + args[0]);
            return;
        }

        String playerColor = args[1].toUpperCase();
        if (!playerColor.equals("WHITE") && !playerColor.equals("BLACK")) {
            printError("Invalid color specified. Use WHITE or BLACK.");
            return;
        }

        try {
            int actualGameID = mapGameNumberToID(gameNumber);
            client.joinGame(actualGameID, playerColor);
            // Success messages and state change handled by ChessClient
        } catch (NumberFormatException e) {
            printError("Invalid game ID format in stored list for game number " + gameNumber);
        } catch (Exception e) {
            printError("Failed to join game: " + e.getMessage());
        }
    }

    private void handleObserveGame(String[] args) {
        if (args.length != 1) {
            printError("Usage: observe <GameNumber>");
            return;
        }
        int gameNumber;
        try {
            gameNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            printError("Invalid game number: " + args[0]);
            return;
        }

        try {
            int actualGameID = mapGameNumberToID(gameNumber);
            client.observeGame(actualGameID);
            // Success messages and state change handled by ChessClient
        } catch (NumberFormatException e) {
            printError("Invalid game ID format in stored list for game number " + gameNumber);
        } catch (Exception e) {
            printError("Failed to observe game: " + e.getMessage());
        }
    }

    private int mapGameNumberToID(int gameNumber) throws Exception {
        if (currentGameList == null || currentGameList.isEmpty()) {
            throw new Exception("No game list available. Use 'list' command first.");
        }
        if (gameNumber < 1 || gameNumber > currentGameList.size()) {
            throw new Exception(
                    "Invalid game number: " + gameNumber + ". Must be between 1 and " + currentGameList.size());
        }
        HashMap<String, Object> game = currentGameList.get(gameNumber - 1);
        Object gameIDObj = game.get("gameID");
        if (gameIDObj == null) {
            throw new Exception("Game data is missing the game ID for selected game number.");
        }
        try {
            // Game ID often comes back as Double from JSON parsing
            return (int) Double.parseDouble(gameIDObj.toString());
        } catch (NumberFormatException | ClassCastException e) {
            throw new Exception("Invalid game ID format in stored data: " + gameIDObj);
        }
    }

    private void printError(String message) {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: " + message + EscapeSequences.RESET_TEXT_COLOR);
    }
}