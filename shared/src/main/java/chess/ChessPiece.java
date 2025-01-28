package chess;

import java.util.ArrayList;
import java.util.Collection;

import javax.management.RuntimeErrorException;

import chess.ChessGame.TeamColor;


/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    protected PieceType type;
    protected ChessGame.TeamColor color;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.type = type;
        this.color = pieceColor;
    }

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
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return (color);
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return (type);
    }

/**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> output = new ArrayList<>();
        switch (type) {
            case PieceType.PAWN:
                PCalc pcalc = new PCalc(this, myPosition, board);
                output.addAll(pcalc.calculate());
                break;
            case PieceType.KNIGHT:
                NCalc ncalc = new NCalc(this, myPosition, board);
                output.addAll(ncalc.calculate());
                break;
            case PieceType.BISHOP:
                BCalc bcalc = new BCalc(this, myPosition, board);
                output.addAll(bcalc.calculate());
                break;
            case PieceType.ROOK:
                RCalc rcalc = new RCalc(this, myPosition, board);
                output.addAll(rcalc.calculate());
                break;
            case PieceType.QUEEN:
                QCalc qcalc = new QCalc(this, myPosition, board);
                output.addAll(qcalc.calculate());
                break;
            case PieceType.KING:
                KCalc kcalc = new KCalc(this, myPosition, board);
                output.addAll(kcalc.calculate());
                break;
            default:
                throw new RuntimeErrorException(null);
        }
        return output;
    }

    @Override
    public String toString() {
        if (color == TeamColor.WHITE) {
            switch (type) {
                case PieceType.PAWN:
                    return ("P");
                case PieceType.KNIGHT:
                    return ("N");
                case PieceType.BISHOP:
                    return ("B");
                case PieceType.ROOK:
                    return ("R");
                case PieceType.QUEEN:
                    return ("Q");
                case PieceType.KING:
                    return ("K");
                case null:
                    return ("othercase");
                default:
                    return (".");
            }
        } else {
            switch (type) {
                case PieceType.PAWN:
                    return ("p");
                case PieceType.KNIGHT:
                    return ("n");
                case PieceType.BISHOP:
                    return ("b");
                case PieceType.ROOK:
                    return ("r");
                case PieceType.QUEEN:
                    return ("q");
                case PieceType.KING:
                    return ("k");
                case null:
                    return ("othercase");
                default:
                    return (".");
            }
        }
    }


    @Override
    public boolean equals(Object obj) {
        ChessPiece other = (ChessPiece) obj;
        if (type != other.type)
            return false;
        if (color != other.color)
            return false;
        return true;
    }
}
