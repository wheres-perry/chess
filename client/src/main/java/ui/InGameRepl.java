package ui;

import chess.*;
import client.ChessClient;

import java.util.Collection;
import java.util.Scanner;
import java.util.Map;
import java.util.List;

import static ui.EscapeSequences.*;

public class InGameRepl {
    private final ChessClient client; // Reference to client for user info etc.
    private final Scanner scanner;
    private boolean inGame;
    private Map<String, Object> currentGameMap;
    private ChessGame.TeamColor playerColor;

    private static final int BOARD_SIZE = 8;
    private static final String[] COL_LABELS = { "a", "b", "c", "d", "e", "f", "g", "h" };
    private static final String[] ROW_LABELS = { "1", "2", "3", "4", "5", "6", "7", "8" };

    public InGameRepl(ChessClient client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
        this.inGame = false;
        this.currentGameMap = null;
        this.playerColor = null;
    }

    /**
     * Runs the in-game command loop.
     * 
     * @return true if the user wants to quit the application, false otherwise.
     */
    public boolean run() {
        if (!inGame) {
            System.out.println(SET_TEXT_COLOR_RED
                    + "Error: Internal state inconsistency - entered InGameRepl but not marked as 'in game'."
                    + RESET_TEXT_COLOR);
            return false;
        }

        System.out.println(SET_TEXT_COLOR_BLUE + "Entered game mode. Type 'help' for commands." + RESET_TEXT_COLOR);
        drawBoard();

        while (inGame) {
            String playerStatus = (playerColor == null) ? "Observing" : "Playing as " + playerColor;
            System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR
                    + SET_TEXT_COLOR_WHITE + "[" + client.getCurrentUser() + " - " + playerStatus + "] "
                    + SET_TEXT_COLOR_DARK_GREY + SET_TEXT_BLINKING + "> "
                    + SET_TEXT_COLOR_GREEN);

            String line = scanner.nextLine().trim();
            System.out.print(RESET_TEXT_COLOR);
            String[] args = line.split("\\s+");
            if (args.length == 0)
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
                        break;
                    case "move":
                        handleMove(args);
                        break;
                    case "resign":
                        handleResign();
                        break;
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
                printError("Command failed: " + e.getMessage());
                e.printStackTrace(); // For debugging REPL command errors
            }
        }
        return false; // User left game, return to PostLogin state
    }

    // --- State Management ---

    /**
     * Sets the game state using a Map representation and the player's color.
     * Called by ChessClient after successfully joining/observing and fetching
     * state.
     * 
     * @param gameMap Map containing the game state data (board, turn, etc.).
     * @param color   The player's TeamColor (or null if observing).
     */
    public void setGameStateMap(Map<String, Object> gameMap, ChessGame.TeamColor color) {
        this.currentGameMap = gameMap;
        this.playerColor = color;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
        if (!inGame) {
            this.currentGameMap = null;
            this.playerColor = null;
        }
    }

    public boolean isInGame() {
        return inGame;
    }

    private void displayHelp() {
        System.out.println(SET_TEXT_COLOR_BLUE + "In-Game commands:");
        System.out.println(SET_TEXT_COLOR_YELLOW + "  help" + SET_TEXT_COLOR_WHITE
                + "                 - Show this help message");
        System.out.println(SET_TEXT_COLOR_YELLOW + "  redraw" + SET_TEXT_COLOR_WHITE
                + "               - Redraw the chessboard");
        System.out.println(SET_TEXT_COLOR_YELLOW + "  leave" + SET_TEXT_COLOR_WHITE
                + "                - Leave the current game (return to lobby)");
        System.out.println(SET_TEXT_COLOR_YELLOW + "  move <FROM> <TO> [PROMOTION]"
                + SET_TEXT_COLOR_WHITE + " - Make a move (e.g., move e2 e4, move a7 a8 Q)");
        System.out.println(SET_TEXT_COLOR_YELLOW + "  resign" + SET_TEXT_COLOR_WHITE
                + "               - Resign the game (forfeit)");
        System.out.println(SET_TEXT_COLOR_YELLOW + "  highlight <SQUARE>" + SET_TEXT_COLOR_WHITE
                + "       - Show legal moves for piece at SQUARE (e.g., highlight e2)");
        System.out.println(SET_TEXT_COLOR_YELLOW + "  quit" + SET_TEXT_COLOR_WHITE
                + "                 - Exit the program");
        System.out.print(RESET_TEXT_COLOR);
    }

    private void handleRedraw() {
        if (currentGameMap == null) {
            printError("No game data available to redraw.");
            return;
        }
        drawBoard();
    }

    private void handleLeave() throws Exception {
        System.out.println(SET_TEXT_COLOR_YELLOW + "Leaving game." + RESET_TEXT_COLOR);
        this.setInGame(false);
    }

    private void handleMove(String[] args) throws Exception {
        if (currentGameMap == null) {
            printError("No active game.");
            return;
        }
        if (playerColor == null) {
            printError("Observers cannot make moves.");
            return;
        }
        if (args.length < 3 || args.length > 4) {
            printError("Usage: move <FROM> <TO> [PROMOTION_PIECE]");
            return;
        }

        ChessPosition fromPos = parsePosition(args[1]);
        ChessPosition toPos = parsePosition(args[2]);
        ChessPiece.PieceType promotionType = (args.length == 4) ? parsePromotionPiece(args[3]) : null;

        if (fromPos == null || toPos == null || (args.length == 4 && promotionType == null)) {
            printError("Invalid square or promotion piece format.");
            return;
        }
        printError("Make move functionality not implemented yet.");
    }

    private void handleResign() throws Exception {
        if (currentGameMap == null) {
            printError("No active game to resign from.");
            return;
        }
        if (playerColor == null) {
            printError("Observers cannot resign.");
            return;
        }

        System.out.print(SET_TEXT_COLOR_YELLOW + "Are you sure you want to resign? (yes/no): " + SET_TEXT_COLOR_WHITE);
        String confirmation = scanner.nextLine().trim().toLowerCase();
        if ("yes".equals(confirmation)) {
            printError("Resign functionality not implemented yet.");
        } else {
            System.out.println("Resignation cancelled.");
        }
    }

    private void handleHighlight(String[] args) throws Exception {
        if (currentGameMap == null) {
            printError("No active game to highlight moves in.");
            return;
        }
        if (args.length != 2) {
            printError("Usage: highlight <SQUARE>");
            return;
        }

        ChessPosition position = parsePosition(args[1]);
        if (position == null) {
            printError("Invalid square format.");
            return;
        }

        // Need to get piece from map to check if it exists
        ChessPiece piece = getPieceFromMap(currentGameMap, position); // Use helper
        if (piece == null) {
            printError("No piece at " + args[1]);
            return;
        }
    }

    // --- Board Drawing ---

    public void drawBoard() {
        if (currentGameMap == null) {
            printError("Cannot draw board, game state map is missing.");
            return;
        }
        ChessGame.TeamColor perspective = (playerColor == ChessGame.TeamColor.BLACK) ? ChessGame.TeamColor.BLACK
                : ChessGame.TeamColor.WHITE;
        drawBoardInternal(perspective, null);
    }

    /**
     * Internal method to draw the board from a given perspective.
     * 
     * @param perspective The TeamColor perspective (WHITE or BLACK at bottom).
     * @param validMoves  (Currently unused) Collection of moves to highlight.
     */
    private void drawBoardInternal(ChessGame.TeamColor perspective, Collection<ChessMove> validMoves) {
        boolean whitePerspective = (perspective == ChessGame.TeamColor.WHITE);
        int rowStart, rowEnd, rowInc;
        int colStart, colEnd, colInc;

        if (whitePerspective) {
            rowStart = BOARD_SIZE;
            rowEnd = 1;
            rowInc = -1;
            colStart = 1;
            colEnd = BOARD_SIZE;
            colInc = 1;
        } else {
            rowStart = 1;
            rowEnd = BOARD_SIZE;
            rowInc = 1;
            colStart = BOARD_SIZE;
            colEnd = 1;
            colInc = -1;
        }

        // Draw header
        drawHeaderFooter(perspective);
        System.out.println();

        // Draw rows
        for (int r = rowStart; whitePerspective ? r >= rowEnd : r <= rowEnd; r += rowInc) {
            // Draw row label (left)
            System.out.print(
                    SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + " " + ROW_LABELS[r - 1] + " " + RESET_BG_COLOR);

            // Draw squares in the row
            for (int c = colStart; whitePerspective ? c <= colEnd : c >= colEnd; c += colInc) {
                ChessPosition currentPos = new ChessPosition(r, c);
                ChessPiece piece = getPieceFromMap(currentGameMap, currentPos); // Use helper
                boolean isLightSquare = (r + c) % 2 != 0;

                // Directly determine background color instead of using method
                String bgColor;
                if (isLightSquare) {
                    bgColor = SET_BG_COLOR_LIGHT_GREY;
                } else {
                    bgColor = SET_BG_COLOR_DARK_GREEN;
                }

                System.out.print(bgColor);
                printPiece(piece);
            }

            // Draw row label (right)
            System.out.print(
                    SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + " " + ROW_LABELS[r - 1] + " " + RESET_BG_COLOR);
            System.out.println(RESET_BG_COLOR + RESET_TEXT_COLOR); // End of row
        }

        // Draw footer
        drawHeaderFooter(perspective);
        System.out.println();
    }

    private void drawHeaderFooter(ChessGame.TeamColor perspective) {
        System.out.print(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + "   "); // Padding

        boolean whitePerspective = (perspective == ChessGame.TeamColor.WHITE);
        int colStart, colEnd, colInc;

        if (whitePerspective) {
            colStart = 0;
            colEnd = BOARD_SIZE;
            colInc = 1;
        } else {
            colStart = BOARD_SIZE - 1;
            colEnd = 0;
            colInc = -1;
        }

        if (whitePerspective) {
            for (int c = colStart; c < colEnd; c += colInc) {
                System.out.print(String.format(" %s ", COL_LABELS[c])); // Column labels
            }
        } else {
            for (int c = colStart; c >= colEnd; c += colInc) {
                System.out.print(String.format(" %s ", COL_LABELS[c])); // Column labels
            }
        }

        System.out.print("   " + RESET_BG_COLOR + RESET_TEXT_COLOR); // Padding and reset
    }

    private void printPiece(ChessPiece piece) {
        if (piece == null) {
            System.out.print(EMPTY);
        } else {
            String pieceColor;
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                pieceColor = SET_TEXT_COLOR_WHITE;
            } else {
                pieceColor = SET_TEXT_COLOR_BLACK;
            }
            System.out.print(pieceColor);

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
            }
        }
    }

    /**
     * Parses algebraic notation (e.g., "a1", "h8") into a ChessPosition.
     * 
     * @param notation The algebraic notation string.
     * @return ChessPosition object or null if invalid format.
     */
    private ChessPosition parsePosition(String notation) {
        if (notation == null || notation.length() != 2)
            return null;
        char colChar = notation.toLowerCase().charAt(0);
        char rowChar = notation.charAt(1);
        if (colChar < 'a' || colChar > 'h' || rowChar < '1' || rowChar > '8')
            return null;
        int col = colChar - 'a' + 1;
        int row = Character.getNumericValue(rowChar);
        return new ChessPosition(row, col);
    }

    /**
     * Parses a single character into a promotion piece type.
     * 
     * @param pieceChar Character ('Q', 'R', 'B', 'N', case-insensitive).
     * @return Corresponding ChessPiece.PieceType or null if invalid.
     */
    private ChessPiece.PieceType parsePromotionPiece(String pieceChar) {
        if (pieceChar == null || pieceChar.length() != 1)
            return null;
        switch (pieceChar.toUpperCase()) {
            case "Q":
                return ChessPiece.PieceType.QUEEN;
            case "R":
                return ChessPiece.PieceType.ROOK;
            case "B":
                return ChessPiece.PieceType.BISHOP;
            case "N":
                return ChessPiece.PieceType.KNIGHT;
            default:
                return null;
        }
    }

    private void printError(String message) {
        System.out.println(SET_TEXT_COLOR_RED + "Error: " + message + RESET_TEXT_COLOR);
    }

    // --- NEW: Helper to extract piece data from the game state map ---
    /**
     * Extracts the ChessPiece at a given position from the game state map.
     * 
     * @param gameMap  The map representing the current game state.
     * @param position The position to check.
     * @return The ChessPiece at that position, or null if empty or error.
     */
    @SuppressWarnings("unchecked")
    private ChessPiece getPieceFromMap(Map<String, Object> gameMap, ChessPosition position) {
        if (gameMap == null || position == null)
            return null;

        try {
            Object boardObj = gameMap.get("board");
            if (!(boardObj instanceof Map))
                return null;
            Map<String, Object> boardMap = (Map<String, Object>) boardObj;

            Object squaresObj = boardMap.get("squares");
            List<List<Object>> squaresList = (List<List<Object>>) squaresObj;

            int rowIndex = position.getRow() - 1;
            int colIndex = position.getColumn() - 1;

            if (rowIndex < 0 || rowIndex >= squaresList.size())
                return null;
            List<Object> row = squaresList.get(rowIndex);
            if (colIndex < 0 || colIndex >= row.size())
                return null;

            Object pieceObj = row.get(colIndex);
            if (!(pieceObj instanceof Map))
                return null;
            Map<String, Object> pieceMap = (Map<String, Object>) pieceObj;
            String typeStr = (String) pieceMap.get("pieceType");
            String colorStr = (String) pieceMap.get("teamColor");

            if (typeStr == null || colorStr == null)
                return null; // Missing essential info

            // Convert strings to enums
            ChessPiece.PieceType type = ChessPiece.PieceType.valueOf(typeStr.toUpperCase());
            ChessGame.TeamColor color = ChessGame.TeamColor.valueOf(colorStr.toUpperCase());

            return new ChessPiece(color, type); // Construct the piece object

        } catch (Exception e) {
            System.err.println(
                    "Error parsing piece from game state map for position " + position + ": " + e.getMessage());
            return null;
        }
    }
}