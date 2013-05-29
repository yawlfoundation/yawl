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

package org.yawlfoundation.yawl.editor.ui.swing.resourcing;

import org.yawlfoundation.yawl.editor.ui.resourcing.DataVariableContent;
import org.yawlfoundation.yawl.editor.ui.swing.JOrderedSingleSelectTable;
import org.yawlfoundation.yawl.elements.data.YVariable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

public class ResourcingInputParamTable extends JOrderedSingleSelectTable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final int MAX_ROW_HEIGHT = 5;
  
  private List<DataVariableContent> variableContentList;
  
  public ResourcingInputParamTable() {
    super();
    setModel(new ResourcingInputParamTableModel());
    setFormat();
  }
  
  public ResourcingInputParamTable(ResourcingInputParamTableModel model) {
    super();
    setModel(model);
    setFormat();
  }
  
  public void setFormat() {
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    getColumn("Name").setMinWidth(
        getMaximumNameWidth()
    );
    getColumn("Name").setPreferredWidth(
        getMaximumNameWidth()
    );

//    getColumn("Type").setMinWidth(
//        getMaximumTypeWidth()
//    );
//    getColumn("Type").setPreferredWidth(
//        getMaximumTypeWidth()
//    );
//
//    getColumn("Refers to").setMinWidth(
//        getMessageWidth(
//            DataVariable.usageToString(DataVariable.USAGE_INPUT_AND_OUTPUT)
//         )
//    );
//    getColumn("Refers to").setPreferredWidth(
//        getMessageWidth(
//            DataVariable.usageToString(DataVariable.USAGE_INPUT_AND_OUTPUT)
//         )
//    );

    getColumn("Refers to").setResizable(false);
    
    getColumnModel().getColumn(
       ResourcingInputParamTableModel.CONTAINS_COLUMN    
    ).setCellEditor(new VariableContentsComboBoxEditor(this));
  }
  
  public Dimension getPreferredScrollableViewportSize() {
    Dimension defaultPreferredSize = super.getPreferredSize();
    
    Dimension preferredSize = new Dimension(
        (int) defaultPreferredSize.getWidth(),
        (int) Math.min(
            defaultPreferredSize.getHeight(),
            getFontMetrics(getFont()).getHeight() * MAX_ROW_HEIGHT
        )
    );
    
    return preferredSize;
  }
  
  public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {

    JComponent component = (JComponent) super.prepareRenderer(renderer, row, col);
    
//    if (col == ResourcingInputParamTableModel.TYPE_COLUMN) {
//      ((JLabel) component).setHorizontalAlignment(JLabel.CENTER);
//    }

    return component;
  }

  
  public void setVariableContentList(List<DataVariableContent> variableList) {
    this.variableContentList = variableList;
    updateState();
  }
  
  public void updateState() {
    setModel(new ResourcingInputParamTableModel(variableContentList));
    setFormat();
  }
  
  private int getMessageWidth(String message) {
    return  getFontMetrics(getFont()).stringWidth(message) + 5;
  }

  private int getMaximumNameWidth() {
    int maxWidth = getMessageWidth("Name-");
    for(int i = 0; i < this.getRowCount(); i++) {
      maxWidth = Math.max(maxWidth, getMessageWidth(this.getNameAt(i)));
    }
    return maxWidth;
  }

  private int getMaximumTypeWidth() {
    int maxWidth = getMessageWidth("Type-");
    for(int i = 0; i < this.getRowCount(); i++) {
      maxWidth = Math.max(maxWidth, getMessageWidth(this.getDataTypeAt(i)));
    }
    return maxWidth;
  }
  
  public ResourcingInputParamTableModel getVariableModel() {
    return (ResourcingInputParamTableModel) getModel();
  }

  public YVariable getVariableAt(int row) {
    return getVariableModel().getVariableAt(row);
  }
  
  public String getNameAt(int row) {
    return getVariableModel().getNameAt(row);
  }

  public String getDataTypeAt(int row) {
    return getVariableModel().getDataTypeAt(row);
  }
  
  public void setVariableContentAt(int row, String variableContent) {
    getVariableModel().setVariableContentAt(
        row,
        variableContent
    );
  }
}

/*
 * Story time:  It looks impossible without an immense amount of experimentation
 * to get a JComboBox to flush it's selection of an item into the underlying 
 * table model upon the actual selection event.  The model update will occur
 * only on a stopCellEditing() call, but that only happens whe focus is 
 * passed onto some other widget, outside of the table.  I've tried all kinds of wierd 
 * and funky ways to force a model update once an item selection of the combo box occurs, 
 * but none have worked.  I don't want to waste any more time on this. The editor below 
 * representsthe bare minimum of code required to get the model to be updated once the cell
 * of the JComboBox has stopped being edited.
 */

class VariableContentsComboBoxEditor extends AbstractCellEditor implements TableCellEditor {

  private static final long serialVersionUID = -7572476764558653203L;

  private JComboBox box;
  private ResourcingInputParamTable table;
  
  public VariableContentsComboBoxEditor(ResourcingInputParamTable table) {
    super();
    this.table = table;
    box = buildComponent();
  }
  
  public Component getTableCellEditorComponent(JTable table, Object value,
          boolean isSelected, int rowIndex, int vColIndex) {

      box.setSelectedItem(value);

      return box;
  }

  public Object getCellEditorValue() {
      return box.getSelectedItem();
  }
  
  private JComboBox buildComponent() {
    final JComboBox box = new JComboBox(
        new String[] { "Data", "Participant", "Role" }
    );

    return box;
  }
  
  public boolean stopCellEditing() {
    table.setVariableContentAt(
        table.getSelectionModel().getLeadSelectionIndex(), 
        (String) box.getSelectedItem()
    );
    return super.stopCellEditing();
  }
}
