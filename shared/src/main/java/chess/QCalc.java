package chess;

import java.util.ArrayList;
import java.util.Collection;

import javax.management.RuntimeErrorException;

public class QCalc {

    private ChessPiece piece;
    private ChessPosition pos;
    private ChessBoard board;
    Collection<ChessMove> output;

    QCalc(ChessPiece Piece, ChessPosition Position, ChessBoard b) {
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

        ChessPosition target = pos;
        int c = 1;

        target = new ChessPosition(myRow, myCol + c);
        while (target.isPositionInbounds()) {
            c++;
            if (isFriend(target)) {
                break;
            }

            if (isEnemy(target)) {
                ChessMove validMove = new ChessMove(pos, target);
                output.add(validMove);
                break;
            }
            if (!isFriend(target) && isEmpty(target) && target.isPositionInbounds()) {
                ChessMove validMove = new ChessMove(pos, target);
                output.add(validMove);
            }
            target = new ChessPosition(myRow, myCol + c);
        }

        // North West
        c = 1;
        target = new ChessPosition(myRow, myCol - c);
        while (target.isPositionInbounds()) {
            c++;
            if (isFriend(target)) {
                break;
            }

            if (isEnemy(target)) {
                ChessMove validMove = new ChessMove(pos, target);
                output.add(validMove);
                break;
            }
            if (!isFriend(target) && isEmpty(target) && target.isPositionInbounds()) {
                ChessMove validMove = new ChessMove(pos, target);
                output.add(validMove);
            }
            target = new ChessPosition(myRow, myCol - c);
        }

        c = 1;
        target = new ChessPosition(myRow + c, myCol);
        while (target.isPositionInbounds()) {
            c++;
            if (isFriend(target)) {
                break;
            }

            if (isEnemy(target)) {
                ChessMove validMove = new ChessMove(pos, target);
                output.add(validMove);
                break;
            }
            if (!isFriend(target) && isEmpty(target) && target.isPositionInbounds()) {
                ChessMove validMove = new ChessMove(pos, target);
                output.add(validMove);
            }
            target = new ChessPosition(myRow + c, myCol);
        }

        c = 1;
        target = new ChessPosition(myRow - c, myCol);
        while (target.isPositionInbounds()) {
            c++;
            if (isFriend(target)) {
                break;
            }

            if (isEnemy(target)) {
                ChessMove validMove = new ChessMove(pos, target);
                output.add(validMove);
                break;
            }
            if (!isFriend(target) && isEmpty(target) && target.isPositionInbounds()) {
                ChessMove validMove = new ChessMove(pos, target);
                output.add(validMove);
            }
            target = new ChessPosition(myRow - c, myCol);
        }

        c = 1;
        target = new ChessPosition(myRow + c, myCol + c);
        while (target.isPositionInbounds()) {
            c++;
            if (isFriend(target)) {
                break;
            }

            if (isEnemy(target)) {
                ChessMove validMove = new ChessMove(pos, target);
                output.add(validMove);
                break;
            }
            if (!isFriend(target) && isEmpty(target) && target.isPositionInbounds()) {
                ChessMove validMove = new ChessMove(pos, target);
                output.add(validMove);
            }
            target = new ChessPosition(myRow + c, myCol + c);
        }

        // North West
        c = 1;
        target = new ChessPosition(myRow + c, myCol - c);
        while (target.isPositionInbounds()) {
            c++;
            if (isFriend(target)) {
                break;
            }

            if (isEnemy(target)) {
                ChessMove validMove = new ChessMove(pos, target);
                output.add(validMove);
                break;
            }
            if (!isFriend(target) && isEmpty(target) && target.isPositionInbounds()) {
                ChessMove validMove = new ChessMove(pos, target);
                output.add(validMove);
            }
            target = new ChessPosition(myRow + c, myCol - c);
        }

        c = 1;
        target = new ChessPosition(myRow - c, myCol + c);
        while (target.isPositionInbounds()) {
            c++;
            if (isFriend(target)) {
                break;
            }

            if (isEnemy(target)) {
                ChessMove validMove = new ChessMove(pos, target);
                output.add(validMove);
                break;
            }
            if (!isFriend(target) && isEmpty(target) && target.isPositionInbounds()) {
                ChessMove validMove = new ChessMove(pos, target);
                output.add(validMove);
            }
            target = new ChessPosition(myRow - c, myCol + c);
        }

        c = 1;
        target = new ChessPosition(myRow - c, myCol - c);
        while (target.isPositionInbounds()) {
            c++;
            if (isFriend(target)) {
                break;
            }

            if (isEnemy(target)) {
                ChessMove validMove = new ChessMove(pos, target);
                output.add(validMove);
                break;
            }
            if (!isFriend(target) && isEmpty(target) && target.isPositionInbounds()) {
                ChessMove validMove = new ChessMove(pos, target);
                output.add(validMove);
            }
            target = new ChessPosition(myRow - c, myCol - c);
        }

        return output;
    }

}
