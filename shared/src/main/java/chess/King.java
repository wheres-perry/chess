package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class King extends ChessMovesCalculator {

    public King(ChessBoard board, ChessPosition position, ChessGame.TeamColor color) {
        super(color, ChessPiece.PieceType.KING, board, position);
    }

    @Override
    final public Collection<ChessMove> moves() {
        final List<ChessMove.Direction> dirs = List.of(
                ChessMove.Direction.N,
                ChessMove.Direction.E,
                ChessMove.Direction.S,
                ChessMove.Direction.W,
                ChessMove.Direction.NE,
                ChessMove.Direction.NW,
                ChessMove.Direction.SE,
                ChessMove.Direction.SW);

        Collection<ChessMove> output = new ArrayList<>();

        for (ChessMove.Direction dir : dirs) {
            Collection<ChessMove> m = linearMove(dir, board, position, 1);
            output.addAll(m);
        }
        return output;
    }
}
