package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import chess.ChessPiece.PieceType;

/**
 * For a class that can manage a chess game, making moves on a board
 */
public class ChessGame {

    private TeamColor TeamTurn;
    private ChessBoard Board;
    // private int turn;

    public ChessGame() {
        Board = new ChessBoard();
        Board.resetBoard();
        TeamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return TeamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        TeamTurn = team;
    }

    public void incrementTurn() {
        TeamTurn = TeamTurn.not();
        // turn += 1;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE, BLACK;

        public TeamColor not() {
            if (this == WHITE) {
                return BLACK;
            }
            return WHITE;
        }
    }

    /**
     * Gets a valid moves for a piece at the given location.
     *
     * @param startPosition the piece to get valid moves for
     * @return Collection of valid moves for requested piece, or null if no piece at
     *         startPosition
     */
    public HashSet<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = Board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        HashSet<ChessMove> validMoves = new HashSet<>();
        Collection<ChessMove> possibleMoves = piece.pieceMoves(Board, startPosition);

        for (ChessMove move : possibleMoves) {
            ChessPiece existingChessPiece = Board.getPiece(move.getEndPosition());
            // Makes move to see if its possible
            Board.addPiece(move.getEndPosition(), piece);
            Board.addPiece(startPosition, null);
            // Ensures it doesn't leave king in check
            if (!isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }
            // Undo move and reverts everything
            Board.addPiece(startPosition, piece);
            Board.addPiece(move.getEndPosition(), existingChessPiece);
        }

        return validMoves;
    }

    /**
     * Gets all pieces for a team
     *
     * @param color the team to get pieces for
     * @return Collection of all pieces for the specified team
     */
    public ArrayList<ChessPosition> getPieces(TeamColor color) {
        ArrayList<ChessPosition> pieces = new ArrayList<>();
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece p = Board.getPiece(pos);
                if (p != null && p.getTeamColor() == color) {
                    pieces.add(pos);
                }
            }
        }
        return pieces;
    }

    /**
     * Gets all valid moves for a team.
     *
     * @param team the team to get moves for (pass the opponent's color when
     *             desired)
     * @return Collection of moves for the specified team.
     */

    public HashSet<ChessMove> allValidMoves(TeamColor team) {
        HashSet<ChessMove> moves = new HashSet<>();
        ArrayList<ChessPosition> pieces = getPieces(team);
        for (ChessPosition p : pieces) {
            HashSet<ChessMove> validMoves = validMoves(p);
            if (validMoves != null) {
                moves.addAll(validMoves);
            }
        }
        return moves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    private void applyMove(ChessBoard board, ChessPosition start, ChessPosition end, ChessPiece p,
            ChessPiece.PieceType promo) {
        if (promo == null) {
            board.addPiece(end, p);
        } else {
            ChessPiece newPiece = new ChessPiece(p.getTeamColor(), promo);
            board.addPiece(end, newPiece);
        }
        board.addPiece(start, null);
        p.setHasMoved(true);
        incrementTurn();
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();

        ChessPiece.PieceType promo = move.getPromotionPiece();
        ChessPiece piece = Board.getPiece(start);

        HashSet<ChessMove> startPieceValidMoves = validMoves(start);

        if (piece == null) { // No Piece
            throw new InvalidMoveException("Tried to move a non existent Piece");
        }

        if (startPieceValidMoves == null) { // No valid moves for piece
            throw new InvalidMoveException("Tried to move a piece with no valid moves");
        }

        if (!startPieceValidMoves.contains(move)) { // Move not in valid moves
            String errorMsg = String.format("Tried an invalid move for %s at %s",
                    piece.getPieceType(), start.toString());
            throw new InvalidMoveException(errorMsg);
        }

        if (piece.getTeamColor() != TeamTurn) { //
            String errorMsg = String.format("Tried to move an enemy piece: %s at %s",
                    piece.getPieceType(), start.toString());
            throw new InvalidMoveException(errorMsg);
        }

        applyMove(Board, start, end, piece, promo);
    }

    /**
     * Applies a move onto the given board. To be used for valid moves only
     */
    private ChessPosition findKingPosition(TeamColor teamColor) {
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece piece = Board.getPiece(pos);
                if (piece != null &&
                        piece.getPieceType() == PieceType.KING &&
                        piece.getTeamColor() == teamColor) {
                    return pos;
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingToCheckPos = findKingPosition(teamColor);
        if (kingToCheckPos == null) { // No king found
            return false;
        }
        // We have to iterate instead of use allValidMoves to avoid infinite recursion
        ArrayList<ChessPosition> opponentPositions = getPieces(teamColor.not());
        for (ChessPosition pos : opponentPositions) {
            ChessPiece enemyPiece = Board.getPiece(pos);
            // Get moves without applying check validation.
            Collection<ChessMove> pseudoMoves = enemyPiece.pieceMoves(Board, pos);
            for (ChessMove move : pseudoMoves) {
                if (move.getEndPosition().equals(kingToCheckPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (isInCheck(teamColor)) { // Not in check, can't be checkmate
            Collection<ChessMove> checkedTeamsPossibleMoves = allValidMoves(teamColor);
            if (checkedTeamsPossibleMoves.isEmpty()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        Collection<ChessMove> currentTurnTeamMoves = allValidMoves(teamColor);
        if (currentTurnTeamMoves.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        Board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return Board;
    }

}
