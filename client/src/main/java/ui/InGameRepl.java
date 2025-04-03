package ui;

import chess.*; // Import necessary chess classes (Game, Board, Piece, Position, Move)
import client.ChessClient;

import java.util.Collection; // Import Collection for highlighting
import java.util.Scanner;

import static ui.EscapeSequences.*; // Import escape sequences for console coloring

public class InGameRepl {
    private final ChessClient client;
    private final Scanner scanner;
    private boolean inGame; // Flag to indicate if currently in an active game context
    private ChessGame currentGame; // Store the current game state (needs to be updated)
    private ChessGame.TeamColor playerColor; // Store the player's color (WHITE, BLACK, or null for observer)

    // Constants for board drawing
    private static final int BOARD_SIZE = 8;
    private static final String[] COL_LABELS = { "a", "b", "c", "d", "e", "f", "g", "h" };
    private static final String[] ROW_LABELS = { "1", "2", "3", "4", "5", "6", "7", "8" };

    public InGameRepl(ChessClient client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
        this.inGame = false;
        this.currentGame = null; // Initialize game state
        this.playerColor = null; // Initialize player color
    }

    /**
     * Runs the in-game command loop.
     * Should be called only when `inGame` is true.
     *
     * @return true if the user wants to quit the application, false otherwise.
     */
    public boolean run() {
        // Initial board draw should be triggered by ChessClient when game state is
        // first received.
        System.out.println(SET_TEXT_COLOR_BLUE + "Entered game mode. Type 'help' for commands.");

        while (inGame) {
            String playerStatus;
            if (playerColor == null) {
                playerStatus = "Observing";
            } else {
                playerStatus = "Playing as " + playerColor;
            }
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
                        handleLeave(); // This will set inGame = false
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
                    case "quit": // Allow quit from in-game
                        System.out.println(SET_TEXT_COLOR_YELLOW + "Exiting application." + RESET_TEXT_COLOR);
                        return true; // Signal exit
                    default:
                        printError("Unknown command in game. Type 'help' for options.");
                }
            } catch (Exception e) {
                printError("Command failed: " + e.getMessage());
                e.printStackTrace(); // Print stack trace for debugging
            }
        }
        return false; // Returned to PostLogin state, don't quit application yet
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
        if (currentGame == null) {
            printError("No game data available to redraw.");
            return;
        }
        drawBoard();
    }

    private void handleLeave() throws Exception {
        this.inGame = false;
        this.currentGame = null; // Clear game state
        this.playerColor = null;
        System.out.println(SET_TEXT_COLOR_YELLOW + "Leaving game." + RESET_TEXT_COLOR);
    }

    private void handleMove(String[] args) throws Exception {
        if (currentGame == null) {
            printError("No active game.");
            return;
        }
        if (playerColor == null) {
            printError("Observers cannot make moves.");
            return;
        }
        if (args.length < 3 || args.length > 4) {
            printError("Usage: move <FROM> <TO> [PROMOTION_PIECE]");
            printError("Example: move e2 e4  OR  move a7 a8 q");
            return;
        }

        ChessPosition fromPos = parsePosition(args[1]);
        ChessPosition toPos = parsePosition(args[2]);
        ChessPiece.PieceType promotionType = null;
        if (args.length == 4) {
            promotionType = parsePromotionPiece(args[3]);
            if (promotionType == null) {
                printError("Invalid promotion piece. Use Q, R, B, or N.");
                return;
            }
        }

        if (fromPos == null || toPos == null) {
            printError("Invalid square format. Use algebraic notation (e.g., a1, h8).");
            return;
        }

        // ChessMove move = new ChessMove(fromPos, toPos, promotionType);

        printError("Move execution not implemented yet (Phase 6).");

    }

    private void handleResign() throws Exception {
        if (currentGame == null) {
            printError("No active game to resign from.");
            return;
        }
        if (playerColor == null) {
            printError("Observers cannot resign.");
            return;
        }

        System.out.print(SET_TEXT_COLOR_YELLOW + "Are you sure you want to resign? (yes/no): " + SET_TEXT_COLOR_WHITE);
        String confirmation = scanner.nextLine().trim().toLowerCase();
        System.out.print(confirmation);
    }

    private void handleHighlight(String[] args) throws Exception {
        if (currentGame == null) {
            printError("No active game to highlight moves in.");
            return;
        }
        if (args.length != 2) {
            printError("Usage: highlight <SQUARE>");
            printError("Example: highlight e2");
            return;
        }

        ChessPosition position = parsePosition(args[1]);
        if (position == null) {
            printError("Invalid square format. Use algebraic notation (e.g., a1, h8).");
            return;
        }

        ChessPiece piece = currentGame.getBoard().getPiece(position);
        if (piece == null) {
            printError("No piece at " + args[1]);
            return;
        }

        Collection<ChessMove> validMoves = currentGame.validMoves(position);

        if (validMoves == null || validMoves.isEmpty()) {
            System.out.println(SET_TEXT_COLOR_YELLOW + "No legal moves for the piece at " + args[1] + "."
                    + RESET_TEXT_COLOR);
            drawBoard();
        } else {
            System.out.println(SET_TEXT_COLOR_YELLOW + "Highlighting legal moves for " + piece.getPieceType() + " at "
                    + args[1] + "." + RESET_TEXT_COLOR);
            drawBoardWithHighlights(validMoves);
        }
    }

    public void setGameState(ChessGame game, ChessGame.TeamColor color) {
        this.currentGame = game;
        this.playerColor = color;
    }

    public void drawBoard() {
        if (currentGame == null) {
            printError("Cannot draw board, game state is missing.");
            return;
        }
        ChessGame.TeamColor perspective;
        if (playerColor == ChessGame.TeamColor.BLACK) {
            perspective = ChessGame.TeamColor.BLACK;
        } else {
            perspective = ChessGame.TeamColor.WHITE;
        }
        drawBoardInternal(perspective, null);
    }

    private void drawBoardWithHighlights(Collection<ChessMove> validMoves) {
        if (currentGame == null) {
            printError("Cannot draw board, game state is missing.");
            return;
        }
        ChessGame.TeamColor perspective;
        if (playerColor == ChessGame.TeamColor.BLACK) {
            perspective = ChessGame.TeamColor.BLACK;
        } else {
            perspective = ChessGame.TeamColor.WHITE;
        }
        drawBoardInternal(perspective, validMoves);
    }

    private void drawBoardInternal(ChessGame.TeamColor perspective, Collection<ChessMove> validMoves) {
        ChessBoard board = currentGame.getBoard();
        boolean whitePerspective = (perspective == ChessGame.TeamColor.WHITE);

        int rowStart, rowEnd, rowIncrement, colStart, colEnd, colIncrement;

        if (whitePerspective) {
            rowStart = BOARD_SIZE;
            rowEnd = 1;
            rowIncrement = -1;
            colStart = 1;
            colEnd = BOARD_SIZE;
            colIncrement = 1;
        } else {
            rowStart = 1;
            rowEnd = BOARD_SIZE;
            rowIncrement = 1;
            colStart = BOARD_SIZE;
            colEnd = 1;
            colIncrement = -1;
        }

        drawHeaderFooter(perspective);
        System.out.println();

        int r = rowStart;
        boolean continueRows = true;

        while (continueRows) {
            System.out.print(
                    SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + " " + ROW_LABELS[r - 1] + " " + RESET_BG_COLOR);

            int c = colStart;
            boolean continueCols = true;

            while (continueCols) {
                ChessPosition currentPos = new ChessPosition(r, c);
                ChessPiece piece = board.getPiece(currentPos);
                boolean isLightSquare = (r + c) % 2 != 0;
                String bgColor = getBackgroundColor(currentPos, isLightSquare, validMoves);
                System.out.print(bgColor);
                printPiece(piece);

                // Update column and check if we should continue
                c += colIncrement;
                if (whitePerspective) {
                    continueCols = (c <= colEnd);
                } else {
                    continueCols = (c >= colEnd);
                }
            }

            System.out.print(
                    SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + " " + ROW_LABELS[r - 1] + " " + RESET_BG_COLOR);
            System.out.println(RESET_BG_COLOR + RESET_TEXT_COLOR);

            // Update row and check if we should continue
            r += rowIncrement;
            if (whitePerspective) {
                continueRows = (r >= rowEnd);
            } else {
                continueRows = (r <= rowEnd);
            }
        }

        drawHeaderFooter(perspective);
        System.out.println();
    }

    private void drawHeaderFooter(ChessGame.TeamColor perspective) {
        System.out.print(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE);
        System.out.print("   ");

        boolean whitePerspective = (perspective == ChessGame.TeamColor.WHITE);
        int colStart, colEnd, colIncrement;

        if (whitePerspective) {
            colStart = 0;
            colEnd = BOARD_SIZE;
            colIncrement = 1;
        } else {
            colStart = BOARD_SIZE - 1;
            colEnd = 0;
            colIncrement = -1;
        }

        for (int c = colStart; (whitePerspective ? c < colEnd : c >= colEnd); c += colIncrement) {
            System.out.print(String.format(" %s ", COL_LABELS[c]));
        }

        System.out.print("   ");
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private String getBackgroundColor(ChessPosition position, boolean isLightSquare, Collection<ChessMove> validMoves) {
        boolean isHighlighted = false;
        boolean isOriginHighlight = false;

        if (validMoves != null && !validMoves.isEmpty()) {
            if (position.equals(validMoves.iterator().next().getStartPosition())) {
                isOriginHighlight = true;
            }
            for (ChessMove move : validMoves) {
                if (position.equals(move.getEndPosition())) {
                    isHighlighted = true;
                    break;
                }
            }
        }

        if (isOriginHighlight) {
            return SET_BG_COLOR_YELLOW;
        } else if (isHighlighted) {
            if (isLightSquare) {
                return SET_BG_COLOR_GREEN;
            } else {
                return SET_BG_COLOR_DARK_GREEN;
            }
        } else {
            if (isLightSquare) {
                return SET_BG_COLOR_LIGHT_GREY;
            } else {
                return SET_BG_COLOR_DARK_GREY;
            }
        }
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

    // --- Helper Methods ---

    private ChessPosition parsePosition(String notation) {
        if (notation == null || notation.length() != 2) {
            return null;
        }
        char colChar = notation.toLowerCase().charAt(0);
        char rowChar = notation.charAt(1);
        if (colChar < 'a' || colChar > 'h' || rowChar < '1' || rowChar > '8') {
            return null;
        }
        int col = colChar - 'a' + 1;
        int row = Character.getNumericValue(rowChar);
        return new ChessPosition(row, col);
    }

    private ChessPiece.PieceType parsePromotionPiece(String pieceChar) {
        if (pieceChar == null || pieceChar.length() != 1) {
            return null;
        }
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

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }
}