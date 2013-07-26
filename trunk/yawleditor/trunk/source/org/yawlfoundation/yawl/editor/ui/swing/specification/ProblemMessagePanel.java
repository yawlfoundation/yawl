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

package org.yawlfoundation.yawl.editor.ui.swing.specification;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.*;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.ProblemListStateListener;
import org.yawlfoundation.yawl.editor.ui.swing.ProblemTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ProblemMessagePanel extends JPanel implements FileStateListener,
        ProblemListStateListener {

  private JScrollPane problemScrollPane;
  private static ProblemTable problemResultsTable = buildProblemMessageTable();
  
  private String title;
  
  public static final ProblemMessagePanel INSTANCE = new ProblemMessagePanel();
  
  public static ProblemMessagePanel getInstance() {
    return INSTANCE;
  }
  
  private ProblemMessagePanel() {
    super();

    buildContent();
      Publisher.getInstance().subscribe((FileStateListener) this);
      Publisher.getInstance().subscribe((ProblemListStateListener) this);
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
    if (! problemList.isEmpty()) {
        for (int i = 0; i < problemList.size(); i++) {
            String problem = (String) problemList.get(i);
             if (problem != null) problemResultsTable.addMessage(problem.trim());
        }
        problemResultsTable.setWidth();
    }
      
  }

  public ProblemTable getProblemResultsTable() {
    return problemResultsTable;
  }
  
  
  private static ProblemTable buildProblemMessageTable() {
    ProblemTable table = new ProblemTable();
    return table;     
  }
  
  public void specificationFileStateChange(FileState state) {
      if (state == FileState.Closed) problemResultsTable.reset();
  }
  
  public String getTitle() {
    return title;
  }
  
  public void contentChange(ProblemListState state) {
    if (state == ProblemListState.Entries) {
        if (isVisible()) {
          repaint();
        }
        else {
          setVisible(true);
        }
        YAWLEditor.getInstance().selectProblemsTab();
    }
  }
}
