package ide;

import backend.errorHandling.Diagnostics;
import backend.utils.SourceManager;
import frontend.JavaDriver;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.MouseOverTextEvent;
import org.fxmisc.richtext.PopupAlignment;
import org.fxmisc.richtext.model.StyleSpans;
import org.jetbrains.annotations.NotNull;
import org.reactfx.EventSource;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;
import org.reactfx.util.Tuple2;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

    private Stage stage;
    private CodeArea codeArea;
    private Popup errorPopup;
    private Label errorMessageLabel;
    private CodeArea resultsArea;
    private ExecutorService executor;
    private List<Diagnostics.Error> errorMessages = new ArrayList<>(0);

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        executor = Executors.newCachedThreadPool();

        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

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
        stage.setScene(scene);
        stage.setTitle("My Language Editor");
        stage.show();

        errorPopup = new Popup();
        errorMessageLabel = new Label();
        errorMessageLabel.setId("errorPopup");
        errorPopup.getContent().add(errorMessageLabel);
        codeArea.setPopupWindow(errorPopup);
        codeArea.setPopupAlignment(PopupAlignment.SELECTION_BOTTOM_CENTER);

        codeArea.setMouseOverTextDelay(Duration.ofMillis(100));
        codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, event -> {
            int hoveredOffset = event.getCharacterIndex();
            Point2D pos = event.getScreenPosition();

            SourceManager sourceManager = new SourceManager(codeArea.getText());

            String errorMessage = getErrorMessage(sourceManager, hoveredOffset);

            if (errorMessage != null) {
                errorMessageLabel.setText(errorMessage);
                errorPopup.show(codeArea, pos.getX(), pos.getY() + 10);
            } else {
                errorPopup.hide();
            }
        });
        codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, e -> errorPopup.hide());

        codeArea.caretPositionProperty().addListener((observable, oldValue, newValue) -> {
            showOrHidePopup(newValue);
        });

        setUpEventStreams();

        codeArea.replaceText(0, 0, sampleCode);
    }

    private void showOrHidePopup(int caretPosition) {
        SourceManager sourceManager = new SourceManager(codeArea.getText());
        String errorMessage = getErrorMessage(sourceManager, caretPosition);

        if (errorMessage != null) {
            errorMessageLabel.setText(errorMessage);
            errorPopup.show(stage);
        } else {
            errorPopup.hide();
        }
    }

    private String getErrorMessage(@NotNull SourceManager sourceManager, int atOffset) {
        String errorMessage = null;
        for (Diagnostics.Error error : errorMessages) {
            if (sourceManager.getGlobalOffset(error.getStartLocation()) <= atOffset &&
                    atOffset <= sourceManager.getGlobalOffset(error.getEndLocation())) {
                errorMessage = error.getMessage();
            }
        }
        return errorMessage;
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
        EventStream<JavaDriver.EvaluationResult> evaluationResult = codeArea.plainTextChanges()
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
                });

        evaluationResult
                .map(JavaDriver.EvaluationResult::getErrors)
                .map(errors -> Evaluation.getErrorHighlighting(errors, codeArea.getText()))
                .subscribe(errorsStream::push);

        evaluationResult
                .map(JavaDriver.EvaluationResult::getOutput)
                .map(output -> Evaluation.getResultsAreaText(output, codeArea.getText()))
                .subscribe(resultsStream::push);

        evaluationResult
                .map(JavaDriver.EvaluationResult::getErrors)
                .subscribe(errors -> {
                    this.errorMessages = errors;
                    showOrHidePopup(codeArea.getCaretPosition());
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

    private Task<JavaDriver.EvaluationResult> evaluateCodeAsync() {
        return performOnSourceCode(JavaDriver::evaluate);
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