/*
 * Created on 5/02/2005
 * YAWLEditor v1.01-1 
 *
 * @author Lindsay Bradford
 * 
 * 
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

package au.edu.qut.yawl.editor.swing.specification;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.specification.ProblemList;
import au.edu.qut.yawl.editor.specification.ProblemListSubscriber;
import au.edu.qut.yawl.editor.specification.SpecificationFileModel;
import au.edu.qut.yawl.editor.specification.SpecificationFileModelListener;
import au.edu.qut.yawl.editor.swing.ProblemTable;

public class ProblemMessagePanel extends JPanel  implements SpecificationFileModelListener, ProblemListSubscriber {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private JScrollPane problemScrollPane;
  private static ProblemTable problemResultsTable = buildProblemMessageTable();
  
  private String title;
  
  public static final ProblemMessagePanel 
    INSTANCE = new ProblemMessagePanel();
  
  public static ProblemMessagePanel getInstance() {
    return INSTANCE;
  }
  
  private ProblemMessagePanel() {
    super();

    buildContent();
    SpecificationFileModel.getInstance().subscribe(this);
    problemResultsTable.subscribeForProblemListUpdates(this);
  }
  
  private void buildContent() {
    setLayout(new BorderLayout());
    
    problemScrollPane = new JScrollPane(problemResultsTable);
    
    add(problemScrollPane, BorderLayout.CENTER);
    setBorder(new EmptyBorder(4,5,5,5));
  }
  
  
  public void setProblemList(String title, List problemList) {
    problemResultsTable.reset();
    
    this.title = title;

    populateProblemListTable(problemList);
  }
  
  private void populateProblemListTable(List problemList) {
    for(int i = 0; i < problemList.size(); i++) {
      String problem = (String) problemList.get(i);
      
      problemResultsTable.addMessage(problem.trim());
    }
  }

  public ProblemTable getProblemResultsTable() {
    return problemResultsTable;
  }
  
  
  private static ProblemTable buildProblemMessageTable() {
    ProblemTable table = new ProblemTable();
    return table;     
  }
  
  public void specificationFileModelStateChanged(int state) {
    switch(state) {
      case SpecificationFileModel.IDLE: {
        problemResultsTable.reset();
        break;
      }
      default: {
        break;
      }
    }
  }
  
  public String getTitle() {
    return title;
  }
  
  public void problemListUpdated(ProblemList.STATUS status) {
    switch(status) {
      case HAS_ENTRIES: {
        if (isVisible()) {
          repaint();
        } else {
          setVisible(true);
        }
        YAWLEditor.getInstance().indicateProblemsTabActivity();
        break;
      }
      case NO_ENTRIES: {
        break;
      }
    }
  }
}
