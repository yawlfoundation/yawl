/*
 * Created on 13/08/2004
 * YAWLEditor v1.01 
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
package au.edu.qut.yawl.editor.swing.data;

import au.edu.qut.yawl.editor.elements.model.YAWLFlowRelation;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.swing.AbstractOrderedRowTableModel;


public class FlowPriorityTableModel extends AbstractOrderedRowTableModel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final String[] COLUMN_LABELS = { 
      "Target Task",
      "Predicate"
    };

  public static final int TARGET_TASK_COLUMN = 0;
  public static final int PREDICATE_COLUMN   = 1;

  public FlowPriorityTableModel() {
    super();
  }
  
  public FlowPriorityTableModel(YAWLTask task) {
    super();
    assert task.hasSplitDecorator() : "task does not have a split decorator";
    setTask(task);
  }
  
  public void setTask(YAWLTask task) {
    for(Object flow: task.getSplitDecorator().getFlowsInPriorityOrder()) {
      getOrderedRows().add(flow);
    }
    refresh();
  }
  
  public int getColumnCount() {
    return COLUMN_LABELS.length;
  }
  
  public String getColumnName(int columnIndex) {
    return COLUMN_LABELS[columnIndex];
  }
  
  public boolean isCellEditable(int row, int column) {
    return false;
  }
  
  public Class getColumnClass(int columnIndex) {
    return String.class;
  }
  
  public Object getValueAt(int row, int col) {
    switch (col) {
      case TARGET_TASK_COLUMN: {
        return getTaskNameAt(row);
      }
      case PREDICATE_COLUMN: {
        return getPredicateAt(row);
      }
      default: {
        return null;
      }
    }
  }

  public String getTaskNameAt(int row) {
    YAWLFlowRelation flow = getFlowAt(row);
    if (flow == null) {
      return "";
    }
    return flow.getTargetLabel();
  }
  
  public String getPredicateAt(int row) {
    YAWLFlowRelation flow = getFlowAt(row);
    return flow.getPredicate();
  }
  
  public YAWLFlowRelation getFlowAt(int row) {
    if (row < 0 || getOrderedRows() == null || getOrderedRows().size() == 0) {
      return null;
    }
    return (YAWLFlowRelation) getOrderedRows().get(row);
  }
  
  public void raiseRow(int row) {
    YAWLFlowRelation raisingFlow = this.getFlowAt(row);
    YAWLFlowRelation loweringFlow = this.getFlowAt(row - 1);
    
    super.raiseRow(row);    
    
    swapFlowPriorities(raisingFlow, loweringFlow);
  }

  public void lowerRow(int row) {
    YAWLFlowRelation raisingFlow = this.getFlowAt(row + 1);
    YAWLFlowRelation loweringFlow = this.getFlowAt(row);
    
    super.lowerRow(row);    

    swapFlowPriorities(raisingFlow, loweringFlow);
  }
  
  /**
   * A convenience method, meant to realign the underling 
   * flow priority values with any shift of location that may have
   * occured of the flows in their linked list positioning.
   * @param firstRow
   * @param secondRow
   */
  private void swapFlowPriorities(YAWLFlowRelation firstRow, 
                                 YAWLFlowRelation secondRow) {
    
    final int firstRowPriority = firstRow.getPriority();
    final int secondRowPriority = secondRow.getPriority();
    
    firstRow.setPriority(
      secondRowPriority
    );
      
    secondRow.setPriority(
      firstRowPriority
    );
  }
}
