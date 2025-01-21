package chess;

import java.util.ArrayList;
import java.util.Collection;

public abstract class ChessMovesCalculator extends ChessPiece {

    protected ChessBoard board;
    protected ChessPosition position;

    public ChessMovesCalculator(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type, boolean HasMoved,
            boolean EPable, ChessBoard board, ChessPosition position) {
        super(pieceColor, type, HasMoved, EPable);
        this.board = board;
        this.position = position;
    }

    public abstract Collection<ChessMove> moves();

    public boolean isFreeOrEnemy(ChessBoard board, ChessPosition targetSquare) { // If it not a team member, its free
        if (board.getPiece(targetSquare).getTeamColor() != color) {
            return true;
        }
        return false;
    }

    public boolean InBounds(int col, int row) {
        if (col < 1 | col > 8) {
            return false;
        }
        if (row < 1 | row > 8) {
            return false;
        }
        return true;
    }

    public boolean DestinationChecks(ChessBoard board, ChessPosition targetSquare) {
        if (isFreeOrEnemy(board, targetSquare)) {
            return false;
        }
        if (!targetSquare.InBounds()) {
            return false;
        }
        return true;
    }

    final public Collection<ChessMove> LinearMove(ChessMove.Direction dir, ChessBoard board, ChessPosition myPosition,
            int distance) {
        Collection<ChessMove> moves = new ArrayList<>();
        final int row_pos = myPosition.getRow();
        final int col_pos = myPosition.getColumn();

        int rowOffset = 0;
        int colOffset = 0;

        switch (dir) {
            case N:
                rowOffset = 1;
                colOffset = 0;
                break;
            case E:
                rowOffset = 0;
                colOffset = 1;
                break;
            case S:
                rowOffset = -1;
                colOffset = 0;
                break;
            case W:
                rowOffset = 0;
                colOffset = -1;
                break;
            case NE:
                rowOffset = 1;
                colOffset = 1;
                break;
            case NW:
                rowOffset = 1;
                colOffset = -1;
                break;
            case SE:
                rowOffset = -1;
                colOffset = 1;
                break;
            case SW:
                rowOffset = -1;
                colOffset = -1;
                break;
            default:
                throw new RuntimeException("Invalid direction");
        }
        for (int i = 1; i <= distance; i++) {
            ChessPosition target = new ChessPosition(row_pos + i * rowOffset,
                    col_pos + i * colOffset);
            if (!DestinationChecks(board, target)) {
                break;
            }
            moves.add(new ChessMove(myPosition, target));
        }
        return moves;
    }

}
