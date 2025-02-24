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

    final public Collection<ChessMove> PromotionWrapper(ChessPosition target) {
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

    final public boolean IsFree(ChessPosition target) {
        if (!target.InBounds()) {
            return false;
        }
        if (IsFriend(board, target)) {
            return false;
        }
        if (IsEnemy(board, target)) {
            return false;
        }
        return true;
    }

    @Override
    final public Collection<ChessMove> moves() {
        Collection<ChessMove> output = new ArrayList<>();
        final int row_pos = position.getRow();
        final int col_pos = position.getColumn();

        ChessPosition attackSquare1 = null;
        ChessPosition attackSquare2 = null;
        ChessPosition forwardSquare = null;
        ChessPosition firstMove = null;

        if (color == ChessGame.TeamColor.BLACK) {
            attackSquare1 = new ChessPosition(row_pos - 1, col_pos + 1);
            attackSquare2 = new ChessPosition(row_pos - 1, col_pos - 1);
            forwardSquare = new ChessPosition(row_pos - 1, col_pos);
            firstMove = new ChessPosition(5, col_pos);

        } else {
            attackSquare1 = new ChessPosition(row_pos + 1, col_pos + 1);
            attackSquare2 = new ChessPosition(row_pos + 1, col_pos - 1);
            forwardSquare = new ChessPosition(row_pos + 1, col_pos);
            firstMove = new ChessPosition(4, col_pos);
        }
        // TODO: Implement En Passant
        // ChessPosition EPSquare1 = new ChessPosition(row_pos, col_pos+1);
        // ChessPosition EPSquare2 = new ChessPosition(row_pos, col_pos-1);
        // Some check to see if square is occupied with recently moved pawn
        // Then add diagonal attack to the moveset

        // Autograder doesn't like my implementation
        // if (!HasMoved){
        // if (IsFree(firstMove)){
        // output.addAll(PromotionWrapper(firstMove));
        // }
        // }

        // Forward Movement
        int doubleRow;
        if (color == ChessGame.TeamColor.BLACK) {
            doubleRow = 7;
        } else {
            doubleRow = 2;
        }
        if (IsFree(forwardSquare)) {
            output.addAll(PromotionWrapper(forwardSquare));
            if ((position.getRow() == doubleRow) && (board.getPiece(firstMove) == null)) {
                output.addAll(PromotionWrapper(firstMove));
            }
        }

        // Attack Movement
        if (attackSquare1.InBounds() && IsEnemy(board, attackSquare1)) {
            output.addAll(PromotionWrapper(attackSquare1));
        }
        if (attackSquare2.InBounds() && IsEnemy(board, attackSquare2)) {
            output.addAll(PromotionWrapper(attackSquare2));
        }

        return output;
    }
}
