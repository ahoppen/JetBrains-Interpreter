package ide;

import backend.errorHandling.Diagnostics;
import backend.interpreter.Value;
import backend.parser.Token;
import backend.utils.SourceLoc;
import backend.utils.SourceManager;
import frontend.JavaDriver;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class IDE extends Application {

    private static class Highlighting {
        final int startOffset;
        final int endOffset;
        final Set<String> style;

        public Highlighting(int startOffset, int endOffset, Set<String> style) {
            if (startOffset > endOffset) {
                System.err.print("xx");
            }
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.style = style;
        }

        public Highlighting(int startOffset, int endOffset, String style) {
            this(startOffset, endOffset, Collections.singleton(style));
        }

        @Override
        public String toString() {
            return startOffset + " - " + endOffset + ": " + style;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static final String sampleCode = String.join("\n", new String[] {
            "var n = 1000",
            "var sequence = map({0, n}, i -> (-1)^i / (2 * i + 1))",
            "var pi = 4 * reduce(sequence, 0, x y -> x + y)",
            "print \"pi = \"",
            "out pi"
    });

    private CodeArea codeArea;
    private CodeArea resultsArea;
    private List<Highlighting> syntaxHighlighting = new ArrayList<>();
    private List<Highlighting> errors = new ArrayList<>();

    @SuppressWarnings("Duplicates")
    private List<Highlighting> mergeHighlighting(List<Highlighting> h1, List<Highlighting> h2) {
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
                Set<String> mergedStyles = new HashSet<>(c1.style.size() + c2.style.size());
                mergedStyles.addAll(c1.style);
                mergedStyles.addAll(c2.style);
                merged.add(new Highlighting(c1.startOffset, c2.startOffset, c1.style));
                int overlappingEnd = Math.min(c1.endOffset, c2.endOffset);
                merged.add(new Highlighting(c2.startOffset, overlappingEnd, mergedStyles));
                if (c1.endOffset > overlappingEnd) {
                    c1 = new Highlighting(overlappingEnd, c1.endOffset, c1.style);
                } else {
                    c1 = i1.hasNext() ? i1.next() : null;
                }
                if (c2.endOffset > overlappingEnd) {
                    c2 = new Highlighting(overlappingEnd, c2.endOffset, c2.style);
                } else {
                    c2 = i2.hasNext() ? i2.next() : null;
                }
            } else if (c2.startOffset < c1.startOffset && c2.endOffset <= c1.startOffset) {
                merged.add(c2);
                c2 = i2.hasNext() ? i2.next() : null;
            } else if (c2.startOffset < c1.startOffset && c1.startOffset < c2.endOffset) {
                Set<String> mergedStyles = new HashSet<>(c1.style.size() + c2.style.size());
                mergedStyles.addAll(c1.style);
                mergedStyles.addAll(c2.style);
                int overlappingEnd = Math.min(c1.endOffset, c2.endOffset);
                merged.add(new Highlighting(c2.startOffset, c1.startOffset, c2.style));
                merged.add(new Highlighting(c1.startOffset, overlappingEnd, mergedStyles));
                if (c1.endOffset > overlappingEnd) {
                    c1 = new Highlighting(overlappingEnd, c1.endOffset, c1.style);
                } else {
                    c1 = i1.hasNext() ? i1.next() : null;
                }
                if (c2.endOffset > overlappingEnd) {
                    c2 = new Highlighting(overlappingEnd, c2.endOffset, c2.style);
                } else {
                    c2 = i2.hasNext() ? i2.next() : null;
                }
            } else {
                throw new RuntimeException("Should not occur");
            }
        }

        return merged;
    }

    @Override
    public void start(Stage primaryStage) {
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.replaceText(0, 0, sampleCode);
        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .subscribe(change -> {
                    syntaxHighlighting = computeSyntaxHighlighting(codeArea.getText());
                    evaluateCode();
                    codeArea.setStyleSpans(0, computeStylesSpans(mergeHighlighting(syntaxHighlighting, errors)));
                });

        resultsArea = new CodeArea();
        resultsArea.setEditable(false);
        resultsArea.setPrefWidth(200);
        resultsArea.setStyle("-fx-background-color: #eee");

        BorderPane mainPane = new BorderPane(codeArea);
        mainPane.setRight(resultsArea);

        Scene scene = new Scene(mainPane, 1024, 800);
        scene.getStylesheets().add(this.getClass().getResource("codeStyle.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Java Keywords Demo");
        primaryStage.show();
    }

    private void evaluateCode() {
        String sourceCode = codeArea.getText();
        SourceManager sourceManager = new SourceManager(sourceCode);

        JavaDriver.EvaluationResult result = JavaDriver.evaluate(sourceCode);
        resultsArea.clear();
        int currentLine = 1;
        List<Map.Entry<SourceLoc, Value>> outputs = new ArrayList<>(result.getOutput().entrySet());
        outputs.sort(Comparator.comparing(Map.Entry::getKey));
        for (Map.Entry<SourceLoc, Value> output : outputs) {
            while (currentLine < output.getKey().getLine()) {
                resultsArea.appendText("\n");
                currentLine++;
            }
            // FIXME: Handle newlines in the output
            resultsArea.appendText(output.getValue().toString());
        }

        List<Highlighting> errorHighlighting = new LinkedList<>();

        for (Diagnostics.Error error : result.getErrors()) {
            int errorOffset = sourceManager.getGlobalOffset(error.getLocation());
            errorHighlighting.add(new Highlighting(errorOffset, errorOffset + 1, "error"));
            break;
        }

        this.errors = errorHighlighting;
    }

    @Nullable
    private static String getStyleNameForToken(@NotNull Token token) {
        switch (token.getKind()) {
            case IDENTIFIER:
                assert token.getPayload() != null;
                switch (token.getPayload()) {
                    case "var":
                    case "out":
                    case "print":
                    case "map":
                    case "reduce":
                        return "keyword";
                    default:
                        return "identifier";
                }
            case ADD:
            case SUB:
            case MULT:
            case DIV:
            case POW:
            case ARROW:
            case COMMA:
            case ASSIGN:
                return "operator";
            case L_PAREN:
            case R_PAREN:
            case L_BRACE:
            case R_BRACE:
                return "braces";
            case INT_LITERAL:
            case FLOAT_LITERAL:
                return "numberLiteral";
            case STRING_LITERAL:
                return "stringLiteral";
            case COMMENT:
                return "comment";
            case ERROR:
            case EOF:
            default:
                return null;
        }
    }

    private static List<Highlighting> computeSyntaxHighlighting(String text) {
        SourceManager sourceManager = new SourceManager(text);

        List<Highlighting> syntaxHighlighting = new LinkedList<>();

        for (Token token : JavaDriver.lex(text)) {
            int startOffset = sourceManager.getGlobalOffset(token.getStartLocation());
            int endOffset = sourceManager.getGlobalOffset(token.getEndLocation());
            String styleName = getStyleNameForToken(token);
            if (styleName != null) {
                syntaxHighlighting.add(new Highlighting(startOffset, endOffset, styleName));
            }
        }
        return syntaxHighlighting;
    }

    private static StyleSpans<Collection<String>> computeStylesSpans(List<Highlighting> highlightings) {
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
            spansBuilder.add(h.style, h.endOffset - h.startOffset);
            lastOffset = h.endOffset;
        }

        return spansBuilder.create();
    }
}