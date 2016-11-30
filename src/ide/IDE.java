package ide;

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
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class IDE extends Application {

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

    @Override
    public void start(Stage primaryStage) {
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.replaceText(0, 0, sampleCode);
        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
                .subscribe(change -> {
                    evaluateCode();
                    codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
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
        JavaDriver.EvaluationResult result = JavaDriver.evaluate(sourceCode);
        resultsArea.clear();
        int currentLine = 1;
        for (Map.Entry<SourceLoc, Value> output : result.getOutput().entrySet()) {
            while (currentLine < output.getKey().getLine()) {
                resultsArea.appendText("\n");
                currentLine++;
            }
            // FIXME: Handle newlines in the output
            resultsArea.appendText(output.getValue().toString());
        }
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

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        SourceManager sourceManager = new SourceManager(text);

        int lastOffset = 0;

        for (Token token : JavaDriver.lex(text)) {
            int startOffset = sourceManager.getGlobalOffset(token.getStartLocation());
            int endOffset = sourceManager.getGlobalOffset(token.getEndLocation());
            if (startOffset > lastOffset) {
                spansBuilder.add(Collections.emptySet(), startOffset - lastOffset);
            }
            String styleName = getStyleNameForToken(token);
            if (styleName == null) {
                spansBuilder.add(Collections.emptySet(), endOffset - startOffset);
            } else {
                spansBuilder.add(Collections.singleton(styleName), endOffset - startOffset);
            }

            lastOffset = endOffset;
        }
        return spansBuilder.create();
    }
}