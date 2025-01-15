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

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.type = type;
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


    public boolean isOccupied(ChessBoard board, ChessPosition myPosition){ //Dummy function for now
        return false;
    }

    public ChessMove up(ChessBoard board, ChessPosition myPosition, int distance){
        ChessPosition targetSquare = new ChessPosition(myPosition.getRow() + distance, myPosition.getColumn());
        if (isOccupied(board, targetSquare)){
            return null;
        }
        else{
            ChessMove move = new ChessMove(myPosition, targetSquare, null);
            return move;
        }
        
    }

    public ChessMove down(ChessBoard board, ChessPosition myPosition, int distance){
        ChessPosition targetSquare = new ChessPosition(myPosition.getRow() - distance, myPosition.getColumn());
        if (isOccupied(board, targetSquare)){
            return null;
        }
        else{
            ChessMove move = new ChessMove(myPosition, targetSquare, null);
            return move;
        }
        
    }

    public ChessMove left(ChessBoard board, ChessPosition myPosition, int distance){
        ChessPosition targetSquare = new ChessPosition(myPosition.getRow(), myPosition.getColumn() - distance);
        if (isOccupied(board, targetSquare)){
            return null;
        }
        else{
            ChessMove move = new ChessMove(myPosition, targetSquare, null);
            return move;
        }
        
    }

    public ChessMove right(ChessBoard board, ChessPosition myPosition, int distance){
        ChessPosition targetSquare = new ChessPosition(myPosition.getRow(), myPosition.getColumn() + distance);
        if (isOccupied(board, targetSquare)){
            return null;
        }
        else{
            ChessMove move = new ChessMove(myPosition, targetSquare, null);
            return move;
        }
        
    }
    
    public ChessMove UL(ChessBoard board, ChessPosition myPosition, int distance){
        ChessPosition targetSquare = new ChessPosition(myPosition.getRow()-distance, myPosition.getColumn() - distance);
        if (isOccupied(board, targetSquare)){
            return null;
        }
        else{
            ChessMove move = new ChessMove(myPosition, targetSquare, null);
            return move;
        }
        
    }

    public ChessMove UR(ChessBoard board, ChessPosition myPosition, int distance){
        ChessPosition targetSquare = new ChessPosition(myPosition.getRow() - distance, myPosition.getColumn() + distance);
        if (isOccupied(board, targetSquare)){
            return null;
        }
        else{
            ChessMove move = new ChessMove(myPosition, targetSquare, null);
            return move;
        }
        
    }

    public ChessMove DL(ChessBoard board, ChessPosition myPosition, int distance){
        ChessPosition targetSquare = new ChessPosition(myPosition.getRow() + distance, myPosition.getColumn() - distance);
        if (isOccupied(board, targetSquare)){
            return null;
        }
        else{
            ChessMove move = new ChessMove(myPosition, targetSquare, null);
            return move;
        }
        
    }

    public ChessMove DR(ChessBoard board, ChessPosition myPosition, int distance){
        ChessPosition targetSquare = new ChessPosition(myPosition.getRow() + distance, myPosition.getColumn() + distance);
        if (isOccupied(board, targetSquare)){
            return null;
        }
        else{
            ChessMove move = new ChessMove(myPosition, targetSquare, null);
            return move;
        }
        
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessMove result;
        switch (type){
            case KING: // 1 Square in other directions
                result = up(board, myPosition, 1);
                if (result != null){moves.add(result);}
                result = down(board, myPosition, 1);
                if (result != null){moves.add(result);}
                result = left(board, myPosition, 1);
                if (result != null){moves.add(result);}
                result = right(board, myPosition, 1);
                if (result != null){moves.add(result);}
                result = UL(board, myPosition, 1);
                if (result != null){moves.add(result);}
                result = UR(board, myPosition, 1);
                if (result != null){moves.add(result);}
                result = DL(board, myPosition, 1);
                if (result != null){moves.add(result);}
                result = DR(board, myPosition, 1);
                if (result != null){moves.add(result);}
                break;
            case QUEEN:
                throw new RuntimeException("Not implemented yet");
                //break;
            case BISHOP:
                int increment = 1;
                while((myPosition.getColumn() - increment >= 1) && (myPosition.getRow() - increment >= 1)){
                    result = UL(board, myPosition, increment);
                    if (result != null){moves.add(result);}
                    increment++;
                }
                increment = 1;

                while((myPosition.getColumn() + increment <= 8) && (myPosition.getRow() - increment >= 1)){ //Ensures only inbound moves
                    result = UR(board, myPosition, increment);
                    if (result != null){moves.add(result);}
                    increment++;
                }
                increment = 1;

                while((myPosition.getColumn() - increment >= 1) && (myPosition.getRow() + increment <= 8)){ //Ensures only inbound moves
                    result = DL(board, myPosition, increment);
                    if (result != null){moves.add(result);}
                    increment++;
                }
                increment = 1;

                while((myPosition.getColumn() + increment <= 8) && (myPosition.getRow() + increment <= 8)){ //Ensures only inbound moves
                    result = DR(board, myPosition, increment);
                    if (result != null){moves.add(result);}
                    increment++;
                }
                break;
            case KNIGHT:
                throw new RuntimeException("Not implemented yet");
                //break;
            case ROOK:
                throw new RuntimeException("Not implemented yet");
                //break;
            case PAWN:
                throw new RuntimeException("Not implemented yet");
                //break;
            default:
                throw new RuntimeException("Piece does not have a valid type");
        }
        return moves;
    }



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
        if (color == ChessGame.TeamColor.BLACK){
            switch (type){
                case PAWN:
                    return "p";
                case ROOK:
                    return "r";
                case KNIGHT:
                    return "n";
                case BISHOP:
                    return "b";
                case KING:
                    return "k";
                case QUEEN:
                    return "q";
                default:
                    throw new RuntimeException("Piece does not have a valid type");
            }
        } 
        else{
            switch (type){
                case PAWN:
                    return "P";
                case ROOK:
                    return "R";
                case KNIGHT:
                    return "N";
                case BISHOP:
                    return "B";
                case KING:
                    return "K";
                case QUEEN:
                    return "Q";
                default:
                    throw new RuntimeException("Piece does not have a valid type");
            }
        }

    }
}
