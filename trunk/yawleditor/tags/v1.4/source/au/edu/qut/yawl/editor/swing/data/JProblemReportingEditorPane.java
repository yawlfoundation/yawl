/*
 * Created on 15/02/2006
 * YAWLEditor v1.4
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
 
package au.edu.qut.yawl.editor.swing.data;

import java.util.List;

import java.awt.Dimension;
import java.awt.BorderLayout;

import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JPanel;

import au.edu.qut.yawl.editor.swing.ProblemTable;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class JProblemReportingEditorPane extends JSplitPane 
                                         implements AbstractXMLStyledDocumentValidityListener{
  private XMLEditorPane editor;

  private JPanel  editorScrollPanel;
  private JPanel  problemlPanel;

  private JScrollPane  editorScrollPane;
  private JScrollPane  problemScrollPane;
  private ProblemTable parseProblemsTable = new ProblemTable();
  
  
  public JProblemReportingEditorPane(XMLEditorPane editor) {
    super(JSplitPane.VERTICAL_SPLIT);
    setEditor(editor);    
    initialise();
  }
  
  private void initialise() {
    editorScrollPanel = buildEditorPanel();
    problemlPanel = buildProblemPanel();
    
    setTopComponent(editorScrollPanel);
    setBottomComponent(problemlPanel);
    
    setDividerSize(10);
    setOneTouchExpandable(true);
    setContinuousLayout(false);

    addTableResizeListener();
  }
  
  private JPanel buildEditorPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    editorScrollPane = new JScrollPane(editor);
    panel.add(editorScrollPane, BorderLayout.CENTER);
    panel.setMinimumSize(
        new Dimension(0,0)    
    );
    return panel;
  }

  private JPanel buildProblemPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    problemScrollPane = new JScrollPane(parseProblemsTable);
    panel.add(problemScrollPane, BorderLayout.CENTER);
    panel.setMinimumSize(
        new Dimension(0,0)    
    );
    return panel;
  }
  
  
  private void setEditor(XMLEditorPane editor) {
    this.editor = editor;
    this.editor.acceptValiditySubscription(this);
  }
  
  public XMLEditorPane getEditor() {
    return this.editor;
  }
  
  public String getText() {
    return editor.getText();
  }
  
  public void setText(String text) {
    editor.setText(text);
    if (editor.isContentValid()) {
      setDividerLocation((double)1);
    }
  }
  
  public boolean isContentValid() {
    return editor.isContentValid();
  }
  
  private void addTableResizeListener() {
    problemScrollPane.addComponentListener(
        new ComponentAdapter() {
          public void componentMoved(ComponentEvent event) {
            // don't care
          }
          
          public void componentResized(ComponentEvent event) {
            parseProblemsTable.resizeProblemColumnIfNecessary(
                (int) problemScrollPane.getViewport().getVisibleRect().getWidth()
            );
          } 
        }
    );
  }
  
  protected void populateProblemListTable(List problemList) {
    parseProblemsTable.reset();
    
    if (problemList != null && problemList.size() > 0) {
      for(int i = 0; i < problemList.size(); i++) {
        String problem = (String) problemList.get(i);
        if (problem != null && problem.trim().length() > 0) {
          parseProblemsTable.addMessage(problem);
        }
      }
    } 
    setDividerLocationBasedOnTableWidth();
  }
  
  
  private void setDividerLocationBasedOnTableWidth() {
    if (getHeight()  == 0) {
      return;
    }
    if (parseProblemsTable.getPreferredSize().getWidth() > getWidth()) {
      setDividerLocation(
          getHeight() - (parseProblemsTable.getRowHeight() * 3)
         );
    } else {
      setDividerLocation(
          getHeight() - (parseProblemsTable.getRowHeight() * 2)
       );
    }
  }
  
  public void documentValidityChanged(boolean documentValid) {
    if (editor.getText().equals("") && documentValid) {
      setDividerLocation((double)1);
      return;
    }
    
    if (documentValid) {
      populateProblemListTable(null);
      setDividerLocation((double)1);
    } else {
      populateProblemListTable(null);
      populateProblemListTable(
          editor.getProblemList()
      );
      setDividerLocationBasedOnTableWidth();
    }
  }
  
}


