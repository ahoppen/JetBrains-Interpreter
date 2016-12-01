package ide;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.reactfx.EventSource;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;
import org.reactfx.util.Tuple2;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

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
    private ExecutorService executor;

    @Override
    public void start(Stage primaryStage) {
        executor = Executors.newCachedThreadPool();
        System.out.println(Thread.currentThread());

        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.replaceText(0, 0, sampleCode);

        VirtualizedScrollPane<CodeArea> codeScrollPane = new VirtualizedScrollPane<>(codeArea);

        resultsArea = new CodeArea();
        resultsArea.setEditable(false);
        resultsArea.setPrefWidth(200);
        resultsArea.setStyle("-fx-background-color: #eee");
        VirtualizedScrollPane<CodeArea> resultsScrollPane = new VirtualizedScrollPane<>(resultsArea);
        codeScrollPane.estimatedScrollYProperty().bindBidirectional(
                resultsScrollPane.estimatedScrollYProperty());

        BorderPane mainPane = new BorderPane(codeScrollPane);
        mainPane.setRight(resultsArea);

        Scene scene = new Scene(mainPane, 1024, 800);
        scene.getStylesheets().add(this.getClass().getResource("codeStyle.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("My Language Editor");
        primaryStage.show();

        setUpEventStreams();
    }

    private void setUpEventStreams() {
        EventStream<List<Highlighting>> syntaxHighlighting = codeArea.plainTextChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .successionEnds(Duration.ofMillis(10))
                .supplyTask(this::computeSyntaxHighlightingAsync)
                .awaitLatest(codeArea.plainTextChanges())
                .filterMap(task -> {
                    if (task.isSuccess()) {
                        return Optional.of(task.get());
                    } else {
                        return Optional.empty();
                    }
                });

        EventSource<List<Highlighting>> errorsStream = new EventSource<>();
        EventSource<String> resultsStream = new EventSource<>();
        codeArea.plainTextChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .successionEnds(Duration.ofMillis(500))
                .supplyTask(this::evaluateCodeAsync)
                .awaitLatest(codeArea.plainTextChanges())
                .filterMap(task -> {
                    if (task.isSuccess()) {
                        return Optional.of(task.get());
                    } else {
                        return Optional.empty();
                    }
                })
                .subscribe(value -> {
                    errorsStream.push(value.getKey());
                    resultsStream.push(value.getValue());
                });

        codeArea.plainTextChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .subscribe(value -> errorsStream.push(new ArrayList<>(0)));

        resultsStream.subscribe(results -> {
            resultsArea.clear();
            resultsArea.appendText(results);
        });

        EventStreams.combine(syntaxHighlighting, errorsStream)
                .mapToTask(this::computeStyleSpans)
                .awaitLatest(codeArea.plainTextChanges())
                .filterMap(task -> {
                    if (task.isSuccess()) {
                        return Optional.of(task.get());
                    } else {
                        return Optional.empty();
                    }
                })
                .subscribe(spans -> codeArea.setStyleSpans(0, spans));
    }

    private Task<Pair<List<Highlighting>, String>> evaluateCodeAsync() {
        return performOnSourceCode(Evaluation::evaluateCode);
    }

    private Task<List<Highlighting>> computeSyntaxHighlightingAsync() {
        return performOnSourceCode(Evaluation::computeSyntaxHighlighting);
    }

    private <T> Task<T> performOnSourceCode(Function<String, T> toPerform) {
        String text = codeArea.getText();
        Task<T> task = new Task<T>() {
            @Override
            protected T call() throws Exception {
                return toPerform.apply(text);
            }
        };
        executor.execute(task);
        return task;
    }

    private Task<StyleSpans<Collection<String>>> computeStyleSpans(Tuple2<List<Highlighting>,
            List<Highlighting>> h) {
        Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
            @Override
            protected StyleSpans<Collection<String>> call() throws Exception {
                return Highlighting.computeStylesSpans(Highlighting.merge(h.get1(), h.get2()));
            }
        };
        executor.execute(task);
        return task;
    }
}