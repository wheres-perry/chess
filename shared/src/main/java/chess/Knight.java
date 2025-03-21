package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Knight extends ChessMovesCalculator {

    public Knight(ChessBoard board, ChessPosition position, ChessGame.TeamColor color) {
        super(color, ChessPiece.PieceType.QUEEN, board, position);
    }

    @Override
    final public Collection<ChessMove> moves() {
        final List<int[]> knightCoords = List.of(
                new int[] { 1, 2 },
                new int[] { 2, 1 },
                new int[] { -1, 2 },
                new int[] { -2, 1 },
                new int[] { 1, -2 },
                new int[] { 2, -1 },
                new int[] { -1, -2 },
                new int[] { -2, -1 });

        Collection<ChessMove> output = new ArrayList<>();
        for (int[] coord : knightCoords) {
            ChessPosition target = new ChessPosition(position.getRow() + coord[0], position.getColumn() + coord[1]);

            if (!target.inBounds()) {
                continue;
            }
            if (isFriend(board, target)) {
                continue;
            }
            output.add(new ChessMove(position, target));
        }
        return output;
    }
}
