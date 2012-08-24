package org.yawlfoundation.yawl.editor.ui.swing.specification;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class DesignNotesPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private DesignNotesEditor editor = new DesignNotesEditor();
  
  public DesignNotesPanel() {
    setBorder(
        new CompoundBorder(
            new EmptyBorder(4,5,5,5),
            new EtchedBorder()
        )
    );
    setLayout(new BorderLayout());
    add(new JScrollPane(editor), BorderLayout.CENTER);
  }
  
  public void setVertex(YAWLVertex vertex) {
    editor.setVertex(vertex);
  }
}

class DesignNotesEditor extends JEditorPane {
  
  private static final int MAX_ROW_HEIGHT = 5;

  private YAWLVertex vertex;

  public DesignNotesEditor() {
    super();

    final DesignNotesEditor editor = this;
    
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
          vertex.setDesignNotes(editor.getText());
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
