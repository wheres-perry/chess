package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Bishop extends ChessMovesCalculator {

    public Bishop(ChessBoard board, ChessPosition position, ChessGame.TeamColor color) {
        super(color, ChessPiece.PieceType.BISHOP, board, position);
    }

    @Override
    final public Collection<ChessMove> moves() {
        final List<ChessMove.Direction> dirs = List.of(
                ChessMove.Direction.NE,
                ChessMove.Direction.NW,
                ChessMove.Direction.SE,
                ChessMove.Direction.SW);

        Collection<ChessMove> output = new ArrayList<>();

        for (ChessMove.Direction dir : dirs) {
            Collection<ChessMove> m = linearMove(dir, board, position, 8);
            output.addAll(m);
        }
        return output;
    }
}
