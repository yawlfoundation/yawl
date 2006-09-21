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

import javax.swing.JSplitPane;
import javax.swing.JScrollPane;

import au.edu.qut.yawl.editor.swing.ProblemTable;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class JProblemReportingEditorPane extends JSplitPane 
                                         implements AbstractXMLStyledDocumentValidityListener{
  private static final long serialVersionUID = 1L;

  private ValidityEditorPane editor;

  private JScrollPane  problemScrollPane;
  private ProblemTable parseProblemsTable = new ProblemTable();
  
  
  public JProblemReportingEditorPane(ValidityEditorPane editor) {
    super(JSplitPane.VERTICAL_SPLIT);
    setEditor(editor);    
    initialise();
  }
  
  private void initialise() {
    setTopComponent(buildEditorPanel());
    setBottomComponent(buildProblemPanel());
    
    setDividerSize(10);
    setOneTouchExpandable(true);
    setContinuousLayout(false);
    setResizeWeight(1);

  }
  
  private JScrollPane buildEditorPanel() {
    return new JScrollPane(editor);
  }

  private JScrollPane buildProblemPanel() {
    problemScrollPane = new JScrollPane(parseProblemsTable);
    return problemScrollPane;
  }
  
  private void setEditor(ValidityEditorPane editor) {
    this.editor = editor;
    this.editor.acceptValiditySubscription(this);
  }
  
  public ValidityEditorPane getEditor() {
    return this.editor;
  }
  
  public String getText() {
    return editor.getText();
  }
  
  public void setText(String text) {
    editor.setText(text);
    if (editor.isContentValid()) {
      hideProblemTable();
    }
  }
  
  public boolean isContentValid() {
    return editor.isContentValid();
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
  
  public void hideProblemTable() {
    setDividerLocation((double)1);
  }
  
  public void refreshDividerLocation() {
    setDividerLocationBasedOnTableWidth();
  }
  
  private void setDividerLocationBasedOnTableWidth() {
    if (getHeight()  == 0) {
      return;
    }
    if (parseProblemsTable.getRowCount() == 0) {
      hideProblemTable();
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
      hideProblemTable();
      return;
    }
    
    if (documentValid) {
      populateProblemListTable(null);
       hideProblemTable();
    } else {
      populateProblemListTable(null);
      populateProblemListTable(
          editor.getProblemList()
      );
      setDividerLocationBasedOnTableWidth();
    }
  }
  
}


