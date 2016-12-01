package ide;

import backend.utils.SourceLoc;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class Highlighting {

    @NotNull private final SourceLoc start;
    @NotNull private final SourceLoc end;
    @NotNull private final Set<String> styles;

    Highlighting(@NotNull SourceLoc start, @NotNull SourceLoc end, @NotNull Set<String> styles) {
        assert start.compareTo(end) <= 0;
        this.start = start;
        this.end = end;
        this.styles = styles;
    }

    Highlighting(@NotNull SourceLoc start, @NotNull SourceLoc end, @NotNull String style) {
        this(start, end, Collections.singleton(style));
    }

    @NotNull
    public SourceLoc getStart() {
        return start;
    }

    @NotNull
    public SourceLoc getEnd() {
        return end;
    }

    @NotNull
    public Set<String> getStyles() {
        return styles;
    }

    @Override
    public String toString() {
        return start + " - " + end + ": " + styles;
    }

    @SuppressWarnings("Duplicates")
    static List<Highlighting> merge(List<Highlighting> h1, List<Highlighting> h2) {
        ListIterator<Highlighting> i1 = h1.listIterator();
        ListIterator<Highlighting> i2 = h2.listIterator();

        List<Highlighting> merged = new ArrayList<>(h1.size() + h2.size());

        Highlighting c1 = i1.hasNext() ? i1.next() : null;
        Highlighting c2 = i2.hasNext() ? i2.next() : null;
        while (c1 != null || c2 != null) {
            if (c1 == null) {
                merged.add(c2);
                c2 = i2.hasNext() ? i2.next() : null;
            } else if (c2 == null) {
                merged.add(c1);
                c1 = i1.hasNext() ? i1.next() : null;
            } else if (c1.start.compareTo(c2.start) <= 0 && c1.end.compareTo(c2.start) <= 0) {
                merged.add(c1);
                c1 = i1.hasNext() ? i1.next() : null;
            } else if (c1.start.compareTo(c2.start) <= 0 && c2.start.compareTo(c1.end) < 0) {
                Set<String> mergedStyles = new HashSet<>(c1.styles.size() + c2.styles.size());
                mergedStyles.addAll(c1.styles);
                mergedStyles.addAll(c2.styles);
                merged.add(new Highlighting(c1.start, c2.start, c1.styles));
                SourceLoc overlappingEnd = SourceLoc.min(c1.end, c2.end);
                merged.add(new Highlighting(c2.start, overlappingEnd, mergedStyles));
                if (c1.end.compareTo(overlappingEnd) > 0) {
                    c1 = new Highlighting(overlappingEnd, c1.end, c1.styles);
                } else {
                    c1 = i1.hasNext() ? i1.next() : null;
                }
                if (c2.end.compareTo(overlappingEnd) > 0) {
                    c2 = new Highlighting(overlappingEnd, c2.end, c2.styles);
                } else {
                    c2 = i2.hasNext() ? i2.next() : null;
                }
            } else if (c2.start.compareTo(c1.start) < 0 && c2.end.compareTo(c1.start) <= 0) {
                merged.add(c2);
                c2 = i2.hasNext() ? i2.next() : null;
            } else if (c2.start.compareTo(c1.start) < 0 && c1.start.compareTo(c2.end) < 0) {
                Set<String> mergedStyles = new HashSet<>(c1.styles.size() + c2.styles.size());
                mergedStyles.addAll(c1.styles);
                mergedStyles.addAll(c2.styles);
                SourceLoc overlappingEnd = SourceLoc.min(c1.end, c2.end);
                merged.add(new Highlighting(c2.start, c1.start, c2.styles));
                merged.add(new Highlighting(c1.start, overlappingEnd, mergedStyles));
                if (c1.end.compareTo(overlappingEnd) > 0) {
                    c1 = new Highlighting(overlappingEnd, c1.end, c1.styles);
                } else {
                    c1 = i1.hasNext() ? i1.next() : null;
                }
                if (c2.end.compareTo(overlappingEnd) > 0) {
                    c2 = new Highlighting(overlappingEnd, c2.end, c2.styles);
                } else {
                    c2 = i2.hasNext() ? i2.next() : null;
                }
            } else {
                throw new RuntimeException("Should not occur");
            }
        }

        return merged;
    }
}
