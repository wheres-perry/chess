package chess;

import java.util.ArrayList;
import java.util.Collection;

public abstract class ChessMovesCalculator extends ChessPiece {

    protected ChessBoard board;
    protected ChessPosition position;

    public ChessMovesCalculator(
            ChessGame.TeamColor color,
            ChessPiece.PieceType type,
            ChessBoard board,
            ChessPosition position) {
        super(color, type);
        this.board = board;
        this.position = position;
    }

    public abstract Collection<ChessMove> moves();

    public boolean isFriend(ChessBoard board, ChessPosition target) {
        ChessPiece piece = board.getPiece(target);
        if (piece == null) {
            return false;
        }
        if (board.getPiece(target).getTeamColor() == color) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isEnemy(ChessBoard board, ChessPosition target) {
        ChessPiece piece = board.getPiece(target);
        if (piece == null) {
            return false;
        }
        if (board.getPiece(target).getTeamColor() == color) {
            return false;
        } else {
            return true;
        }
    }

    final public Collection<ChessMove> linearMove(ChessMove.Direction dir, ChessBoard board, ChessPosition myPosition,
            int distance) {
        if (distance < 0) {
            throw new IllegalArgumentException("Distance is a scalar (cannot be negative)");
        }

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
                throw new IllegalArgumentException("Invalid direction passed to linearMove");
        }
        for (int i = 1; i <= distance; i++) {
            ChessPosition target = new ChessPosition(row_pos + i * rowOffset,
                    col_pos + i * colOffset);
            if (!target.inBounds()) {
                break;
            }
            if (isFriend(board, target)) {
                break;
            }
            moves.add(new ChessMove(myPosition, target));
            if (isEnemy(board, target)) {
                break;
            }
        }
        return moves;
    }

}
