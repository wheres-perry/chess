package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * 
 */
public class ChessBoard implements Cloneable {
    private ChessPiece[][] boardMatrix = new ChessPiece[8][8];

    public ChessBoard() { // Default constructor no params
        resetBoard();
    }

    public ChessBoard(String layout) { // Laying Groundwork for creating new board states based on a string
        throw new RuntimeException("Not implemented");
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        boardMatrix[position.getColumn() - 1][position.getRow() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     *         position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return boardMatrix[position.getColumn() - 1][position.getRow() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        ChessPosition pos;
        ChessPiece piece;

        boardMatrix = new ChessPiece[8][8];

        // Rooks
        pos = new ChessPosition(1, 1);
        piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        addPiece(pos, piece);
        pos = new ChessPosition(1, 8);
        addPiece(pos, piece);
        pos = new ChessPosition(8, 1);
        piece = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        addPiece(pos, piece);
        pos = new ChessPosition(8, 8);
        addPiece(pos, piece);

        // Knights
        pos = new ChessPosition(1, 2);
        piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        addPiece(pos, piece);
        pos = new ChessPosition(1, 7);
        addPiece(pos, piece);
        pos = new ChessPosition(8, 2);
        piece = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        addPiece(pos, piece);
        pos = new ChessPosition(8, 7);
        addPiece(pos, piece);

        // Bishops
        pos = new ChessPosition(1, 3);
        piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        addPiece(pos, piece);
        pos = new ChessPosition(1, 6);
        addPiece(pos, piece);
        pos = new ChessPosition(8, 3);
        piece = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        addPiece(pos, piece);
        pos = new ChessPosition(8, 6);
        addPiece(pos, piece);

        // Queens
        pos = new ChessPosition(1, 4);
        piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        addPiece(pos, piece);
        pos = new ChessPosition(8, 4);
        piece = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
        addPiece(pos, piece);

        // Kings
        pos = new ChessPosition(1, 5);
        piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        addPiece(pos, piece);
        pos = new ChessPosition(8, 5);
        piece = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
        addPiece(pos, piece);

        // Pawns
        for (int i = 1; i <= 8; i++) {
            pos = new ChessPosition(2, i);
            piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            addPiece(pos, piece);
        }
        for (int i = 1; i <= 8; i++) {
            pos = new ChessPosition(7, i);
            piece = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
            addPiece(pos, piece);
        }

    }

  
    @Override
    public ChessBoard clone() throws CloneNotSupportedException {
        ChessBoard cloned = (ChessBoard) super.clone();
        cloned.boardMatrix = new ChessPiece[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (boardMatrix[i][j] != null) {
                    cloned.boardMatrix[i][j] = boardMatrix[i][j].clone(); // Requires ChessPiece to implement Cloneable
                }
            }
        }
        return cloned;
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
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean isWhite = true;

        for (int r = 7; r >= 0; r--) {
            for (int c = 0; c < 8; c++) {
                ChessPiece piece = boardMatrix[c][r];
                if (piece == null) {
                    if (isWhite) {
                        sb.append("⬜");
                    } else {
                        sb.append("⬛");
                    }
                } else {
                    sb.append(piece.toString()).append(" ");
                }
                isWhite = !(isWhite);
            }
            sb.append("\b");
            sb.append("\n");
        }
        return sb.toString();
    }
}
