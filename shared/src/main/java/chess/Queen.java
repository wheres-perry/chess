package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Queen extends ChessMovesCalculator {

    public Queen(ChessBoard board, ChessPosition position, ChessGame.TeamColor color) {
        super(color, ChessPiece.PieceType.QUEEN, board, position); // Needs this for the ChessMovesCalculatorConstructor
    }

    @Override
    final public Collection<ChessMove> moves() {
        List<ChessMove.Direction> dirs = List.of(
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
            output.addAll(LinearMove(dir, board, position, 8));
        }
        return output;
    }
}
