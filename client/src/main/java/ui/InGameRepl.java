package ui;

import com.google.gson.Gson;
import chess.*;
import client.ChessClient;
import websocket.messages.*;

import java.util.Scanner;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static ui.EscapeSequences.*;

/**
 * Handles user interactions within an active game session (playing or
 * observing).
 * Draws the board and processes game-specific commands.
 */
public class InGameRepl {
    private final ChessClient client;
    private final Scanner scanner;
    private final Gson gson = new Gson();
    private ChessGame currentGame;
    private Integer currentGameID;
    private boolean inGame;
    private ChessGame.TeamColor playerColor;

    private static final int BOARD_SIZE = 8;
    private static final String[] COL_LABELS = { "a", "b", "c", "d", "e", "f", "g", "h" };
    private static final String[] ROW_LABELS = { "1", "2", "3", "4", "5", "6", "7", "8" };
    private static final String LIGHT_SQUARE_BG = SET_BG_COLOR_LIGHT_GREY;
    private static final String DARK_SQUARE_BG = SET_BG_COLOR_DARK_GREEN;
    private static final String HIGHLIGHT_ALLY_SQUARE_BG = SET_BG_COLOR_YELLOW;
    private static final String HIGHLIGHT_ENEMY_SQUARE_BG = SET_BG_COLOR_RED;
    private static final String HIGHLIGHT_EMPTY_SQUARE_BG = SET_BG_COLOR_BLUE;
    private static final String WHITE_PIECE_TEXT = SET_TEXT_COLOR_WHITE;
    private static final String BLACK_PIECE_TEXT = SET_TEXT_COLOR_BLACK;
    private static final String BORDER_BG = SET_BG_COLOR_DARK_GREY;
    private static final String BORDER_TEXT = SET_TEXT_COLOR_WHITE;

    public InGameRepl(ChessClient client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
        this.inGame = false;
        this.currentGame = null;
        this.currentGameID = null;
        this.playerColor = null;
    }

    /**
     * Runs the in-game command loop.
     *
     * @return true if the user enters 'quit', false otherwise.
     */
    public boolean run() {
        if (!inGame) {
            printError("Internal state inconsistency - entered InGameRepl but not marked as 'in game'.");
            return false;
        }

        while (inGame) {
            System.out.print(getPrompt());
            String line = scanner.nextLine().trim();
            System.out.print(RESET_TEXT_COLOR);
            String[] args = line.split("\\s+");
            if (args.length == 0 || args[0].isEmpty())
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
            }
        }
        return false;
    }

    public void setPlayerColor(ChessGame.TeamColor color) {
        this.playerColor = color;
    }

    public Integer getCurrentGameID() {
        return currentGameID;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
        if (!inGame) {
            this.currentGame = null;
            this.currentGameID = null;
            this.playerColor = null;
        }
    }

    public boolean isInGame() {
        return inGame;
    }

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

    private void handleRedraw() {
        drawBoard();
    }

    private void handleLeave() throws Exception {
        client.sendLeaveCommand();
        System.out.println(SET_TEXT_COLOR_YELLOW + "Leaving game..." + RESET_TEXT_COLOR);
    }

    private void handleMove(String[] args) throws Exception {
        if (playerColor == null) {
            printError("Observers cannot make moves.");
            return;
        }
        if (args.length < 3 || args.length > 4) {
            printError("Usage: move <FROM> <TO> [PROMOTION_PIECE]");
            return;
        }
        if (currentGame == null) {
            printError("Game state not loaded yet.");
            return;
        }
        ChessPosition fromPos = parsePosition(args[1]);
        ChessPosition toPos = parsePosition(args[2]);
        ChessPiece.PieceType promotionType = null;

        if (fromPos == null || toPos == null) {
            printError("Invalid square format (e.g., a1, h8).");
            return;
        }
        if (args.length == 4) {
            promotionType = parsePromotionPiece(args[3]);
            if (promotionType == null) {
                printError("Invalid promotion piece. Use Q, R, B, or N.");
                return;
            }
        }
        ChessMove move = new ChessMove(fromPos, toPos, promotionType);
        client.sendMakeMoveCommand(move);
    }

    private void handleResign() throws Exception {
        if (playerColor == null) {
            printError("Observers cannot resign.");
            return;
        }
        System.out.print(SET_TEXT_COLOR_YELLOW + "Are you sure you want to resign? (yes/no): " + SET_TEXT_COLOR_WHITE);
        String confirmation = scanner.nextLine().trim().toLowerCase();
        if ("yes".equals(confirmation)) {
            client.sendResignCommand();
        } else {
            System.out.println("Resignation cancelled.");
        }
    }

    private void handleHighlight(String[] args) throws Exception {
        if (args.length != 2) {
            printError("Usage: highlight <SQUARE>");
            return;
        }
        if (currentGame == null) {
            printError("Game state not loaded yet. Cannot highlight moves.");
            return;
        }
        ChessPosition position = parsePosition(args[1]);
        if (position == null) {
            printError("Invalid square format (e.g., a1, h8).");
            return;
        }

        ChessPiece selectedPiece = currentGame.getBoard().getPiece(position);
        if (selectedPiece == null) {
            printError("No piece found at " + args[1]);
            return;
        }

        if (playerColor != null && selectedPiece.getTeamColor() != playerColor) {
            printError("You can only highlight your own pieces.");
            return;
        }

        Collection<ChessMove> validMoves = currentGame.validMoves(position);
        if (validMoves == null || validMoves.isEmpty()) {
            System.out.println(
                    SET_TEXT_COLOR_YELLOW + "No legal moves for the piece at " + args[1] + "." + RESET_TEXT_COLOR);
            drawBoard();
        } else {
            drawBoardWithHighlights(validMoves);
        }
    }

    public void drawBoard() {
        if (currentGame == null) {
            System.out.println(SET_TEXT_COLOR_YELLOW + "Waiting for game state..." + RESET_TEXT_COLOR);
            return;
        }
        ChessGame.TeamColor perspective = (playerColor == ChessGame.TeamColor.BLACK) ? ChessGame.TeamColor.BLACK
                : ChessGame.TeamColor.WHITE;
        ChessBoard boardToDraw = currentGame.getBoard();
        drawBoardInternal(boardToDraw, perspective, null, null);
    }

    private void drawBoardWithHighlights(Collection<ChessMove> legalMoves) {
        if (currentGame == null) {
            drawBoard();
            return;
        }
        if (legalMoves == null || legalMoves.isEmpty()) {
            drawBoard();
            return;
        }

        ChessPosition startPos = legalMoves.iterator().next().getStartPosition();
        Set<ChessPosition> endPositions = new HashSet<>();
        for (ChessMove move : legalMoves) {
            endPositions.add(move.getEndPosition());
        }

        ChessGame.TeamColor perspective = (playerColor == ChessGame.TeamColor.BLACK) ? ChessGame.TeamColor.BLACK
                : ChessGame.TeamColor.WHITE;
        ChessBoard boardToDraw = currentGame.getBoard();

        drawBoardInternal(boardToDraw, perspective, startPos, endPositions);
        System.out.println(RESET + SET_TEXT_COLOR_BLUE + "Highlighted legal moves for piece at " + startPos
                + ". Type 'redraw' to clear." + RESET_TEXT_COLOR);
    }

    private void drawBoardInternal(ChessBoard board, ChessGame.TeamColor perspective,
            ChessPosition startHighlight, Set<ChessPosition> endHighlights) {
        boolean whitePerspective = (perspective == ChessGame.TeamColor.WHITE);

        drawHeaderFooter(perspective);
        System.out.println();

        int rowStart, rowEnd, rowInc;
        if (whitePerspective) {
            rowStart = BOARD_SIZE;
            rowEnd = 1;
            rowInc = -1;
        } else {
            rowStart = 1;
            rowEnd = BOARD_SIZE;
            rowInc = 1;
        }

        for (int r = rowStart; whitePerspective ? (r >= rowEnd) : (r <= rowEnd); r += rowInc) {
            drawRowLabel(r);

            int colStart, colEnd, colInc;
            if (whitePerspective) {
                colStart = 1;
                colEnd = BOARD_SIZE;
                colInc = 1;
            } else {
                colStart = BOARD_SIZE;
                colEnd = 1;
                colInc = -1;
            }

            for (int c = colStart; whitePerspective ? (c <= colEnd) : (c >= colEnd); c += colInc) {
                ChessPosition currentPos = new ChessPosition(r, c);
                ChessPiece piece = board.getPiece(currentPos);

                boolean isLightSquare = (r + c) % 2 != 0;
                String bgColor = isLightSquare ? LIGHT_SQUARE_BG : DARK_SQUARE_BG;

                if (startHighlight != null && currentPos.equals(startHighlight)) {
                    bgColor = HIGHLIGHT_ALLY_SQUARE_BG;
                } else if (endHighlights != null && endHighlights.contains(currentPos)) {
                    ChessPiece targetPiece = board.getPiece(currentPos);
                    if (targetPiece == null) {
                        bgColor = HIGHLIGHT_EMPTY_SQUARE_BG;
                    } else {
                        bgColor = HIGHLIGHT_ENEMY_SQUARE_BG;
                    }
                }

                System.out.print(bgColor);
                printPiece(piece);
            }

            drawRowLabel(r);
            System.out.println(RESET);
        }

        drawHeaderFooter(perspective);
        System.out.println(RESET);
    }

    private void drawRowLabel(int rowIndex) {
        System.out.print(BORDER_BG + BORDER_TEXT + " " + ROW_LABELS[rowIndex - 1] + " " + RESET_BG_COLOR);
    }

    private void drawHeaderFooter(ChessGame.TeamColor perspective) {
        System.out.print(BORDER_BG + BORDER_TEXT + "   ");

        boolean whitePerspective = (perspective == ChessGame.TeamColor.WHITE);
        int colStart, colEnd, colInc;

        if (whitePerspective) {
            colStart = 0;
            colEnd = BOARD_SIZE;
            colInc = 1;
        } else {
            colStart = BOARD_SIZE - 1;
            colEnd = -1;
            colInc = -1;
        }

        for (int c = colStart; whitePerspective ? (c < colEnd) : (c > colEnd); c += colInc) {
            System.out.print(" " + COL_LABELS[c] + " ");
        }

        System.out.print("   " + RESET);
    }

    private void printPiece(ChessPiece piece) {
        if (piece == null) {
            System.out.print(EMPTY);
        } else {
            String pieceColorText = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_PIECE_TEXT
                    : BLACK_PIECE_TEXT;
            System.out.print(pieceColorText);

            switch (piece.getPieceType()) {
                case KING:
                    System.out.print((piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_KING : BLACK_KING);
                    break;
                case QUEEN:
                    System.out.print((piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_QUEEN : BLACK_QUEEN);
                    break;
                case BISHOP:
                    System.out.print((piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_BISHOP : BLACK_BISHOP);
                    break;
                case KNIGHT:
                    System.out.print((piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_KNIGHT : BLACK_KNIGHT);
                    break;
                case ROOK:
                    System.out.print((piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_ROOK : BLACK_ROOK);
                    break;
                case PAWN:
                    System.out.print((piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? WHITE_PAWN : BLACK_PAWN);
                    break;
                default:
                    System.out.print(EMPTY);
                    break;
            }
        }
    }

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

    private ChessPiece.PieceType parsePromotionPiece(String pieceStr) {
        if (pieceStr == null || pieceStr.length() != 1) {
            return null;
        }
        char pieceChar = pieceStr.toUpperCase().charAt(0);
        switch (pieceChar) {
            case 'Q':
                return ChessPiece.PieceType.QUEEN;
            case 'R':
                return ChessPiece.PieceType.ROOK;
            case 'B':
                return ChessPiece.PieceType.BISHOP;
            case 'N':
                return ChessPiece.PieceType.KNIGHT;
            default:
                return null;
        }
    }

    public void printError(String message) {
        System.out.print(ERASE_LINE);
        System.out.println("\n" + SET_TEXT_COLOR_RED + "Error: " + message + RESET_TEXT_COLOR);
        System.out.print(getPrompt());
    }

    public void handleServerMessage(String message) {
        try {
            ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);
            switch (serverMessage.getServerMessageType()) {
                case LOAD_GAME:
                    LoadGameMessage loadGameMsg = gson.fromJson(message, LoadGameMessage.class);
                    this.currentGame = loadGameMsg.getGame().game();
                    this.currentGameID = loadGameMsg.getGame().gameID();
                    System.out.print(ERASE_LINE);
                    System.out.println();
                    drawBoard();
                    System.out.print("\n" + getPrompt());
                    break;
                case NOTIFICATION:
                    NotificationMessage notificationMsg = gson.fromJson(message, NotificationMessage.class);
                    System.out.print(ERASE_LINE);
                    System.out.println("\n" + SET_TEXT_COLOR_MAGENTA + "[SERVER_MSG] " + notificationMsg.getMessage()
                            + RESET_TEXT_COLOR);
                    System.out.print(getPrompt());
                    break;
                case ERROR:
                    ErrorMessage errorMsg = gson.fromJson(message, ErrorMessage.class);
                    printError("[SERVER_ERROR] " + errorMsg.getErrorMessage());
                    break;
            }
        } catch (Exception e) {
            printError("Failed to process message from server: " + e.getMessage());
            System.err.println("Received raw message: " + message);
        }
    }

    private String getPrompt() {
        String playerStatus = (playerColor == null) ? "Observing" : "Playing as " + playerColor;
        String turnIndicator = "";
        if (currentGame != null && currentGame.getTeamTurn() != null) {
            turnIndicator = " (" + currentGame.getTeamTurn() + "'s Turn)";
        } else if (currentGame != null) {
            turnIndicator = " (Game Over)";
        }

        return RESET + SET_TEXT_COLOR_WHITE + "[" + client.getCurrentUser() + " - " + playerStatus + turnIndicator
                + "] "
                + SET_TEXT_COLOR_DARK_GREY + SET_TEXT_BLINKING + ">>> "
                + SET_TEXT_COLOR_GREEN;
    }
}