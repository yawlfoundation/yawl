package au.edu.qut.yawl.editor.swing.resourcing;

import java.util.Set;

import javax.swing.JComboBox;

import au.edu.qut.yawl.editor.elements.model.YAWLAtomicTask;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.elements.model.YAWLVertex;
import au.edu.qut.yawl.editor.net.utilities.NetUtilities;

public class FamiliarTaskComboBox extends JComboBox {
  private static final long serialVersionUID = 1L;

  private YAWLAtomicTask task;
  private Set<YAWLAtomicTask> familiarTasks;

  public void setTask(YAWLAtomicTask task) {
    this.task = task;
    this.familiarTasks = NetUtilities.getPreccedingAtomicTasksOf((YAWLVertex) getTask());
    generateFamiliarTaskItems();
  }
  
  public YAWLAtomicTask getTask() {
    return this.task;
  }
  
  private void generateFamiliarTaskItems() {
    removeAllItems();
    
    //TODO: Tighten up to be just preceeding atomic tasks with resourcing requirements.
    
    for(YAWLAtomicTask task : familiarTasks) {
      addItem(((YAWLTask) task).getEngineId());      
    }
  }
  
  public int getFamiliarTaskNumber() {
    return familiarTasks == null ? 0 : familiarTasks.size();
  }
  
  public YAWLAtomicTask getSelectedFamiliarTask() {
    for(YAWLAtomicTask task : familiarTasks) {
      if(((YAWLTask) task).getEngineId().equals(getSelectedItem())) {
        return task;
      }
    }
    return null;
  }
  
  public void setSelectedFamiliarTask(YAWLAtomicTask task) {
    for(int i = 0; i < getItemCount(); i++) {
      if (getItemAt(i).equals(((YAWLTask) task).getEngineId())) {
        setSelectedIndex(i);
      }
    }
  }
}
