package chess;

import chess.ChessGame.TeamColor;
import chess.ChessPiece.PieceType;
import java.util.ArrayList;
import java.util.Collection;

import javax.management.RuntimeErrorException;

public class PCalc {

    private ChessPiece piece;
    private ChessPosition pos;
    private ChessBoard board;
    Collection<ChessMove> output;

    PCalc(ChessPiece Piece, ChessPosition Position, ChessBoard b) {
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

    Collection<ChessMove> promoWrapper(ChessMove m, int p) {
        Collection<PieceType> promos = new ArrayList<>();
        promos.add(PieceType.KNIGHT);
        promos.add(PieceType.BISHOP);
        promos.add(PieceType.QUEEN);
        promos.add(PieceType.ROOK);

        ChessPosition s = m.getStartPosition();
        ChessPosition e = m.getEndPosition();

        Collection<ChessMove> o = new ArrayList<>();
        if (e.getRow() == p) {
            for (PieceType q : promos) {
                ChessMove newmove = new ChessMove(s, e, q);
                o.add(newmove);
            }

        } else {
            o.add(m);
        }
        return o;
    }

    Collection<ChessMove> calculate() {
        Collection<ChessMove> output = new ArrayList<>();
        int myRow = pos.getRow();
        int myCol = pos.getColumn();
        int promoRow;
        int startRow;

        if (piece.color == TeamColor.WHITE) {
            promoRow = 8;
            startRow = 2;

            ChessPosition hitSquare1 = new ChessPosition(myRow + 1, myCol + 1);
            ChessPosition hitSquare2 = new ChessPosition(myRow + 1, myCol - 1);
            ChessPosition forwardSquare = new ChessPosition(myRow + 1, myCol);

            ChessMove m = null;
            ChessPiece p;
            if (hitSquare1.isPositionInbounds()) {
                p = board.getPiece(hitSquare1);
                if (p != null) {
                    if (isEnemy(hitSquare1)) {
                        m = new ChessMove(pos, hitSquare1);
                        output.addAll(promoWrapper(m, promoRow));
                    }
                }
            }
            
            if (hitSquare2.isPositionInbounds()) {
                p = board.getPiece(hitSquare2);
                if (p != null) {
                    if (isEnemy(hitSquare2)) {
                        m = new ChessMove(pos, hitSquare2);
                        output.addAll(promoWrapper(m, promoRow));
                    }
                }
            }

            p = board.getPiece(forwardSquare);
            if (p == null) {
                m = new ChessMove(pos, forwardSquare);
                output.addAll(promoWrapper(m, promoRow));
                if (pos.getRow() == startRow) {
                    ChessPosition s = new ChessPosition(myRow + 2, myCol);
                    p = board.getPiece(s);
                    if (p == null) {
                        m = new ChessMove(pos, s);
                        output.addAll(promoWrapper(m, promoRow));
                    }
                }
            }

        } else {
            promoRow = 1;
            startRow = 7;

            ChessPosition hitSquare1 = new ChessPosition(myRow - 1, myCol + 1);
            ChessPosition hitSquare2 = new ChessPosition(myRow - 1, myCol - 1);
            ChessPosition forwardSquare = new ChessPosition(myRow - 1, myCol);

            ChessMove m = null;
            ChessPiece p;

            if (hitSquare1.isPositionInbounds()) {
                p = board.getPiece(hitSquare1);
                if (p != null) {
                    if (isEnemy(hitSquare1)) {
                        m = new ChessMove(pos, hitSquare1);
                        output.addAll(promoWrapper(m, promoRow));
                    }
                }
            }

        
            if (hitSquare2.isPositionInbounds()) {
                p = board.getPiece(hitSquare2);
                if (p != null) {
                    if (isEnemy(hitSquare2)) {
                        m = new ChessMove(pos, hitSquare2);
                        output.addAll(promoWrapper(m, promoRow));
                    }
                }
            }

            p = board.getPiece(forwardSquare);
            if (p == null) {
                m = new ChessMove(pos, forwardSquare);
                output.addAll(promoWrapper(m, promoRow));
                if (pos.getRow() == startRow) {
                    ChessPosition s = new ChessPosition(myRow - 2, myCol);
                    p = board.getPiece(s);
                    if (p == null) {
                        m = new ChessMove(pos, s);
                        output.addAll(promoWrapper(m, promoRow));
                    }
                }
            }

        }

        return output;

    }

}
