package chess;

import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
    private ChessPiece[][] boardMatrix = new ChessPiece[8][8];
public class ChessBoard {
    private ChessPiece[][] boardMatrix = new ChessPiece[8][8];

    public ChessBoard() {

    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        boardMatrix[position.getColumn()-1][position.getRow()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     *         position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return boardMatrix[position.getColumn()-1][position.getRow()-1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
        ChessPosition pos;
        ChessPiece piece;
 
        pos = new ChessPosition(1, 1);
        piece = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        addPiece(pos, piece);
        pos = new ChessPosition(1, 8);
        addPiece(pos, piece);

        pos = new ChessPosition(8, 1);
        piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        addPiece(pos, piece);
        pos = new ChessPosition(8, 8);
        addPiece(pos, piece);

        pos = new ChessPosition(1, 2);
        piece = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        addPiece(pos, piece);
        pos = new ChessPosition(1, 6);
        addPiece(pos, piece);

        pos = new ChessPosition(8, 2);
        piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        addPiece(pos, piece);
        pos = new ChessPosition(8, 6);
        addPiece(pos, piece);

        pos = new ChessPosition(1, 3);
        piece = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        addPiece(pos, piece);
        pos = new ChessPosition(1, 5);
        addPiece(pos, piece);

        pos = new ChessPosition(8, 3);
        piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        addPiece(pos, piece);
        pos = new ChessPosition(8, 5);
        addPiece(pos, piece);



        pos = new ChessPosition(1, 4);
        piece = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
        addPiece(pos, piece);
        pos = new ChessPosition(8, 4);
        piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        addPiece(pos, piece);

        pos = new ChessPosition(1, 5);
        piece = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
        addPiece(pos, piece);
        pos = new ChessPosition(8, 5);
        piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        addPiece(pos, piece);


        for (int i = 1; i <= 8; i++){
            pos = new ChessPosition(2, i);
            piece = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
            addPiece(pos, piece);
        }
        for (int i = 1; i <= 8; i++){
            pos = new ChessPosition(7, i);
            piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            addPiece(pos, piece);
        }

    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPiece piece = boardMatrix[i][j];
                result = 31 * result + (piece == null ? 0 : piece.hashCode());
            }
        }
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChessBoard other = (ChessBoard) obj;
        
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPiece thisPiece = boardMatrix[i][j];
                ChessPiece otherPiece = other.boardMatrix[i][j];
                
                if (thisPiece == null && otherPiece != null) {
                    return false;
                }
                if (thisPiece != null && !thisPiece.equals(otherPiece)) {
                    return false;
                }
            }
        }
        return true;
    public void resetBoard() {
        ChessPosition pos;
        ChessPiece piece;
 
        pos = new ChessPosition(1, 1);
        piece = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        addPiece(pos, piece);
        pos = new ChessPosition(2, 8);
        addPiece(pos, piece);

        pos = new ChessPosition(6, 1);
        piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        addPiece(pos, piece);
        pos = new ChessPosition(6, 8);
        addPiece(pos, piece);




        for (int i = 1; i <= 8; i++){
            pos = new ChessPosition(2, i);
            piece = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
            addPiece(pos, piece);
        }
        for (int i = 1; i <= 8; i++){
            ChessPosition pos = new ChessPosition(6, i);
            ChessPiece piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            addPiece(pos, piece);
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.deepHashCode(boardMatrix);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ChessBoard other = (ChessBoard) obj;
        if (!Arrays.deepEquals(boardMatrix, other.boardMatrix))
            return false;
        return true;
    }
}
