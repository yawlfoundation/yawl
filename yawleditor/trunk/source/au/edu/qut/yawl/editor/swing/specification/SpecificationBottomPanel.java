package au.edu.qut.yawl.editor.swing.specification;

import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import org.jgraph.event.GraphSelectionEvent;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.elements.model.YAWLVertex;
import au.edu.qut.yawl.editor.net.utilities.NetCellUtilities;
import au.edu.qut.yawl.editor.specification.SpecificationSelectionListener;
import au.edu.qut.yawl.editor.specification.SpecificationSelectionSubscriber;
import au.edu.qut.yawl.editor.swing.YAWLEditorDesktop;

public class SpecificationBottomPanel extends JTabbedPane implements SpecificationSelectionSubscriber {

    private static final long serialVersionUID = 1L;

    private static final int DESIGN_NOTES_PAMEL_INDEX = 0;
    private static final int PROBLEM_PANEL_INDEX = 1;

    private DesignNotesPanel designNotesPanel;
    private ProblemMessagePanel problemMessagePanel;
    
    public SpecificationBottomPanel() {
      setBorder(new EmptyBorder(4,5,5,5));

      designNotesPanel = new DesignNotesPanel();
      addTab("Notes", designNotesPanel);

      problemMessagePanel = ProblemMessagePanel.getInstance();
      addTab("Problems", problemMessagePanel);
      
      selectNotesTab();

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
          setEnabledAt(DESIGN_NOTES_PAMEL_INDEX, false);
          setTitleAt(DESIGN_NOTES_PAMEL_INDEX, "Notes");

          YAWLEditor.getInstance().hideBottomOfSplitPane();
          designNotesPanel.setVertex(null);
          break;
        }
        default: {
          
          YAWLVertex vertex = NetCellUtilities.getVertexFromCell(
              YAWLEditorDesktop.getInstance().getSelectedGraph().getSelectionCell()
          );
          
          if (vertex == null) {
            return;
          }
          
          setEnabledAt(DESIGN_NOTES_PAMEL_INDEX, true);
          setTitleAt(
              DESIGN_NOTES_PAMEL_INDEX, 
              "Notes (" + vertex.getEngineId()  + ")"
          );

          designNotesPanel.setVertex(vertex);
          YAWLEditor.getInstance().showNotesTab();
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
