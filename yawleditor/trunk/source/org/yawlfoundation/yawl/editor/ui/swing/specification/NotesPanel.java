package org.yawlfoundation.yawl.editor.ui.swing.specification;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;

public class NotesPanel extends JPanel {

    private NotesEditor editor;

    public NotesPanel() {
        editor = new NotesEditor();
        setBorder(new CompoundBorder(new EmptyBorder(4, 5, 5, 5), new EtchedBorder()));
        setLayout(new BorderLayout());
        add(new JScrollPane(editor), BorderLayout.CENTER);
    }


    public void setVertex(YAWLVertex vertex) {
        editor.setVertex(vertex);
    }
}

