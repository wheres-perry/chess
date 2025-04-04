// client/src/main/java/ui/InGameRepl.java
package ui;

import chess.*;
import client.ChessClient;

import java.util.Scanner;
// import java.util.Map; // Keep commented unless map parsing is reintroduced

import static ui.EscapeSequences.*;

/**
 * Handles user interactions within an active game session (playing or
 * observing).
 * Draws the board and processes game-specific commands.
 */
public class InGameRepl {
    private final ChessClient client;
    private final Scanner scanner;
    private boolean inGame; // Flag indicating if the user is currently in a game (playing or observing)
    private ChessGame.TeamColor playerColor;

    // Constants for board drawing
    private static final int BOARD_SIZE = 8;
    private static final String[] COL_LABELS = { "a", "b", "c", "d", "e", "f", "g", "h" };
    private static final String[] ROW_LABELS = { "1", "2", "3", "4", "5", "6", "7", "8" };

    // Aliases for piece symbols
    private static final String LIGHT_SQUARE_BG = SET_BG_COLOR_LIGHT_GREY;
    private static final String DARK_SQUARE_BG = SET_BG_COLOR_DARK_GREEN;

    private static final String WHITE_PIECE_TEXT = SET_TEXT_COLOR_WHITE;
    private static final String BLACK_PIECE_TEXT = SET_TEXT_COLOR_BLACK;

    private static final String BORDER_BG = SET_BG_COLOR_DARK_GREY;
    private static final String BORDER_TEXT = SET_TEXT_COLOR_WHITE;

    public InGameRepl(ChessClient client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
        this.inGame = false;
        this.playerColor = null;
    }

    /**
     * Runs the in-game command loop. This loop continues as long as the 'inGame'
     * flag is true.
     * It prompts the user for commands specific to the game context (e.g., move,
     * redraw, resign).
     *
     * @return true if the user enters the 'quit' command (to exit the application
     *         entirely),
     *         false otherwise (e.g., if the user uses 'leave' to return to the
     *         lobby).
     */
    public boolean run() {
        if (!inGame) {
            // This should not normally happen if ChessClient logic is correct
            printError("Internal state inconsistency - entered InGameRepl but not marked as 'in game'.");
            return false;
        }

        // Initial prompt is handled by ChessClient after successful join/observe

        while (inGame) {
            String playerStatus = (playerColor == null) ? "Observing" : "Playing as " + playerColor;
            System.out.print(RESET // Reset all formatting before the prompt
                    + SET_TEXT_COLOR_WHITE + "[" + client.getCurrentUser() + " - " + playerStatus + "] "
                    + SET_TEXT_COLOR_DARK_GREY + SET_TEXT_BLINKING + ">>> "
                    + SET_TEXT_COLOR_GREEN);

            String line = scanner.nextLine().trim();
            System.out.print(RESET_TEXT_COLOR);
            String[] args = line.split("\\s+");
            if (args.length == 0 || args[0].isEmpty()) // Handle empty input line
                continue;
            String command = args[0].toLowerCase();

            try {
                switch (command) {
                    case "help":
                        displayHelp();
                        break;
                    case "redraw":
                        handleRedraw();
                        break;
                    case "leave":
                        handleLeave();
                        // 'inGame' becomes false, loop terminates, returns false to go back to
                        // PostLoginRepl
                        break;
                    case "move":
                        handleMove(args);
                        break;
                    case "resign":
                        handleResign();
                        break; //
                    case "highlight":
                        handleHighlight(args);
                        break;
                    case "quit":
                        System.out.println(SET_TEXT_COLOR_YELLOW + "Exiting application." + RESET_TEXT_COLOR);
                        return true;
                    default:
                        printError("Unknown command. Type 'help' for options.");
                }
            } catch (Exception e) {
                // Catch exceptions from command handlers (including server errors passed up)
                printError("Command failed: " + e.getMessage());
            }
        }
        return false; // Exited loop because 'inGame' became false (likely via 'leave'), not 'quit'
    }

    // --- State Management ---

    /**
     * Sets the player's color. Called by ChessClient after successfully joining a
     * game.
     * Set to null if the user is observing.
     *
     * @param color The player's TeamColor (WHITE or BLACK), or null for observers.
     */
    public void setPlayerColor(ChessGame.TeamColor color) {
        this.playerColor = color;
    }

    /**
     * Sets the in-game status. Called by ChessClient when entering or leaving a
     * game.
     *
     * @param inGame true if the user is now in a game, false otherwise.
     */
    public void setInGame(boolean inGame) {
        this.inGame = inGame;
        if (!inGame) {
            // Reset player color when leaving a game
            this.playerColor = null;
        }
    }

    /**
     * @return true if the client is currently in an active game session (playing or
     *         observing).
     */
    public boolean isInGame() {
        return inGame;
    }

    // --- Command Handlers ---

    private void displayHelp() {
        System.out.println(SET_TEXT_COLOR_BLUE + "In-Game commands:");
        System.out.println(SET_TEXT_COLOR_YELLOW + "  help" + SET_TEXT_COLOR_WHITE
                + "                - Show this help message");
        System.out.println(SET_TEXT_COLOR_YELLOW + "  redraw" + SET_TEXT_COLOR_WHITE
                + "              - Redraw the chessboard");
        System.out.println(SET_TEXT_COLOR_YELLOW + "  leave" + SET_TEXT_COLOR_WHITE
                + "               - Leave the current game (return to lobby)");
        System.out.println(SET_TEXT_COLOR_YELLOW + "  move <FROM> <TO> [PROMOTION]" + SET_TEXT_COLOR_WHITE
                + " - Make a move (e.g., move e2 e4, move a7 a8 Q)");
        System.out.println(SET_TEXT_COLOR_YELLOW + "  resign" + SET_TEXT_COLOR_WHITE
                + "              - Resign the game (forfeit)");
        System.out.println(SET_TEXT_COLOR_YELLOW + "  highlight <SQUARE>" + SET_TEXT_COLOR_WHITE
                + "    - Show legal moves for piece at SQUARE (e.g., highlight e2)");
        System.out.println(SET_TEXT_COLOR_YELLOW + "  quit" + SET_TEXT_COLOR_WHITE
                + "                - Exit the program");
        System.out.print(RESET_TEXT_COLOR);
    }

    /** Draws the current board state based on the player's perspective. */
    private void handleRedraw() {
        drawBoard();
    }

    /**
     * Handles the 'leave' command. Sets the in-game state to false, causing the
     * REPL loop to exit.
     * The actual server notification for leaving might be handled elsewhere (e.g.,
     * via WebSocket).
     */
    private void handleLeave() throws Exception {
        // Note: Actual 'leave game' logic (like sending a message to the server via
        // WebSocket)
        // would typically go here or be called from here.
        System.out.println(SET_TEXT_COLOR_YELLOW + "Leaving game." + RESET_TEXT_COLOR);
        this.setInGame(false); // This will terminate the inGameRepl loop
    }

    // Unused for now:::
    private void handleMove(String[] args) throws Exception {
        // Gameplay logic (Phase 6)
        if (playerColor == null) {
            printError("Observers cannot make moves.");
            return;
        }
        if (args.length < 3 || args.length > 4) {
            printError("Usage: move <FROM> <TO> [PROMOTION_PIECE]");
            return;
        }
        // Parsing and move validation/sending will go here in Phase 6
        ChessPosition fromPos = parsePosition(args[1]);
        ChessPosition toPos = parsePosition(args[2]);

        if (fromPos == null || toPos == null) {
            printError("Invalid square format (e.g., a1, h8).");
            return;
        }
        if (args.length == 4) {
            printError("Invalid promotion piece. Use Q, R, B, or N.");
            return;
        }
        printError("Make move functionality not implemented yet.");
    }

    private void handleResign() throws Exception {
        // Gameplay logic (Phase 6)
        if (playerColor == null) {
            printError("Observers cannot resign.");
            return;
        }
        System.out.print(SET_TEXT_COLOR_YELLOW + "Are you sure you want to resign? (yes/no): " + SET_TEXT_COLOR_WHITE);
        String confirmation = scanner.nextLine().trim().toLowerCase();
        if ("yes".equals(confirmation)) {
            // Resignation logic (sending command to server) will go here in Phase 6
            printError("Resign functionality not implemented yet.");
        } else {
            System.out.println("Resignation cancelled.");
        }
    }

    private void handleHighlight(String[] args) throws Exception {
        // Gameplay logic (Phase 6)
        if (args.length != 2) {
            printError("Usage: highlight <SQUARE>");
            return;
        }
        ChessPosition position = parsePosition(args[1]);
        if (position == null) {
            printError("Invalid square format (e.g., a1, h8).");
            return;
        }
        printError("Highlight functionality not implemented yet.");
    }

    // --- Board Drawing ---

    /**
     * Draws the initial state of the chessboard to the console, oriented based on
     * the player's perspective.
     * Observers see the board from White's perspective.
     */
    public void drawBoard() {
        // Determine perspective: Black players see the board flipped, White and
        // observers see standard orientation.
        ChessGame.TeamColor perspective = (playerColor == ChessGame.TeamColor.BLACK) ? ChessGame.TeamColor.BLACK
                : ChessGame.TeamColor.WHITE;

        // Create a new board and reset it to the initial state for drawing
        // As per spec for Phase 5: "draw the initial state of a Chess game"
        ChessBoard boardToDraw = new ChessBoard();
        boardToDraw.resetBoard();

        // Call the internal drawing method with the initial board and perspective
        drawBoardInternal(boardToDraw, perspective);
    }

    /**
     * Internal method responsible for rendering the chessboard to the console.
     *
     * @param board       The ChessBoard object containing the piece layout to draw.
     * @param perspective The TeamColor perspective (WHITE or BLACK at bottom).
     *                    Determines orientation.
     */
    private void drawBoardInternal(ChessBoard board, ChessGame.TeamColor perspective) {
        boolean whitePerspective = (perspective == ChessGame.TeamColor.WHITE);

        // Draw top border (column labels)
        drawHeaderFooter(perspective);
        System.out.println(); // Newline after top border

        // Determine row iteration order based on perspective
        int rowStart, rowEnd, rowInc;
        if (whitePerspective) {
            rowStart = BOARD_SIZE; // Start from row 8
            rowEnd = 1; // End at row 1
            rowInc = -1; // Decrement row index
        } else { // Black perspective
            rowStart = 1; // Start from row 1
            rowEnd = BOARD_SIZE; // End at row 8
            rowInc = 1; // Increment row index
        }

        // Iterate through rows based on perspective
        for (int r = rowStart; whitePerspective ? (r >= rowEnd) : (r <= rowEnd); r += rowInc) {
            // Draw left row label
            drawRowLabel(r);

            // Determine column iteration order based on perspective
            int colStart, colEnd, colInc;
            if (whitePerspective) {
                colStart = 1; // Start from column 'a' (1)
                colEnd = BOARD_SIZE; // End at column 'h' (8)
                colInc = 1; // Increment column index
            } else { // Black perspective
                colStart = BOARD_SIZE; // Start from column 'h' (8)
                colEnd = 1; // End at column 'a' (1)
                colInc = -1; // Decrement column index
            }

            // Iterate through columns based on perspective
            for (int c = colStart; whitePerspective ? (c <= colEnd) : (c >= colEnd); c += colInc) {
                ChessPosition currentPos = new ChessPosition(r, c);
                ChessPiece piece = board.getPiece(currentPos); // Get piece from the board

                // Determine square background color (h1 = light)
                // (r + c) is odd for light squares, even for dark squares
                boolean isLightSquare = (r + c) % 2 != 0;
                String bgColor = isLightSquare ? LIGHT_SQUARE_BG : DARK_SQUARE_BG;

                System.out.print(bgColor);
                printPiece(piece);
            }

            drawRowLabel(r);
            System.out.println(RESET);
        }

        drawHeaderFooter(perspective);
        System.out.println(RESET);
    }

    /** Draws the row label (number) for the given row index. */
    private void drawRowLabel(int rowIndex) {
        System.out.print(BORDER_BG + BORDER_TEXT + " " + ROW_LABELS[rowIndex - 1] + " " + RESET_BG_COLOR);
    }

    /**
     * Draws the top or bottom border of the chessboard, containing column labels.
     * The order of labels depends on the viewing perspective.
     *
     * @param perspective The perspective (WHITE or BLACK) determining label order.
     */
    private void drawHeaderFooter(ChessGame.TeamColor perspective) {
        System.out.print(BORDER_BG + BORDER_TEXT + "   "); // Padding before labels

        boolean whitePerspective = (perspective == ChessGame.TeamColor.WHITE);
        int colStart, colEnd, colInc;

        if (whitePerspective) {
            colStart = 0; // Index for 'a'
            colEnd = BOARD_SIZE; // Iterate up to (but not including) 8
            colInc = 1;
        } else { // Black perspective
            colStart = BOARD_SIZE - 1; // Index for 'h'
            colEnd = -1; // Iterate down to (but not including) -1
            colInc = -1;
        }

        // Iterate and print column labels
        for (int c = colStart; whitePerspective ? (c < colEnd) : (c > colEnd); c += colInc) {
            System.out.print(" " + COL_LABELS[c] + " "); // Print label with spacing
        }

        System.out.print("   " + RESET); // Padding after labels and reset colors
    }

    /**
     * Prints the appropriate character and color for a given chess piece, or an
     * empty space if null.
     *
     * @param piece The ChessPiece to print, or null for an empty square.
     */
    private void printPiece(ChessPiece piece) {
        if (piece == null) {
            System.out.print(EMPTY); // Print empty space for null pieces
        } else {
            // Set text color based on piece team
            String pieceColorText;
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                pieceColorText = WHITE_PIECE_TEXT;
            } else {
                pieceColorText = BLACK_PIECE_TEXT;
            }
            System.out.print(pieceColorText);

            // Print the corresponding Unicode symbol
            switch (piece.getPieceType()) {
                case KING:
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        System.out.print(WHITE_KING);
                    } else {
                        System.out.print(BLACK_KING);
                    }
                    break;
                case QUEEN:
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        System.out.print(WHITE_QUEEN);
                    } else {
                        System.out.print(BLACK_QUEEN);
                    }
                    break;
                case BISHOP:
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        System.out.print(WHITE_BISHOP);
                    } else {
                        System.out.print(BLACK_BISHOP);
                    }
                    break;
                case KNIGHT:
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        System.out.print(WHITE_KNIGHT);
                    } else {
                        System.out.print(BLACK_KNIGHT);
                    }
                    break;
                case ROOK:
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        System.out.print(WHITE_ROOK);
                    } else {
                        System.out.print(BLACK_ROOK);
                    }
                    break;
                case PAWN:
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        System.out.print(WHITE_PAWN);
                    } else {
                        System.out.print(BLACK_PAWN);
                    }
                    break;
                default:
                    System.out.print(EMPTY);
                    break; // Should not happen
            }
        }
    }

    // --- Input Parsing ---

    /**
     * Parses algebraic notation (e.g., "a1", "h8") into a ChessPosition object.
     * Handles case-insensitivity for the column letter.
     *
     * @param notation The algebraic notation string (e.g., "e4").
     * @return Corresponding ChessPosition object, or null if the format is invalid.
     */
    private ChessPosition parsePosition(String notation) {
        if (notation == null || notation.length() != 2) {
            return null;
        }
        char colChar = notation.toLowerCase().charAt(0); // Ensure lowercase for column check
        char rowChar = notation.charAt(1);

        if (colChar < 'a' || colChar > 'h' || rowChar < '1' || rowChar > '8') {
            return null; // Invalid characters
        }

        int col = colChar - 'a' + 1; // Convert 'a'->1, 'b'->2, ..., 'h'->8
        int row = Character.getNumericValue(rowChar); // Convert '1'->1, ..., '8'->8

        return new ChessPosition(row, col);
    }

    /**
     * Parses a single character representing a promotion piece type.
     * Handles case-insensitivity.
     *
     * @param pieceChar Character representing the piece ('Q', 'R', 'B', 'N').
     * @return Corresponding ChessPiece.PieceType (QUEEN, ROOK, BISHOP, KNIGHT), or
     *         null if invalid.
     */

    /** Prints an error message to the console in red. */
    private void printError(String message) {
        System.out.println(SET_TEXT_COLOR_RED + "Error: " + message + RESET_TEXT_COLOR);
    }

}