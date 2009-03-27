package org.yawlfoundation.yawl.editor.swing.specification;

import org.jgraph.event.GraphSelectionEvent;
import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.net.utilities.NetCellUtilities;
import org.yawlfoundation.yawl.editor.specification.SpecificationSelectionListener;
import org.yawlfoundation.yawl.editor.specification.SpecificationSelectionSubscriber;
import org.yawlfoundation.yawl.editor.swing.YAWLEditorDesktop;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class SpecificationBottomPanel extends JTabbedPane implements SpecificationSelectionSubscriber {

    private static final long serialVersionUID = 1L;

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

      SpecificationSelectionListener.getInstance().subscribe(
           this,
           new int[] { 
             SpecificationSelectionListener.STATE_NO_ELEMENTS_SELECTED,
             SpecificationSelectionListener.STATE_ONE_OR_MORE_ELEMENTS_SELECTED,
             SpecificationSelectionListener.STATE_SINGLE_ELEMENT_SELECTED,
           }
       );
    }

    public void receiveGraphSelectionNotification(int state, GraphSelectionEvent event) {
      switch(state) {
        case SpecificationSelectionListener.STATE_ONE_OR_MORE_ELEMENTS_SELECTED: 
        case SpecificationSelectionListener.STATE_NO_ELEMENTS_SELECTED:{
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
              "Notes (" + vertex.getEngineLabel()  + ")"
          );

          designNotesPanel.setVertex(vertex);
          YAWLEditor.getInstance().selectNotesTab();
          break;
        }
      }
    }
    
    public void selectNotesTab() {
      setSelectedComponent(designNotesPanel);
    }
    
    public void selectProblemsTab() {
      setSelectedComponent(problemMessagePanel);
      setTitleAt(
          PROBLEM_PANEL_INDEX,
          problemMessagePanel.getTitle()
      );
    }
}
