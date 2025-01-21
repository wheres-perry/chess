package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Queen extends ChessMovesCalculator {

    public Queen(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type, boolean HasMoved,
            boolean EPable, ChessBoard board, ChessPosition position) {
        super(pieceColor, type, HasMoved, EPable, board, position);
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
            Collection<ChessMove> m = LinearMove(dir, board, position, 8);
            output.addAll(m);
        }
        return output;
    }
}
