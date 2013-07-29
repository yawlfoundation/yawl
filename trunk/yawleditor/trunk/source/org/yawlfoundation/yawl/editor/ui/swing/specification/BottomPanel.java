package org.yawlfoundation.yawl.editor.ui.swing.specification;

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetCellUtilities;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphStateListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.Arrays;
import java.util.List;

public class BottomPanel extends JTabbedPane implements GraphStateListener {

    private static final int NOTES_PANEL_INDEX = 0;
    private static final int PROBLEM_PANEL_INDEX = 1;

    private NotesPanel notesPanel;
    private ProblemPanel problemPanel;


    public BottomPanel() {
        setBorder(new EmptyBorder(4,5,5,5));

        notesPanel = new NotesPanel();
        addTab("Notes", notesPanel);

        problemPanel = new ProblemPanel(this);
        addTab("Problems", problemPanel);

        setEnabledAt(NOTES_PANEL_INDEX, false);
        setSelectedComponent(problemPanel);

        Publisher.getInstance().subscribe(this,
                Arrays.asList(GraphState.NoElementSelected,
                        GraphState.ElementsSelected,
                        GraphState.OneElementSelected));
    }

    public void graphSelectionChange(GraphState state, GraphSelectionEvent event) {
        switch(state) {
            case ElementsSelected:
            case NoElementSelected: {
                setEnabledAt(NOTES_PANEL_INDEX, false);
                setTitleAt(NOTES_PANEL_INDEX, "Notes");
                notesPanel.setVertex(null);
                notesPanel.setVisible(false);
                setSelectedComponent(problemPanel);
                notesPanel.repaint();
                break;
            }
            default: {

                YAWLVertex vertex = NetCellUtilities.getVertexFromCell(
                        YAWLEditor.getNetsPane().getSelectedGraph().getSelectionCell()
                );

                if (vertex == null) {
                    return;
                }

                setEnabledAt(NOTES_PANEL_INDEX, true);
                setTitleAt(
                        NOTES_PANEL_INDEX,
                        "Notes (" + vertex.getName()  + ")"
                );

                notesPanel.setVertex(vertex);
                selectNotesTab();
                break;
            }
        }
    }

    public void selectNotesTab() {
        setSelectedComponent(notesPanel);
        notesPanel.setPreferredSize(this.getSize());
    }

    public void selectProblemsTab() {
        setSelectedComponent(problemPanel);
        setTitleAt(PROBLEM_PANEL_INDEX, problemPanel.getTitle());
    }

    public void setProblemList(String title, List<String> problems) {
        problemPanel.setProblemList(title, problems);
    }
}
