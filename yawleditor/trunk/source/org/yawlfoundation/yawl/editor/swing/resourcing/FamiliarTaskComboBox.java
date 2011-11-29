package org.yawlfoundation.yawl.editor.swing.resourcing;

import org.yawlfoundation.yawl.editor.elements.model.YAWLAtomicTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.net.utilities.NetUtilities;

import javax.swing.*;
import java.util.Set;

public class FamiliarTaskComboBox extends JComboBox {
  private static final long serialVersionUID = 1L;

  private YAWLAtomicTask task;
  private Set<YAWLAtomicTask> familiarTasks;

  public void setTask(YAWLAtomicTask task) {
    this.task = task;
    this.familiarTasks = NetUtilities.getPreceedingResourcingRequiredTasksOf((YAWLVertex) getTask());
    generateFamiliarTaskItems();
  }
  
  public YAWLAtomicTask getTask() {
    return this.task;
  }
  
  private void generateFamiliarTaskItems() {
    removeAllItems();
    
    for(YAWLAtomicTask task : familiarTasks) {
      addItem(((YAWLTask) task).getLabel());      
    }
  }
  
  public int getFamiliarTaskNumber() {
    return familiarTasks == null ? 0 : familiarTasks.size();
  }
  
  public YAWLAtomicTask getSelectedFamiliarTask() {
    for(YAWLAtomicTask task : familiarTasks) {
      if(((YAWLTask) task).getLabel().equals(getSelectedItem())) {
        return task;
      }
    }
    return null;
  }
  
  public void setSelectedFamiliarTask(YAWLAtomicTask task) {
    for(int i = 0; i < getItemCount(); i++) {
      if (getItemAt(i).equals(((YAWLTask) task).getLabel())) {
        setSelectedIndex(i);
      }
    }
  }
}
