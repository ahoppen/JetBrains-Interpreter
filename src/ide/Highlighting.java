package ide;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class Highlighting {

    private final int startOffset;
    private final int endOffset;
    @NotNull private final Set<String> styles;

    Highlighting(int startOffset, int endOffset, @NotNull Set<String> styles) {
        assert startOffset <= endOffset;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.styles = styles;
    }

    Highlighting(int startOffset, int endOffset, String style) {
        this(startOffset, endOffset, Collections.singleton(style));
    }

    @Override
    public String toString() {
        return startOffset + " - " + endOffset + ": " + styles;
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
            } else if (c1.startOffset <= c2.startOffset && c1.endOffset <= c2.startOffset) {
                merged.add(c1);
                c1 = i1.hasNext() ? i1.next() : null;
            } else if (c1.startOffset <= c2.startOffset && c2.startOffset < c1.endOffset) {
                Set<String> mergedStyles = new HashSet<>(c1.styles.size() + c2.styles.size());
                mergedStyles.addAll(c1.styles);
                mergedStyles.addAll(c2.styles);
                merged.add(new Highlighting(c1.startOffset, c2.startOffset, c1.styles));
                int overlappingEnd = Math.min(c1.endOffset, c2.endOffset);
                merged.add(new Highlighting(c2.startOffset, overlappingEnd, mergedStyles));
                if (c1.endOffset > overlappingEnd) {
                    c1 = new Highlighting(overlappingEnd, c1.endOffset, c1.styles);
                } else {
                    c1 = i1.hasNext() ? i1.next() : null;
                }
                if (c2.endOffset > overlappingEnd) {
                    c2 = new Highlighting(overlappingEnd, c2.endOffset, c2.styles);
                } else {
                    c2 = i2.hasNext() ? i2.next() : null;
                }
            } else if (c2.startOffset < c1.startOffset && c2.endOffset <= c1.startOffset) {
                merged.add(c2);
                c2 = i2.hasNext() ? i2.next() : null;
            } else if (c2.startOffset < c1.startOffset && c1.startOffset < c2.endOffset) {
                Set<String> mergedStyles = new HashSet<>(c1.styles.size() + c2.styles.size());
                mergedStyles.addAll(c1.styles);
                mergedStyles.addAll(c2.styles);
                int overlappingEnd = Math.min(c1.endOffset, c2.endOffset);
                merged.add(new Highlighting(c2.startOffset, c1.startOffset, c2.styles));
                merged.add(new Highlighting(c1.startOffset, overlappingEnd, mergedStyles));
                if (c1.endOffset > overlappingEnd) {
                    c1 = new Highlighting(overlappingEnd, c1.endOffset, c1.styles);
                } else {
                    c1 = i1.hasNext() ? i1.next() : null;
                }
                if (c2.endOffset > overlappingEnd) {
                    c2 = new Highlighting(overlappingEnd, c2.endOffset, c2.styles);
                } else {
                    c2 = i2.hasNext() ? i2.next() : null;
                }
            } else {
                throw new RuntimeException("Should not occur");
            }
        }

        return merged;
    }

    static StyleSpans<Collection<String>> computeStylesSpans(List<Highlighting> highlightings) {
        int lastOffset = 0;

        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        for (Highlighting h : highlightings) {
            if (h.startOffset > lastOffset) {
                spansBuilder.add(Collections.emptySet(), h.startOffset - lastOffset);
                lastOffset = h.startOffset;
            }
            if (h.startOffset != lastOffset) {
                throw new RuntimeException("Overlapping highlights");
            }
            spansBuilder.add(h.styles, h.endOffset - h.startOffset);
            lastOffset = h.endOffset;
        }

        return spansBuilder.create();
    }
}
