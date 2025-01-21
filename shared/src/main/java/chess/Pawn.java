package chess;

// import java.util.ArrayList;
import java.util.Collection;

public class Pawn extends ChessMovesCalculator {

    public Pawn(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type, boolean HasMoved,
            boolean EPable, ChessBoard board, ChessPosition position) {
        super(pieceColor, type, HasMoved, EPable, board, position);
    }

    @Override
    final public Collection<ChessMove> moves() {
        // TODO Create Moves, Implement En Passant
        // Collection<ChessMove> output = new ArrayList<>();
        throw new RuntimeException("Stub");
        // return output;
    }
}
