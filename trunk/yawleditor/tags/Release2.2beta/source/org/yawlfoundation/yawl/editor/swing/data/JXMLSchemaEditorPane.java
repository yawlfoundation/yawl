/*
 * Created on 16/05/2004
 * YAWLEditor v1.01
 *
 * @author Lindsay Bradford
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */


package org.yawlfoundation.yawl.editor.swing.data;

import org.yawlfoundation.yawl.editor.swing.menu.DataTypeDialogToolBarMenu;
import org.yawlfoundation.yawl.editor.swing.menu.YAWLToolBarButton;
import org.yawlfoundation.yawl.editor.thirdparty.engine.YAWLEngineProxy;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Highlighter;
import javax.swing.text.PlainDocument;
import java.util.ArrayList;
import java.util.List;

public class JXMLSchemaEditorPane extends JProblemReportingEditorPane {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public JXMLSchemaEditorPane() {
    super(new JXMLSchemaEditor());
    ((JXMLSchemaEditor) getEditor()).setContainingPane(this);
  }

    public JXMLSchemaEditorPane(boolean showLineNumbers) {
      super(new JXMLSchemaEditor(), showLineNumbers);
      ((JXMLSchemaEditor) getEditor()).setContainingPane(this);
    }

}

class JXMLSchemaEditor extends ValidityEditorPane {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private JXMLSchemaEditorPane containingPane;

  public void setContainingPane(JXMLSchemaEditorPane pane) {
      containingPane = pane;
  }

    public JXMLSchemaEditorPane getContainingPane() {
        return containingPane;
    }



  public JXMLSchemaEditor() {
    super();
    setDocument(
        new XMLSchemaStyledDocument(this)
    );
    getDocument().putProperty(PlainDocument.tabSizeAttribute, 2);
    addCaretListener(new SelectionListener());
  }
    

 class XMLSchemaStyledDocument extends AbstractXMLStyledDocument {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public XMLSchemaStyledDocument(ValidityEditorPane editor) {
      super(editor);
    }
    
    public List getProblemList() {
      return YAWLEngineProxy.getInstance().getSchemaValidationResults(
          getEditor().getText()
      );
    }
    
    public void setPreAndPostEditorText(String preEditorText, String postEditorText) {
      // deliberately does nothing.
    }

    public void checkValidity() {      
      if (getEditor().getText().equals("")) {
        setContentValid(AbstractXMLStyledDocument.Validity.VALID);
      }
      else if (isValidating()) {
        List validationResults = getProblemList();
 
        setContentValid(validationResults == null ?
            AbstractXMLStyledDocument.Validity.VALID:
            AbstractXMLStyledDocument.Validity.INVALID
        );
      }
        DataTypeDialogToolBarMenu menu = DataTypeDialogToolBarMenu.getInstance();
        if (menu != null) {
            YAWLToolBarButton formatBtn = menu.getButton("format");
            if (formatBtn != null) formatBtn.setEnabled(isContentValidity());
        }    
    }
  }

  class SelectionListener implements CaretListener {
     public SelectionListener() {
         super();
     }

      public void caretUpdate(CaretEvent e) {
          YAWLToolBarButton btnCut =
                  DataTypeDialogToolBarMenu.getInstance().getButton("cut");
          YAWLToolBarButton btnCopy =
                  DataTypeDialogToolBarMenu.getInstance().getButton("copy");
          int dot = e.getDot();
          int mark = e.getMark();
          boolean selected = (dot != mark);
          if (btnCut != null) btnCut.setEnabled(selected);
          if (btnCopy != null) btnCopy.setEnabled(selected);

          JXMLSchemaEditorPane pane = ((JXMLSchemaEditor) e.getSource()).getContainingPane();
          if (pane.getShowLineNumbers()) {
              pane.setLineNumbers(e.getDot());
          }

          if (selected) {
              List<Highlighter.Highlight> toClear = new ArrayList<Highlighter.Highlight>();
              int start = Math.min(dot, mark);
              int end = Math.max(dot, mark);
              for (Highlighter.Highlight h : pane.getEditor().getHighlighter().getHighlights()) {
                  if (! ((h.getStartOffset() == start) && (h.getEndOffset() == end))) {
                      toClear.add(h);
                  }
              }
              for (Highlighter.Highlight h : toClear) {
                  pane.getEditor().getHighlighter().removeHighlight(h);
              }
          }
          else {
              pane.getEditor().getHighlighter().removeAllHighlights();
          }
      }
  }

}

