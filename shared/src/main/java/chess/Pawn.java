package chess;

import java.util.ArrayList;
// import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Pawn extends ChessMovesCalculator {

    public Pawn(ChessBoard board, ChessPosition position, ChessGame.TeamColor color) {
        super(color, ChessPiece.PieceType.PAWN, board, position);
    }

    int promoRow;

    final public Collection<ChessMove> promotionWrapper(ChessPosition target) {
        Collection<ChessMove> moves = new ArrayList<>();
        if (color == ChessGame.TeamColor.BLACK) {
            promoRow = 1;
        } else {
            promoRow = 8;
        }

        if (target.getRow() == promoRow) {
            Collection<PieceType> PromotionPieces = List.of(PieceType.KNIGHT, PieceType.BISHOP, PieceType.ROOK,
                    PieceType.QUEEN);
            for (PieceType p : PromotionPieces) {
                moves.add(new ChessMove(position, target, p));
            }
        } else {
            moves.add(new ChessMove(position, target));
        }
        return moves;
    }

    final public boolean isFree(ChessPosition target) {
        if (!target.inBounds()) {
            return false;
        }
        if (isFriend(board, target)) {
            return false;
        }
        if (isEnemy(board, target)) {
            return false;
        }
        return true;
    }

    @Override
    final public Collection<ChessMove> moves() {
        Collection<ChessMove> output = new ArrayList<>();
        final int rowPos = position.getRow();
        final int colPos = position.getColumn();

        ChessPosition attackSquare1 = null;
        ChessPosition attackSquare2 = null;
        ChessPosition forwardSquare = null;
        ChessPosition firstMove = null;

        if (color == ChessGame.TeamColor.BLACK) {
            attackSquare1 = new ChessPosition(rowPos - 1, colPos + 1);
            attackSquare2 = new ChessPosition(rowPos - 1, colPos - 1);
            forwardSquare = new ChessPosition(rowPos - 1, colPos);
            firstMove = new ChessPosition(5, colPos);

        } else {
            attackSquare1 = new ChessPosition(rowPos + 1, colPos + 1);
            attackSquare2 = new ChessPosition(rowPos + 1, colPos - 1);
            forwardSquare = new ChessPosition(rowPos + 1, colPos);
            firstMove = new ChessPosition(4, colPos);
        }

        int doubleRow;
        if (color == ChessGame.TeamColor.BLACK) {
            doubleRow = 7;
        } else {
            doubleRow = 2;
        }
        if (isFree(forwardSquare)) {
            output.addAll(promotionWrapper(forwardSquare));
            if ((position.getRow() == doubleRow) && (board.getPiece(firstMove) == null)) {
                output.addAll(promotionWrapper(firstMove));
            }
        }

        // Attack Movement
        if (attackSquare1.inBounds() && isEnemy(board, attackSquare1)) {
            output.addAll(promotionWrapper(attackSquare1));
        }
        if (attackSquare2.inBounds() && isEnemy(board, attackSquare2)) {
            output.addAll(promotionWrapper(attackSquare2));
        }

        return output;
    }
}
