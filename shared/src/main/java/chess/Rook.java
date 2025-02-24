package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Rook extends ChessMovesCalculator {

    public Rook(ChessBoard board, ChessPosition position, ChessGame.TeamColor color) {
        super(color, ChessPiece.PieceType.ROOK, board, position);
    }

    @Override
    final public Collection<ChessMove> moves() {
        // TODO Implement Castling
        final List<ChessMove.Direction> dirs = List.of(
                ChessMove.Direction.N,
                ChessMove.Direction.E,
                ChessMove.Direction.S,
                ChessMove.Direction.W);

        Collection<ChessMove> output = new ArrayList<>();

        for (ChessMove.Direction dir : dirs) {
            Collection<ChessMove> m = LinearMove(dir, board, position, 8);
            output.addAll(m);
        }
        return output;
    }
}
