package utils;

public class SourceLoc {

    private final int line;
    private final int column;

    public SourceLoc(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SourceLoc)) {
            return false;
        }
        SourceLoc other = ((SourceLoc)obj);
        return other.line == line && other.column == column;
    }

    @Override
    public int hashCode() {
        return line * 10000 + column;
    }

    @Override
    public String toString() {
        return line + ":" + column;
    }
}
