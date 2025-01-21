package chess;

import java.util.Collection;

public class Knight extends ChessMovesCalculator {

    public Knight(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type, boolean HasMoved,
            boolean EPable, ChessBoard board, ChessPosition position) {
        super(pieceColor, type, HasMoved, EPable, board, position);
    }

    @Override
    final public Collection<ChessMove> moves() {
        // TODO Implement Knight Moves
        throw new RuntimeException("Stub");
    }
}
