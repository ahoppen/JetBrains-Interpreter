package ide;

import backend.errorHandling.Diagnostics;
import backend.interpreter.Value;
import backend.utils.SourceLoc;
import frontend.JavaDriver;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.MouseOverTextEvent;
import org.fxmisc.richtext.PopupAlignment;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.TwoDimensional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    private static final String sampleCode = String.join("\n", new String[] {
            "var n = 1000",
            "var sequence = map({0, n}, i -> (-1)^i / (2 * i + 1))",
            "var pi = 4 * reduce(sequence, 0, x y -> x + y)",
            "print \"pi = \" out pi"
    });

    private Stage stage;
    private CodeArea codeArea;
    private CodeArea resultsArea;
    private ExecutorService executor;
    private List<Diagnostics.Error> errorMessages = new ArrayList<>(0);
    private ErrorPopupModel errorPopupModel;
    @Nullable private File currentFile;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        executor = Executors.newCachedThreadPool();

        // Set up the UI
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
        mainPane.setTop(createMenu());

        Scene scene = new Scene(mainPane, 1024, 800);
        scene.getStylesheets().add(this.getClass().getResource("codeStyle.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Untitled");
        stage.show();

        // Configure the errors popup
        errorPopupModel = new ErrorPopupModel(codeArea, (line, column, toInsert) -> {
            codeArea.replaceText(line, column, line, column, toInsert);
        });

        codeArea.setPopupAlignment(PopupAlignment.SELECTION_BOTTOM_CENTER);

        codeArea.setMouseOverTextDelay(Duration.ofMillis(100));
        codeArea.caretPositionProperty()
                .addListener((__, ___, newValue) -> {
                    Diagnostics.Error error = getError(getSourceLoc(newValue));
                    errorPopupModel.showOrHidePopup(stage, error);
        });

        codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, event -> {
            int hoveredOffset = event.getCharacterIndex();
            Diagnostics.Error error = getError(getSourceLoc(hoveredOffset));

            errorPopupModel.showOrHidePopup(codeArea, event.getScreenPosition(), error);
        });
        codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, e -> {
            errorPopupModel.showOrHidePopup(stage, null);
        });


        // Set up event streams to evaluate the source code
        setUpEventStreams();

        // Inject the sample code
        codeArea.replaceText(0, 0, sampleCode);
    }

    private MenuBar createMenu() {
        MenuBar menuBar = new MenuBar();

        Menu menuFile = new Menu("File");
        MenuItem open = new MenuItem("Open");
        open.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
        open.setOnAction(__ -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open File");
            FileChooser.ExtensionFilter mlFilter = new FileChooser.ExtensionFilter(
                    "MyLanguage files (*.ml)", "*.ml");
            FileChooser.ExtensionFilter allFilesFilter = new FileChooser.ExtensionFilter(
                    "All files", "*.*");
            fileChooser.getExtensionFilters().addAll(mlFilter, allFilesFilter);
            currentFile = fileChooser.showOpenDialog(stage);
            if (currentFile != null) {
                stage.setTitle(currentFile.getName());
                codeArea.clear();
                try {
                    Reader fileReader = new FileReader(currentFile);
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
            }
        });

        MenuItem save = new MenuItem("Save");
        save.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN));
        save.setOnAction(__ -> {
            if (currentFile == null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save File");
                currentFile = fileChooser.showSaveDialog(stage);
                if (currentFile != null) {
                    stage.setTitle(currentFile.getName());
                }
            }
            if (currentFile != null) {
                try {
                    FileOutputStream writer = new FileOutputStream(currentFile, false);
                    writer.write(codeArea.getText().getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        menuFile.getItems().addAll(open, save);

        menuBar.getMenus().addAll(menuFile);
        menuBar.useSystemMenuBarProperty().setValue(true);

        return menuBar;
    }

    /**
     * Get the error that occurred at the given source location or <code>null</code> if there was no
     * error at this position
     * @param sourceLoc The location at which to check for an error
     * @return The error at this position or <code>null</code>
     */
    private Diagnostics.Error getError(@NotNull SourceLoc sourceLoc) {
        for (Diagnostics.Error error : errorMessages) {
            if (error.getStartLocation().compareTo(sourceLoc) <= 0 &&
                    error.getEndLocation().compareTo(sourceLoc) >= 0) {
                return error;
            }
        }
        return null;
    }

    /**
     * @return The source location of the current caret position
     */
    @NotNull
    private SourceLoc getCurrentSourceLoc() {
        return getSourceLoc(codeArea.getCaretPosition());
    }

    /**
     * @param offset The offset in characters from the start in the source file
     * @return The source location in line and column of the given character offset
     */
    @NotNull
    private SourceLoc getSourceLoc(int offset) {
        TwoDimensional.Position pos = codeArea.offsetToPosition(offset,
                TwoDimensional.Bias.Forward);
        return new SourceLoc(pos.getMajor() + 1, pos.getMinor() + 1);
    }

    private void setUpEventStreams() {
        // Create a stream that is updated with new syntax highlighting information every time
        // the text changes
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

        // Create a stream that evaluates the source code if it wasn't changed for 0.5s
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


        // Create an event source to which errors will be pushed that occur during evaluation
        EventSource<List<Highlighting>> errorsHighlighting = new EventSource<>();

        // Extract the errors from the evaluation result, convert them to highlighting and push
        // the highlighting to the corresponding stream
        evaluationResult
                .map(JavaDriver.EvaluationResult::getErrors)
                .map(IDEHelpers::getErrorHighlighting)
                .subscribe(errorsHighlighting::push);

        // Store the error messages in an instance variable so that it can be used to show the
        // error popups. Also see if the caret position is now at an error and show the popup
        evaluationResult
                .map(JavaDriver.EvaluationResult::getErrors)
                .subscribe(errors -> {
                    this.errorMessages = errors;
                    errorPopupModel.showOrHidePopup(stage, getError(getCurrentSourceLoc()));
                });

        // Update the text in the results area upon program evaluation
        evaluationResult
                .map(JavaDriver.EvaluationResult::getOutput)
                .subscribe(this::updateResultsArea);

        // Every time the source code changes, remove any error highlighting since it may be out
        // of date
        codeArea.plainTextChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
                .subscribe(value -> errorsHighlighting.push(new ArrayList<>(0)));

        // Combine syntax and error highlighting and apply it to the code area every time the syntax
        // highlighting or code highlighting changes
        EventStreams.combine(syntaxHighlighting, errorsHighlighting)
                .mapToTask(this::mergeHighlightsAsync)
                .awaitLatest(codeArea.plainTextChanges())
                .filterMap(task -> {
                    if (task.isSuccess()) {
                        return Optional.of(task.get());
                    } else {
                        return Optional.empty();
                    }
                })
                .subscribe(highlightings -> {
                    SourceLoc lastLoc = new SourceLoc(1, 1);
                    for (Highlighting h : highlightings) {
                        // Remove any previous highlighting for code that doesn't have highlighting
                        // any more
                        if (lastLoc.compareTo(h.getStart()) < 0) {
                            setCodeAreaHighlighting(lastLoc, h.getStart(), Collections.emptySet());
                        }
                        setCodeAreaHighlighting(h.getStart(), h.getEnd(), h.getStyles());
                        lastLoc = h.getEnd();
                    }
                });
    }

    /**
     * Set the text of the result area to display the output of the source code
     * @param output The output of the source code
     */
    private void updateResultsArea(@NotNull Map<SourceLoc, Value> output) {
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
        List<Map.Entry<SourceLoc, Value>> outputs = new ArrayList<>(output.entrySet());
        outputs.sort(Comparator.comparing(Map.Entry::getKey));
        // Fill the lines with the results
        for (Map.Entry<SourceLoc, Value> entry : outputs) {
            int line = entry.getKey().getLine() - 1;
            int length = resultsArea.getParagraph(line).length();
            resultsArea.replaceText(line, length, line, length,
                    entry.getValue().toString());
        }
    }

    /**
     * Set the highlighting in between two source locations in the code area
     * @param from The source location where the highlighting should start
     * @param to The source location where the highlighting should end
     * @param styles The set of styles that should be applied to this code
     */
    private void setCodeAreaHighlighting(@NotNull SourceLoc from, @NotNull SourceLoc to,
                                         @NotNull Collection<String> styles) {
        int fromLine = from.getLine() - 1;
        int toLine = to.getLine() - 1;
        for (int line = fromLine; line <= toLine; line++) {
            int fromColumn = (line == fromLine) ? from.getColumn() - 1 : 0;
            int toColumn;
            if (line == toLine) {
                toColumn =  to.getColumn() - 1;
            } else {
                toColumn = codeArea.getParagraph(line).length();
            }
            codeArea.setStyle(line, fromColumn, toColumn, styles);
        }
    }

    /**
     * Helper method to perform asynchronous tasks on the source code.
     * @param toPerform The function to perform asynchronously on the source code.
     * @return A task representing the asynchronous task
     */
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

    private Task<JavaDriver.EvaluationResult> evaluateCodeAsync() {
        return performOnSourceCode(JavaDriver::evaluate);
    }

    private Task<List<Highlighting>> computeSyntaxHighlightingAsync() {
        return performOnSourceCode(IDEHelpers::computeSyntaxHighlighting);
    }

    /**
     * Merge two lists of code highlightings into a single list asynchronously
     * @param h A pair of the two code highlightings to merge. Both lists are assumed to be sorted
     *          by their start location and code highlighting must be non-overlapping in them
     * @return The merged code highlighting
     */
    private Task<List<Highlighting>> mergeHighlightsAsync(Tuple2<List<Highlighting>,
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