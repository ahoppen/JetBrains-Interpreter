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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Timer;
import java.util.TimerTask;

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
    private boolean popupHovered;
    private boolean displayedAsHover;

    ErrorPopupModel(@NotNull FixItCallback callback) {
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


        popupContent.addEventHandler(MouseEvent.MOUSE_ENTERED, __ -> popupHovered = true);
        popupContent.addEventHandler(MouseEvent.MOUSE_EXITED, __ -> {
            popupHovered = false;
            if (displayedAsHover) {
                errorPopup.hide();
            }
        });
    }

    @NotNull Popup getErrorPopup() {
        return errorPopup;
    }

    void showOrHidePopup(@NotNull Node parent, @NotNull Point2D pos,
                         @Nullable Diagnostics.Error error) {
        if (error != null) {
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

    void showOrHidePopup(@NotNull Window window,
                         @Nullable Diagnostics.Error error) {
        if (error != null) {
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

    private void hidePopup() {
        if (displayedAsHover) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
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
