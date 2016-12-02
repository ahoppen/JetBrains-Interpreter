package ide;

import backend.errorHandling.Diagnostics;
import backend.interpreter.Value;
import backend.utils.SourceLoc;
import frontend.JavaDriver;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.MouseOverTextEvent;
import org.fxmisc.richtext.PopupAlignment;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.TwoDimensional;
import org.jetbrains.annotations.NotNull;
import org.reactfx.EventSource;
import org.reactfx.EventStream;
import org.reactfx.EventStreams;
import org.reactfx.util.Tuple2;

import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class IDE extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private static final String sampleCode = String.join("\r\n", new String[] {
            "var n = 1000",
            "var sequence = map({0, n}, i -> (-1)^i / (2 * i + 1))",
            "var pi = 4 * reduce(sequence, 0, x y -> x + y)",
            "print \"pi = \"",
            "out pi"
    });

    private Stage stage;
    private CodeArea codeArea;
    private Popup errorPopup;
    private Button applyFixItButton;
    private HBox popupContent;
    private Diagnostics.Error.FixItInsert currentFixIt;
    private Label errorMessageLabel;
    private boolean popupHovered;
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
        resultsScrollPane.estimatedScrollYProperty().bindBidirectional(
                codeScrollPane.estimatedScrollYProperty());

        BorderPane mainPane = new BorderPane(codeScrollPane);
        mainPane.setRight(resultsArea);
        mainPane.setTop(buildMenu());

        Scene scene = new Scene(mainPane, 1024, 800);
        scene.getStylesheets().add(this.getClass().getResource("codeStyle.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("My Language Editor");
        stage.show();

        errorMessageLabel = new Label();
        errorMessageLabel.setId("errorPopup");

        applyFixItButton = new Button("Fix-It");
        applyFixItButton.setOnAction(e -> {
            int line = currentFixIt.getLocation().getLine() - 1;
            int column = currentFixIt.getLocation().getColumn() - 1;
            String toInsert = currentFixIt.getToInsert();
            codeArea.replaceText(line, column, line, column, toInsert);
        });

        popupContent = new HBox(errorMessageLabel, applyFixItButton);

        errorPopup = new Popup();
        errorPopup.getContent().add(popupContent);
        codeArea.setPopupWindow(errorPopup);
        codeArea.setPopupAlignment(PopupAlignment.SELECTION_BOTTOM_CENTER);

        codeArea.setMouseOverTextDelay(Duration.ofMillis(100));
        codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, event -> {
            int hoveredOffset = event.getCharacterIndex();
            Point2D pos = event.getScreenPosition();

            String errorMessage = getErrorMessage(getSourceLoc(hoveredOffset));

            if (errorMessage != null) {
                errorMessageLabel.setText(errorMessage);
                errorPopup.show(codeArea, pos.getX(), pos.getY() + 10);
                popupContent.requestFocus();
            } else {
                errorPopup.hide();
            }
        });
        codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, e -> {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!popupHovered) {
                        Platform.runLater(() -> errorPopup.hide());
                    }
                }
            }, 500);
        });


        codeArea.caretPositionProperty()
                .addListener((__, ___, newValue) -> showOrHidePopup(getSourceLoc(newValue)));


        popupContent.addEventHandler(MouseEvent.MOUSE_ENTERED, __ -> popupHovered = true);
        popupContent.addEventHandler(MouseEvent.MOUSE_EXITED, __ -> {
            popupHovered = false;
            errorPopup.hide();
        });

        setUpEventStreams();

        codeArea.replaceText(0, 0, sampleCode);
    }

    private MenuBar buildMenu() {
        MenuBar menuBar = new MenuBar();

        Menu menuFile = new Menu("File");
        MenuItem open = new MenuItem("Open");
        open.setOnAction(t -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open File");
            File file = fileChooser.showOpenDialog(stage);
            codeArea.clear();
            try {
                Reader fileReader = new FileReader(file);
                int c = fileReader.read();
                StringBuilder sb = new StringBuilder();
                while (c != -1) {
                    sb.append((char)c);
                    c = fileReader.read();
                }
                codeArea.appendText(sb.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        menuFile.getItems().addAll(open);

        menuBar.getMenus().addAll(menuFile);

        return menuBar;
    }

    private void showOrHidePopup(@NotNull SourceLoc caretPosition) {
        String errorMessage = getErrorMessage(caretPosition);

        if (errorMessage != null) {
            errorMessageLabel.setText(errorMessage);
            errorPopup.show(stage);
            popupContent.requestFocus();
        } else {
            errorPopup.hide();
        }
    }

    private String getErrorMessage(@NotNull SourceLoc sourceLoc) {
        String errorMessage = null;
        for (Diagnostics.Error error : errorMessages) {
            if (error.getStartLocation().compareTo(sourceLoc) <= 0 &&
                    error.getEndLocation().compareTo(sourceLoc) >= 0) {
                errorMessage = error.getMessage();
                if (error.getFixIt() != null) {
                    currentFixIt = error.getFixIt();
                    applyFixItButton.setVisible(true);
                } else {
                    applyFixItButton.setVisible(false);
                }
            }
        }
        return errorMessage;
    }

    @NotNull
    private SourceLoc getCurrentSourceLoc() {
        return getSourceLoc(codeArea.getCaretPosition());
    }

    @NotNull
    private SourceLoc getSourceLoc(int offset) {
        TwoDimensional.Position pos = codeArea.offsetToPosition(offset,
                TwoDimensional.Bias.Forward);
        return new SourceLoc(pos.getMajor() + 1, pos.getMinor() + 1);
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
                .map(Evaluation::getErrorHighlighting)
                .subscribe(errorsStream::push);

        evaluationResult
                .map(JavaDriver.EvaluationResult::getOutput)
                .map(output -> Evaluation.getResultsAreaText(output, codeArea.getParagraphs().size()))
                .subscribe(resultsStream::push);

        evaluationResult
                .map(JavaDriver.EvaluationResult::getErrors)
                .subscribe(errors -> {
                    this.errorMessages = errors;
                    showOrHidePopup(getCurrentSourceLoc());
                });

        codeArea.plainTextChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .subscribe(value -> errorsStream.push(new ArrayList<>(0)));

        evaluationResult
                .map(JavaDriver.EvaluationResult::getOutput)
                .subscribe(output -> {
                    // Clear all existing lines
                    for (int line = 0; line < resultsArea.getParagraphs().size(); line++) {
                        Paragraph paragraph = resultsArea.getParagraph(line);
                        resultsArea.replaceText(line, 0, line, paragraph.length(),
                                "");
                    }
                    // Create enough new lines to match the number of lines in the code
                    while (resultsArea.getParagraphs().size() <= codeArea.getParagraphs().size()) {
                        resultsArea.appendText("\n");
                    }
                    // Delete any remaining lines in the results area
                    if (resultsArea.getParagraphs().size() > codeArea.getParagraphs().size()) {
                        Paragraph lastParagraph = resultsArea.getParagraph(
                                codeArea.getParagraphs().size() - 1);
                        resultsArea.replaceText(codeArea.getParagraphs().size() - 1,
                                lastParagraph.length(),
                                resultsArea.getParagraphs().size() - 1,
                                0,
                                "");
                    }
                    // Fill the lines with the results
                    for (Map.Entry<SourceLoc, Value> entry : output.entrySet()) {
                        int line = entry.getKey().getLine() - 1;
                        resultsArea.replaceText(line, 0, line, 0,
                                entry.getValue().toString());
                    }
                });


        EventStreams.combine(syntaxHighlighting, errorsStream)
                .mapToTask(this::mergeHighlights)
                .awaitLatest(codeArea.plainTextChanges())
                .filterMap(task -> {
                    if (task.isSuccess()) {
                        return Optional.of(task.get());
                    } else {
                        return Optional.empty();
                    }
                })
                .subscribe(highlightings -> {
                    for (Highlighting h : highlightings) {
                        assert h.getStart().getLine() == h.getEnd().getLine();
                        int line = h.getStart().getLine() - 1;
                        int from = h.getStart().getColumn() - 1;
                        int to = h.getEnd().getColumn() - 1;
                        codeArea.setStyle(line, from, to, h.getStyles());
                    }
                });
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

    private Task<List<Highlighting>> mergeHighlights(Tuple2<List<Highlighting>,
            List<Highlighting>> h) {
        Task<List<Highlighting>> task = new Task<List<Highlighting>>() {
            @Override
            protected List<Highlighting> call() throws Exception {
                return Highlighting.merge(h.get1(), h.get2());
            }
        };
        executor.execute(task);
        return task;
    }
}