package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private int row_;
    private int col_;

    public ChessPosition(int row, int col) {
        row_ = row;
        col_ = col;
    }

    /**
     * @return which row this position is in
     *         1 codes for the bottom row
     */
    public int getRow() {
        return (row_);
    }

    /**
     * @return which column this position is in
     *         1 codes for the left row
     */
    public int getColumn() {
        return (col_);
    }

    public boolean isPositionInbounds() {
        if ((row_ <= 8) && (row_ >= 1)) {
            if ((col_ <= 8) && (col_ >= 1)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + row_;
        result = prime * result + col_;
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
        if (row_ != other.row_)
            return false;
        if (col_ != other.col_)
            return false;
        return true;
    }

    @Override
    public String toString() {

        return ("(" + row_ + ", " + col_ + ")");
    }
}
