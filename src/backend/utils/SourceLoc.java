package backend.utils;

import org.jetbrains.annotations.NotNull;

public final class SourceLoc implements Comparable<SourceLoc> {

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
        if (obj == this) {
            return true;
        }
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


    @Override
    public int compareTo(@NotNull SourceLoc o) {
        if (line != o.line) {
            return line - o.line;
        }
        return column - o.column;
    }

    public static SourceLoc min(@NotNull SourceLoc x, @NotNull SourceLoc y) {
        if (x.compareTo(y) <= 0) {
            return x;
        } else {
            return y;
        }
    }
}
