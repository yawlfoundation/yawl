package org.yawlfoundation.yawl.editor.ui.swing.specification;

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.utilities.NetCellUtilities;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphStateListener;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.GraphState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.swing.YAWLEditorDesktop;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.Arrays;

public class SpecificationBottomPanel extends JTabbedPane implements GraphStateListener {

    private static final int DESIGN_NOTES_PANEL_INDEX = 0;
    private static final int PROBLEM_PANEL_INDEX = 1;

    private DesignNotesPanel designNotesPanel;
    private ProblemMessagePanel problemMessagePanel;
    
    public SpecificationBottomPanel() {
      setBorder(new EmptyBorder(4,5,5,5));

      designNotesPanel = new DesignNotesPanel();
      addTab("Notes", designNotesPanel);

      problemMessagePanel = ProblemMessagePanel.getInstance();
      addTab("Problems", problemMessagePanel);
      
      setEnabledAt(DESIGN_NOTES_PANEL_INDEX, false);
      setSelectedComponent(problemMessagePanel);

        Publisher.getInstance().subscribe(this,
                Arrays.asList(GraphState.NoElementSelected,
                        GraphState.ElementsSelected,
                        GraphState.OneElementSelected));
    }

    public void graphSelectionChange(GraphState state, GraphSelectionEvent event) {
      switch(state) {
        case ElementsSelected:
        case NoElementSelected:{
          setEnabledAt(DESIGN_NOTES_PANEL_INDEX, false);
          setTitleAt(DESIGN_NOTES_PANEL_INDEX, "Notes");

            designNotesPanel.setVertex(null);
            designNotesPanel.setVisible(false);
            setSelectedComponent(problemMessagePanel);
            designNotesPanel.repaint();
          break;
        }
        default: {
          
          YAWLVertex vertex = NetCellUtilities.getVertexFromCell(
              YAWLEditorDesktop.getInstance().getSelectedGraph().getSelectionCell()
          );
          
          if (vertex == null) {
            return;
          }
          
          setEnabledAt(DESIGN_NOTES_PANEL_INDEX, true);
          setTitleAt(
              DESIGN_NOTES_PANEL_INDEX, 
              "Notes (" + vertex.getName()  + ")"
          );

          designNotesPanel.setVertex(vertex);
          YAWLEditor.getInstance().selectNotesTab();
          break;
        }
      }
    }
    
    public void selectNotesTab() {
        setSelectedComponent(designNotesPanel);
        designNotesPanel.setPreferredSize(this.getSize());
    }
    
    public void selectProblemsTab() {
      setSelectedComponent(problemMessagePanel);
      setTitleAt(
          PROBLEM_PANEL_INDEX,
          problemMessagePanel.getTitle()
      );
    }
}
