package org.fxmisc.richtext.model;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

import org.reactfx.EventStream;
import org.reactfx.SuspendableNo;
import org.reactfx.value.Val;

/**
 * Content model for {@link org.fxmisc.richtext.StyledTextArea}. Implements edit operations
 * on styled text, but not worrying about additional aspects such as
 * caret or selection, which are handled by {@link StyledTextAreaModel}.
 */
public interface EditableStyledDocument<PS, S> extends StyledDocument<PS, S> {

    /* ********************************************************************** *
     *                                                                        *
     * Observables                                                            *
     *                                                                        *
     * Observables are "dynamic" (i.e. changing) characteristics of an object.*
     * They are not directly settable by the client code, but change in       *
     * response to user input and/or API actions.                             *
     *                                                                        *
     * ********************************************************************** */

    ObservableValue<String> textProperty();

    int getLength();
    Val<Integer> lengthProperty();

    @Override
    ObservableList<Paragraph<PS, S>> getParagraphs();

    /**
     * Read-only snapshot of the current state of this document.
     */
    ReadOnlyStyledDocument<PS, S> snapshot();

    /* ********************************************************************** *
     *                                                                        *
     * Event streams                                                          *
     *                                                                        *
     * ********************************************************************** */

    default EventStream<PlainTextChange> plainChanges() {
        return richChanges()
                .map(c -> new PlainTextChange(c.position, c.removed.getText(), c.inserted.getText()))
                // filter out rich changes where the style was changed but text wasn't added/removed
                .filter(pc -> !pc.removed.equals(pc.inserted));
    }

    EventStream<RichTextChange<PS, S>> richChanges();

    SuspendableNo beingUpdatedProperty();
    boolean isBeingUpdated();

    /* ********************************************************************** *
     *                                                                        *
     * Actions                                                                *
     *                                                                        *
     * Actions change the state of the object. They typically cause a change  *
     * of one or more observables and/or produce an event.                    *
     *                                                                        *
     * ********************************************************************** */

    void replace(int start, int end, StyledDocument<PS, S> replacement);

    void setStyle(int from, int to, S style);

    void setStyle(int paragraph, S style);

    void setStyle(int paragraph, int fromCol, int toCol, S style);

    void setStyleSpans(int from, StyleSpans<? extends S> styleSpens);

    void setStyleSpans(int paragraph, int from, StyleSpans<? extends S> styleSpens);

    void setParagraphStyle(int parIdx, PS style);

}
