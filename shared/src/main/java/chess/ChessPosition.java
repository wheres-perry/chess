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
        return col;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + row;
        result = prime * result + col;
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
        ChessPosition other = (ChessPosition) obj;
        if (row != other.row)
            return false;
        if (col != other.col)
            return false;
        return true;
    }
}
