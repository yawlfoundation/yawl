package org.yawlfoundation.yawl.editor.ui.swing.specification;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 *
 */
class NotesEditor extends JEditorPane {

    private YAWLVertex vertex;

    private static final int MAX_ROW_HEIGHT = 5;


    public NotesEditor() {
        super();

        getDocument().addDocumentListener(
                new DocumentListener() {
                    public void insertUpdate(DocumentEvent e) {
                        updateDesignNotes();
                    }

                    public void changedUpdate(DocumentEvent e) {
                        updateDesignNotes();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        updateDesignNotes();
                    }

                    private void updateDesignNotes() {
                        vertex.setDesignNotes(getText());
                    }
                }
        );
    }

    public void setVertex(YAWLVertex vertex) {
        this.vertex = vertex;
        if (vertex == null) {
            return;
        }
        setText(vertex.getDesignNotes());
    }

    public Dimension getPreferredViewableScrollportSize() {
        Dimension preferredSize = super.getPreferredScrollableViewportSize();

        preferredSize.setSize(
                preferredSize.getWidth(),
                Math.min(
                        preferredSize.getHeight(),
                        getFontMetrics(getFont()).getHeight() * MAX_ROW_HEIGHT
                )
        );

        return preferredSize;
    }
}
