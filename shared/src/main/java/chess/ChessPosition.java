package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private final int rank;
    private final int file;

    public ChessPosition(int row, int col) {
        this.rank = row;
        this.file = col;
    }

    /**
     * @return which row this position is in
     *         1 codes for the bottom row
     */
    public int getRow() { // Should be called rank
        return rank;
    }

    /**
     * @return which column this position is in
     *         1 codes for the left row
     */
    public int getColumn() { // Should be called file
        return file;
    }
}
