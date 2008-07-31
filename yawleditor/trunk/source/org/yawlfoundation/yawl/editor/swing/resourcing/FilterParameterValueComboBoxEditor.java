package org.yawlfoundation.yawl.editor.swing.resourcing;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.Map;
import java.util.Vector;

/**
 * Author: Michael Adams
 * Creation Date: 29/07/2008
 */
public class FilterParameterValueComboBoxEditor extends AbstractCellEditor
        implements TableCellEditor {

  private static final long serialVersionUID = 1L;

  private JComboBox box;
  private ResourcingInputParamTable table;
  private Map<String, String> contentMap;                                 // [name, id]

  public FilterParameterValueComboBoxEditor(ResourcingInputParamTable table) {
    super();
    this.table = table;
  }

  public Component getTableCellEditorComponent(JTable table, Object value,
          boolean isSelected, int rowIndex, int vColIndex) {

      box.setSelectedItem(value);

      return box;
  }

  public Object getCellEditorValue() {
      return box.getSelectedItem();
  }

  protected void setContents(Map<String, String> content) {
      contentMap = content;
      box = new JComboBox(new Vector(content.keySet()));
  }

  protected String getSelectedID() {
      return contentMap.get((String) getCellEditorValue());
  }

  public boolean stopCellEditing() {
    table.setVariableContentAt(
        table.getSelectionModel().getLeadSelectionIndex(),
        (String) box.getSelectedItem()
    );
    return super.stopCellEditing();
  }
}
