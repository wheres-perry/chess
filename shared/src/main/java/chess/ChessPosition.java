package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private final int row;
    private final int col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * @return which row this position is in
     *         1 codes for the bottom row
     */
    public int getRow() { // Should be called rank
        return row;
    }

    /**
     * @return which column this position is in
     *         1 codes for the left row
     */
    public int getColumn() { // Should be called file
        return col;
    }

    public Boolean InBounds() {
        if (col < 1 | col > 8) {
            return false;
        }
        if (row < 1 | row > 8) {
            return false;
        }
        return true;
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

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
