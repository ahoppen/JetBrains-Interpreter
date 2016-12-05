package ide;

import backend.errorHandling.Diagnostics;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import org.fxmisc.richtext.CodeArea;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Model encapsulating logic of when and where to display the error popup
 */
class ErrorPopupModel {

    @FunctionalInterface
    interface FixItCallback {
        void insertText(int line, int column, @NotNull String toInsert);
    }

    @NotNull private final Popup errorPopup;
    @NotNull private final Button applyFixItButton;
    @NotNull private final HBox popupContent;
    @Nullable private Diagnostics.Error.FixItInsert currentFixIt = null;
    @NotNull private final Label errorMessageLabel;
    @Nullable private Timer hideTimer;
    private boolean popupHovered;
    private boolean displayedAsHover;

    /**
     * @param callback The callback to be called to apply a fix-it
     */
    ErrorPopupModel(@NotNull CodeArea codeArea, @NotNull FixItCallback callback) {
        errorMessageLabel = new Label();
        errorMessageLabel.setId("errorPopup");

        applyFixItButton = new Button("Fix-It");
        applyFixItButton.setOnAction(e -> {
            assert currentFixIt != null;
            int line = currentFixIt.getLocation().getLine() - 1;
            int column = currentFixIt.getLocation().getColumn() - 1;
            String toInsert = currentFixIt.getToInsert();
            callback.insertText(line, column, toInsert);
        });

        popupContent = new HBox(errorMessageLabel, applyFixItButton);

        errorPopup = new Popup();
        errorPopup.getContent().add(popupContent);

        codeArea.setPopupWindow(errorPopup);

        popupContent.addEventHandler(MouseEvent.MOUSE_ENTERED, __ -> popupHovered = true);
        popupContent.addEventHandler(MouseEvent.MOUSE_EXITED, __ -> {
            popupHovered = false;
            if (displayedAsHover) {
                errorPopup.hide();
            }
        });
    }

    /**
     * Show the error popup under the given cursor position if <code>error</code> is not
     * <code>null</code> or hide the error popup if <code>error</code> is not <code>null</code>
     * @param parent A node onto which the error popup can be attached
     * @param pos The cursor position at which the popup shall be displayed
     * @param error The error to display or <code>null</code> if the popup should be hidden
     */
    void showOrHidePopup(@NotNull Node parent, @NotNull Point2D pos,
                         @Nullable Diagnostics.Error error) {
        if (error != null) {
            if (hideTimer != null) {
                hideTimer.cancel();
            }
            displayedAsHover = true;
            currentFixIt = error.getFixIt();
            applyFixItButton.setVisible(currentFixIt != null);

            errorMessageLabel.setText(error.getMessage());
            errorPopup.show(parent, pos.getX(), pos.getY() + 10);
            popupContent.requestFocus();
        } else {
            hidePopup();
        }
    }

    /**
     * Show the error popup at the current cursor position if <code>error</code> is not
     * <code>null</code> or hide the error popup if <code>error</code> is not <code>null</code>
     * @param window The window to which the popup can be attached
     * @param error The error to display or <code>null</code> if the popup should be hidden
     */
    void showOrHidePopup(@NotNull Window window,
                         @Nullable Diagnostics.Error error) {
        if (error != null) {
            if (hideTimer != null) {
                hideTimer.cancel();
            }
            displayedAsHover = false;
            currentFixIt = error.getFixIt();
            applyFixItButton.setVisible(currentFixIt != null);

            errorMessageLabel.setText(error.getMessage());
            errorPopup.show(window);
            popupContent.requestFocus();
        } else {
            hidePopup();
        }
    }

    /**
     * Hide the popup
     */
    private void hidePopup() {
        if (displayedAsHover) {
            // If popup is displayed as a hover, only dismiss it after some time to give the user
            // the opportunity to hover over the popup so that he can press the fix-it button
            if (hideTimer != null) {
                hideTimer.cancel();
            }
            hideTimer = new Timer();
            hideTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!popupHovered) {
                        Platform.runLater(errorPopup::hide);
                    }
                }
            }, 500);
        } else {
            errorPopup.hide();
        }
    }
}
