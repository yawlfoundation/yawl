package au.edu.qut.yawl.editor.swing.specification;

import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import org.jgraph.event.GraphSelectionEvent;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.specification.SpecificationSelectionListener;
import au.edu.qut.yawl.editor.specification.SpecificationSelectionSubscriber;

public class SpecificationBottomPanel extends JTabbedPane implements SpecificationSelectionSubscriber {

    private static final long serialVersionUID = 1L;

    private ProblemMessagePanel problemMessagePanel;
    private NotesPanel notesPanel;
    
    public SpecificationBottomPanel() {
      setBorder(new EmptyBorder(4,5,5,5));

      problemMessagePanel = ProblemMessagePanel.getInstance();
      
      notesPanel = new NotesPanel();
      
      addTab("Notes", notesPanel);
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
          setEnabledAt(0, false);
          YAWLEditor.getInstance().hideBottomOfSplitPane();
          break;
        }
        default: {
          setEnabledAt(0, true);
          YAWLEditor.getInstance().showNotesTab();
          break;
        }
      }
    }
    
    public void selectNotesTab() {
      setSelectedComponent(notesPanel);
    }
    
    public void selectProblemsTab() {
      setSelectedComponent(problemMessagePanel);
    }
}
