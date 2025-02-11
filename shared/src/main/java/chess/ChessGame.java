package chess;

import java.util.ArrayList;
import java.util.Collection;

import chess.ChessPiece.PieceType;

/**
 * For a class that can manage a chess game, making moves on a board
 */
public class ChessGame implements Cloneable {

    private TeamColor TeamTurn;
    private ChessBoard Board;
    private int turn;

    private TeamColor TeamTurn;
    private ChessBoard Board;
    private int turn;

    public ChessGame() {
        Board = new ChessBoard();
        TeamTurn = TeamColor.WHITE;
<<<<<<< HEAD
    }

    public ChessGame(TeamColor turn) {
        Board = new ChessBoard();
        TeamTurn = turn;
=======
>>>>>>> Broken-Piece-Moves
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

<<<<<<< HEAD
    public void incrementTurn() {
=======
    public void incrementTurn(){
>>>>>>> Broken-Piece-Moves
        TeamTurn = TeamTurn.not();
        turn += 1;
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
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     *         startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = Board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        
        Collection<ChessMove> validMoves = new ArrayList<>();
        Collection<ChessMove> possibleMoves = piece.pieceMoves(Board, startPosition);
    
        // Test each possible move
        for (ChessMove move : possibleMoves) {
            ChessGame hypotheticalGame = this.clone();
            try {
                hypotheticalGame.makeMove(move);
                if (!hypotheticalGame.isInCheck(piece.getTeamColor())) {
                    validMoves.add(move);
                }
            } catch (InvalidMoveException e) {

                continue;
            }
        }
        
        return validMoves;
    }

    public ArrayList<ChessPosition> getPieces(TeamColor color) {
        ArrayList<ChessPosition> pieces = new ArrayList<>();
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece p = Board.getPiece(pos);
                if (p != null) {
                    if (p.getTeamColor() == color) {
                        pieces.add(pos);
                    }
                }
            }
        }
        return pieces;
    }

    public Collection<ChessMove> allValidTeamMoves(TeamColor color) {
        ArrayList<ChessMove> teamMoves = new ArrayList<>();
        ArrayList<ChessPosition> teamPieces = getPieces(color);
        for (ChessPosition p : teamPieces) {
            Collection<ChessMove> m = validMoves(p);
            if (m != null) {
                teamMoves.addAll(teamMoves);
            }
        }
        return teamMoves;
    }

    public Collection<ChessMove> allValidOpponentMoves(TeamColor color) {
        ArrayList<ChessMove> teamMoves = new ArrayList<>(); 
        ArrayList<ChessPosition> teamPieces = getPieces(color.not());
        
        for (ChessPosition p : teamPieces) {
            ChessPiece piece = Board.getPiece(p);
            Collection<ChessMove> moves = piece.pieceMoves(Board, p);
            if (moves != null) {
                teamMoves.addAll(moves);
            }
        }
        return teamMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece piece = Board.getPiece(start);
        if (piece == null) {
            throw new InvalidMoveException("Tried to move a non existent Piece");
        }
        Collection<ChessMove> validMoves = piece.pieceMoves(Board, start);
        if (!validMoves.contains(move)) {
            String errorMsg = String.format("Invalid move for %s at %s",
                    piece.getPieceType(),
                    start.toString()); // Use ChessPosition's built-in formatting
            throw new InvalidMoveException(errorMsg);
        }

        Board.addPiece(end, piece);
        Board.addPiece(start, null);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */

     public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPos = null;
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece piece = Board.getPiece(pos);
                if (piece != null && 
                    piece.getPieceType() == PieceType.KING && 
                    piece.getTeamColor() == teamColor) {
                    kingPos = pos;
                    break;
                }
            }
            if (kingPos != null) break;
        }
        
        Collection<ChessMove> opponentMoves = allValidOpponentMoves(teamColor);
        
        for (ChessMove move : opponentMoves) {
            if (move.getEndPosition().equals(kingPos)) {
                return true;
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
        // TODO: Create board states of all possible moves and determine if any of them
        // remove check. If not checkmate.
        return false;
        // throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
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

    @Override
    public ChessGame clone() {
        try {
            ChessGame cloned = (ChessGame) super.clone();

            // Deep clone critical components
            cloned.Board = this.Board.clone(); // Uses ChessBoard's clone()
            cloned.TeamTurn = this.TeamTurn; // Enum is immutable
            cloned.turn = this.turn; // Primitive value

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("ChessGame cloning failed", e);
        }
    }

}
