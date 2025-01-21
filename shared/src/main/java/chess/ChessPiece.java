package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a single chess piece
 */
public class ChessPiece {
    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    };

    private final ChessGame.TeamColor color;
    private final ChessPiece.PieceType type;
    private boolean HasMoved;
    private boolean EPable;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type, boolean HasMoved,
            boolean EPable) { // Overloaded constructor
        this.color = pieceColor;
        this.type = type;
        this.HasMoved = HasMoved;
        this.EPable = EPable;
    }

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) { // Overloaded constructor
        this.color = pieceColor;
        this.type = type;
        this.HasMoved = false;
        this.EPable = false;
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */





    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((color == null) ? 0 : color.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ChessPiece other = (ChessPiece) obj;
        if (color != other.color)
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    @Override
    public String toString() {
        if (color == ChessGame.TeamColor.BLACK) {
            switch (type) {
                case PAWN:
                    return "♟";
                case ROOK:
                    return "♜";
                case KNIGHT:
                    return "♞";
                case BISHOP:
                    return "♝";
                case KING:
                    return "♚";
                case QUEEN:

                return "♛";
                default:
                    throw new RuntimeException("Piece does not have a valid type");
            }
        } else {

            switch (type) {
                case PAWN:

                return "♙";
                case ROOK:
                    return "♖";
                case KNIGHT:
                    return "♘";
                case BISHOP:
                    return "♗";
                case KING:
                    return "♔";
                case QUEEN:
                    return "♕";
                default:
                    throw new RuntimeException("Piece does not have a valid type");
            }
        }

    }
}
