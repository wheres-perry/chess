package ui;

import client.ChessClient;
import java.util.Scanner;

public class InGameRepl {
    private final ChessClient client;
    private final Scanner scanner;
    private boolean inGame; // Flag to indicate if currently in an active game context

    public InGameRepl(ChessClient client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
        this.inGame = false;
    }

    /**
     * Runs the in-game command loop.
     * Should be called only when `inGame` is true.
     * 
     * @return true if the user wants to quit the application, false otherwise.
     */
    public boolean run() {
        // Initial board draw should happen when joining/observing in ChessClient
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "Entered game mode. Type 'help' for commands.");
        while (inGame) {
            System.out.print(
                    EscapeSequences.RESET_BG_COLOR + EscapeSequences.RESET_TEXT_COLOR + "[" + client.getCurrentUser()
                            + " - In Game] >>> " + EscapeSequences.SET_TEXT_COLOR_GREEN);
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
                    // TODO: Add cases for 'move', 'redraw', 'leave', 'resign', 'highlight'
                    case "leave":
                        handleLeave(); // This should set inGame = false
                        break;
                    case "quit": // Allow quit from in-game
                        // Consider prompting if they want to resign or just quit client
                        return true;
                    default:
                        System.out.println(
                                EscapeSequences.SET_TEXT_COLOR_RED + "Unknown command in game. Type 'help' for options."
                                        + EscapeSequences.RESET_TEXT_COLOR);
                }
            } catch (Exception e) {
                System.out.println(
                        EscapeSequences.SET_TEXT_COLOR_RED + "Error: " + e.getMessage()
                                + EscapeSequences.RESET_TEXT_COLOR);
            }
        }
        return false; // Returned to PostLogin state, don't quit application yet
    }

    private void displayHelp() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "In-Game commands:");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  help" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "           - Show this help message");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  redraw" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "         - Redraw the chessboard");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  leave" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "          - Leave the current game");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  move <FROM> <TO> [PROMOTION]"
                + EscapeSequences.SET_TEXT_COLOR_WHITE + " - Make a move (e.g., move e2 e4)");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  resign" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "         - Resign the game");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  highlight <SQUARE>"
                + EscapeSequences.SET_TEXT_COLOR_WHITE
                + " - Show legal moves for piece at SQUARE (e.g., highlight e2)");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "  quit" + EscapeSequences.SET_TEXT_COLOR_WHITE
                + "           - Exit the program");
        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        // TODO: Implement these commands
    }

    private void handleLeave() throws Exception {
        // TODO: Call a corresponding method in ChessClient if server interaction is
        // needed
        // client.leaveGame(); // Example if server needs notification
        this.inGame = false;
        System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "Leaving game." + EscapeSequences.RESET_TEXT_COLOR);
        // Loop condition `inGame` becomes false, returning control to ChessClient main
        // loop
    }

    // --- Getters and Setters ---
    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    // TODO: Add methods for handling move, resign, redraw etc.

    // TODO: Add board drawing method(s) potentially called by ChessClient
}
