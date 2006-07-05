/*
 * Created on 6/08/2004
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

import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.elements.model.YAWLFlowRelation;
import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.swing.JSingleSelectTable;
import au.edu.qut.yawl.editor.swing.data.FlowPredicateUpdateDialog;
import au.edu.qut.yawl.editor.swing.AbstractDoneDialog;

public class FlowPriorityTable extends JSingleSelectTable {
  NetGraph net;
  
  private final FlowPredicateUpdateDialog predicateDialog;

  YAWLFlowRelation oldSelectedFlow;
  
  public FlowPriorityTable(AbstractDoneDialog parent) {    
    super(5);
    predicateDialog = new FlowPredicateUpdateDialog(parent);
    setModel(new FlowPriorityTableModel());
  }
 
  public void setTask(YAWLTask task, NetGraph graph) {
    this.net = graph;
    setModel(new FlowPriorityTableModel(task));
  }
  
  public FlowPriorityTableModel getFlowModel() {
    return (FlowPriorityTableModel) getModel();
  }
  
  public void increasePriorityOfSelectedFlow() {
    getFlowModel().increasePriorityOfFlow(getSelectedRow());
    selectRow(getSelectedRow() - 1);
  }

  public void decreasePriorityOfSelectedFlow() {
    getFlowModel().decreasePriorityOfFlow(getSelectedRow());
    selectRow(getSelectedRow() + 1);
  }
  
  public void updatePredicateOfSelectedFlow() {
    predicateDialog.setFlow(getFlowModel().getFlowAt(getSelectedRow()), net);
    predicateDialog.setVisible(true);
    getFlowModel().fireTableRowsUpdated(getSelectedRow(), getSelectedRow());
  }
}