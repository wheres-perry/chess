package ui;

import chess.*;
import client.ChessClient;
import serverConnection.ServerFacade;
import serverConnection.WebSocketClient;
import websocket.messages.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import static ui.EscapeSequences.*;

/**
 * Handles user interactions within an active game session (playing or
 * observing).
 * Draws the board, processes game-specific commands via WebSocket,
 * and displays incoming server messages.
 */
public class InGameRepl implements WebSocketClient.WebSocketListener {
    private final ChessClient client;
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private boolean inGame;
    private ChessGame.TeamColor playerColor;
    private ChessGame currentGame;
    private Integer currentGameID;
    private Set<ChessPosition> highlightPositions = null;

    private static final int BOARD_SIZE = 8;
    private static final String[] COL_LABELS = { "a", "b", "c", "d", "e", "f", "g", "h" };
    private static final String[] ROW_LABELS = { "1", "2", "3", "4", "5", "6", "7", "8" };
    private static final String LIGHT_SQUARE_BG = SET_BG_COLOR_LIGHT_GREY;
    private static final String DARK_SQUARE_BG = SET_BG_COLOR_DARK_GREEN;
    private static final String HIGHLIGHT_LIGHT_BG = SET_BG_COLOR_YELLOW;
    private static final String HIGHLIGHT_DARK_BG = SET_BG_COLOR_BLUE;
    private static final String WHITE_PIECE_TEXT = SET_TEXT_COLOR_WHITE;
    private static final String BLACK_PIECE_TEXT = SET_TEXT_COLOR_BLACK;
    private static final String BORDER_BG = SET_BG_COLOR_DARK_GREY;
    private static final String BORDER_TEXT = SET_TEXT_COLOR_WHITE;

    public InGameRepl(ChessClient client, ServerFacade serverFacade) {
        this.client = client;
        this.serverFacade = serverFacade;
        this.scanner = new Scanner(System.in);
        this.inGame = false;
        this.playerColor = null;
        this.currentGame = new ChessGame();
        this.currentGame.getBoard().resetBoard();
    }

    /**
     * Runs the in-game command loop.
     * 
     * @return true if user quits application, false otherwise.
     */
    public boolean run() {
        if (!inGame) {
            printError("Internal state inconsistency - entered InGameRepl but not marked as 'in game'.");
            return false;
        }

        while (inGame) {
            String playerStatus = (playerColor == null) ? "Observing" : "Playing as " + playerColor;
            System.out.print(RESET
                    + SET_TEXT_COLOR_WHITE + "[" + client.getCurrentUser() + " - " + playerStatus + "] "
                    + SET_TEXT_COLOR_DARK_GREY + SET_TEXT_BLINKING + ">>> "
                    + SET_TEXT_COLOR_GREEN);

            String line = scanner.nextLine().trim();
            System.out.print(RESET_TEXT_COLOR);
            String[] args = line.split("\\s+");
            if (args.length == 0 || args[0].isEmpty())
                continue;
            String command = args[0].toLowerCase();
            highlightPositions = null;

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
            } catch (ServerFacade.ServerFacadeException se) {
                printError("Server communication error: " + se.getMessage());
            } catch (InvalidMoveException ime) {
                printError("Invalid move: " + ime.getMessage());
            } catch (Exception e) {
                printError("An unexpected error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void onMessageReceived(ServerMessage message) {
        System.out.println();
        switch (message.getServerMessageType()) {
            case LOAD_GAME:
                LoadGameMessage loadMsg = (LoadGameMessage) message;
                this.currentGame = loadMsg.getGame().game();
                if (this.currentGameID == null)
                    this.currentGameID = loadMsg.getGame().gameID();
                System.out.println(SET_TEXT_COLOR_BLUE + "Game state loaded/updated." + RESET_TEXT_COLOR);
                drawBoard();
                break;
            case NOTIFICATION:
                NotificationMessage notifyMsg = (NotificationMessage) message;
                System.out.println(
                        SET_TEXT_COLOR_MAGENTA + "[SERVER NOTIFICATION] " + notifyMsg.getMessage() + RESET_TEXT_COLOR);
                break;
            case ERROR:
                ErrorMessage errorMsg = (ErrorMessage) message;
                printError("[SERVER ERROR] " + errorMsg.getErrorMessage());
                break;
        }
        String playerStatus = (playerColor == null) ? "Observing" : "Playing as " + playerColor;
        System.out.print(RESET + SET_TEXT_COLOR_WHITE + "[" + client.getCurrentUser() + " - " + playerStatus + "] "
                + SET_TEXT_COLOR_DARK_GREY + SET_TEXT_BLINKING + ">>> " + SET_TEXT_COLOR_GREEN);

    }

    @Override
    public void onError(String errorMessage) {
        System.out.println();
        printError("[WebSocket Error] " + errorMessage);
        String playerStatus = (playerColor == null) ? "Observing" : "Playing as " + playerColor;
        System.out.print(RESET + SET_TEXT_COLOR_WHITE + "[" + client.getCurrentUser() + " - " + playerStatus + "] "
                + SET_TEXT_COLOR_DARK_GREY + SET_TEXT_BLINKING + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    /**
     * Sets the player's color.
     * 
     * @param color The player's TeamColor, or null for observers.
     */
    public void setPlayerColor(ChessGame.TeamColor color) {
        this.playerColor = color;
    }

    /**
     * Sets the in-game status.
     * 
     * @param inGame true if the user is now in a game, false otherwise.
     */
    public void setInGame(boolean inGame) {
        this.inGame = inGame;
        if (!inGame) {
            this.playerColor = null;
            this.currentGame = null;
            this.currentGameID = null;
            this.highlightPositions = null;
        }
    }

    /**
     * @return true if the client is currently in an active game session.
     */
    public boolean isInGame() {
        return inGame;
    }

    /**
     * Sets the current game ID being played or observed.
     * 
     * @param gameID The ID of the current game.
     */
    public void setCurrentGameID(Integer gameID) {
        this.currentGameID = gameID;
    }

    private void displayHelp() {
        System.out.println(SET_TEXT_COLOR_BLUE + "In-Game commands:");
        System.out.println(
                SET_TEXT_COLOR_YELLOW + "  help" + SET_TEXT_COLOR_WHITE + "                - Show this help message");
        System.out.println(
                SET_TEXT_COLOR_YELLOW + "  redraw" + SET_TEXT_COLOR_WHITE + "              - Redraw the chessboard");
        System.out.println(SET_TEXT_COLOR_YELLOW + "  leave" + SET_TEXT_COLOR_WHITE
                + "               - Leave the current game (return to lobby)");
        System.out.println(SET_TEXT_COLOR_YELLOW + "  move <FROM> <TO> [PROMOTION]" + SET_TEXT_COLOR_WHITE
                + " - Make a move (e.g., move e2 e4, move a7 a8 Q)");
        System.out.println(SET_TEXT_COLOR_YELLOW + "  resign" + SET_TEXT_COLOR_WHITE
                + "              - Resign the game (forfeit)");
        System.out.println(SET_TEXT_COLOR_YELLOW + "  highlight <SQUARE>" + SET_TEXT_COLOR_WHITE
                + "    - Show legal moves for piece at SQUARE (e.g., highlight e2)");
        System.out.println(
                SET_TEXT_COLOR_YELLOW + "  quit" + SET_TEXT_COLOR_WHITE + "                - Exit the program");
        System.out.print(RESET_TEXT_COLOR);
    }

    private void handleRedraw() {
        drawBoard();
    }

    private void handleLeave() throws Exception {
        if (currentGameID == null || client.getAuthToken() == null) {
            printError("Cannot leave - game ID or auth token missing.");
            return;
        }
        System.out.println(SET_TEXT_COLOR_YELLOW + "Sending leave request..." + RESET_TEXT_COLOR);
        serverFacade.sendLeaveCommand(client.getAuthToken(), currentGameID);
        setInGame(false);
    }

    private void handleMove(String[] args) throws Exception {
        if (playerColor == null) {
            printError("Observers cannot make moves.");
            return;
        }
        if (currentGame == null || currentGameID == null) {
            printError("Game state not loaded. Cannot make move.");
            return;
        }
        if (currentGame.getTeamTurn() != playerColor) {
            printError("It's not your turn.");
            return;
        }

        if (args.length < 3 || args.length > 4) {
            printError("Usage: move <FROM> <TO> [PROMOTION_PIECE(Q/R/B/N)]");
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

        ChessPiece movingPiece = currentGame.getBoard().getPiece(fromPos);
        if (movingPiece == null || movingPiece.getTeamColor() != playerColor) {
            printError("No valid piece of yours at " + args[1] + ".");
            return;
        }

        ChessMove move = new ChessMove(fromPos, toPos, promotionType);

        serverFacade.sendMakeMoveCommand(client.getAuthToken(), currentGameID, move);
        System.out.println(SET_TEXT_COLOR_YELLOW + "Move command sent." + RESET_TEXT_COLOR);
    }

    private void handleResign() throws Exception {
        if (playerColor == null) {
            printError("Observers cannot resign.");
            return;
        }
        if (currentGameID == null || client.getAuthToken() == null) {
            printError("Cannot resign - game ID or auth token missing.");
            return;
        }

        System.out.print(SET_TEXT_COLOR_YELLOW + "Are you sure you want to resign? This cannot be undone. (yes/no): "
                + SET_TEXT_COLOR_WHITE);
        String confirmation = scanner.nextLine().trim().toLowerCase();
        System.out.print(RESET_TEXT_COLOR);

        if ("yes".equals(confirmation)) {
            serverFacade.sendResignCommand(client.getAuthToken(), currentGameID);
            System.out.println(SET_TEXT_COLOR_YELLOW + "Resign command sent." + RESET_TEXT_COLOR);
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
            printError("Game state not loaded. Cannot highlight moves.");
            return;
        }

        ChessPosition position = parsePosition(args[1]);
        if (position == null) {
            printError("Invalid square format (e.g., a1, h8).");
            return;
        }

        ChessPiece piece = currentGame.getBoard().getPiece(position);
        if (piece == null) {
            printError("No piece at " + args[1] + " to highlight moves for.");
            highlightPositions = null;
            drawBoard();
            return;
        }

        Collection<ChessMove> validMoves = currentGame.validMoves(position);

        highlightPositions = new HashSet<>();
        if (validMoves != null) {
            highlightPositions.add(position);
            for (ChessMove move : validMoves) {
                highlightPositions.add(move.getEndPosition());
            }
        } else {
            printError("Could not calculate valid moves for the piece at " + args[1] + ".");
            highlightPositions = null;
        }

        drawBoard();
    }

    /**
     * Draws the current board state based on perspective and highlights.
     */
    public void drawBoard() {
        if (currentGame == null) {
            System.out.println(SET_TEXT_COLOR_YELLOW + "No game data available to draw." + RESET_TEXT_COLOR);
            return;
        }
        ChessGame.TeamColor perspective = (playerColor == ChessGame.TeamColor.BLACK) ? ChessGame.TeamColor.BLACK
                : ChessGame.TeamColor.WHITE;
        drawBoardInternal(currentGame.getBoard(), perspective, highlightPositions);
    }

    /**
     * Internal drawing method with highlighting support.
     * 
     * @param board            The board state to draw.
     * @param perspective      The viewing player's color.
     * @param highlightSquares Set of squares to highlight.
     */
    private void drawBoardInternal(ChessBoard board, ChessGame.TeamColor perspective,
            Set<ChessPosition> highlightSquares) {
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
                boolean highlight = highlightSquares != null && highlightSquares.contains(currentPos);

                String bgColor;
                if (highlight) {
                    bgColor = isLightSquare ? HIGHLIGHT_LIGHT_BG : HIGHLIGHT_DARK_BG;
                } else {
                    bgColor = isLightSquare ? LIGHT_SQUARE_BG : DARK_SQUARE_BG;
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
        String pieceColorText = (piece != null && piece.getTeamColor() == ChessGame.TeamColor.BLACK) ? BLACK_PIECE_TEXT
                : WHITE_PIECE_TEXT;
        System.out.print(pieceColorText);

        if (piece == null) {
            System.out.print(EMPTY);
            return;
        }

        switch (piece.getPieceType()) {
            case KING:
                System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING);
                break;
            case QUEEN:
                System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN);
                break;
            case BISHOP:
                System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP);
                break;
            case KNIGHT:
                System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT);
                break;
            case ROOK:
                System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK);
                break;
            case PAWN:
                System.out.print(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN);
                break;
            default:
                System.out.print(EMPTY);
                break;
        }
    }

    /**
     * Parses algebraic notation into a ChessPosition.
     * 
     * @param notation Algebraic notation (e.g., "e4").
     * @return ChessPosition or null if invalid.
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
     * Parses promotion piece character.
     * 
     * @param pieceChar Character (Q, R, B, N).
     * @return PieceType or null if invalid.
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

    /** Prints an error message to the console in red. */
    private void printError(String message) {
        System.out.println(SET_TEXT_COLOR_RED + "Error: " + message + RESET_TEXT_COLOR);
    }

}