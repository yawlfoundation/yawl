package au.edu.qut.yawl.editor.swing.specification;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

public class NotesPanel extends JPanel {

  private NotesEditor notesEditor = new NotesEditor();
  
  public NotesPanel() {
    setBorder(
        new CompoundBorder(
            new EmptyBorder(4,5,5,5),
            new EtchedBorder()
        )
    );
    setLayout(new BorderLayout());
    add(notesEditor, BorderLayout.CENTER);
  }
  
}

class NotesEditor extends JEditorPane {
  //TODO: tie into task note model. Make resizing not suck.
}
