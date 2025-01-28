package chess;


import java.util.ArrayList;
import java.util.Collection;

import javax.management.RuntimeErrorException;

public class NCalc {

    private ChessPiece piece;
    private ChessPosition pos;
    private ChessBoard board;
    Collection<ChessMove> output;

    NCalc(ChessPiece Piece, ChessPosition Position, ChessBoard b) {
        piece = Piece;
        pos = Position;
        board = b;
        if (Position.isPositionInbounds() == false) {
            throw new RuntimeErrorException(null);
        }
    }

    boolean isFriend(ChessPosition target) {
        ChessPiece p = board.getPiece(target);
        if (p == null) {
            return false;
        }
        if (piece.getTeamColor() == p.getTeamColor()) {
            return true;
        }
        return false;
    }

    boolean isEnemy(ChessPosition target) {
        ChessPiece p = board.getPiece(target);
        if (p == null) {
            return false;
        }
        if (piece.getTeamColor() != p.getTeamColor()) {
            return true;
        }
        return false;
    }

    boolean isEmpty(ChessPosition target) {
        ChessPiece p = board.getPiece(target);
        if (p == null) {
            return true;
        }
        return false;
    }




    Collection<ChessMove> calculate() {
    
        int myRow = pos.getRow();
        int myCol = pos.getColumn();

        ArrayList<ChessMove> output = new ArrayList<>();
        int[][] moves = { { 1, 2 }, { 2, 1 }, { -1, 2 }, { -2, 1 }, { 1, -2 }, { 2, -1 }, { -1, -2 }, { -2, -1 } };

        for (int[] move : moves) {
            ChessPosition target = new ChessPosition(myRow + move[0], myCol + move[1]);
            if (!target.isPositionInbounds()) {
                continue;
            }
            if (isFriend(target)) {
                continue;
            }
            if (isEnemy(target) || isEmpty(target)) {
                ChessMove validMove = new ChessMove(pos, target);
                output.add(validMove);
            }
        }
        




        return output;
    }

}
