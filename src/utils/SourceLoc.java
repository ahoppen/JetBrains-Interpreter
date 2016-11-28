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
}
