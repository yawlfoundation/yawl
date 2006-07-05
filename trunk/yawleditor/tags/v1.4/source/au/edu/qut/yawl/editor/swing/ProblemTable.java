/*
 * Created on 18/11/2005
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

package au.edu.qut.yawl.editor.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

public class ProblemTable extends JSingleSelectTable {
  
  private int previousLargestWidth = 0;
  
  public ProblemTable() {
    super();
    initialise();
  }
  
  private void initialise() {
    setModel(new MessageTableModel());
    this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    setMinimumSize(
        new Dimension(
            (int) getPreferredSize().getWidth(),
            0
        )    
    );
  }
  
  public void addMessage(String message) {
    int currentMessageWidth = getMessageWidth(message);
    
    getMessageModel().addMessage(message);

    if (currentMessageWidth > previousLargestWidth) {
      previousLargestWidth = currentMessageWidth;
      resizeProblemColumnIfNecessary(getWidth());
    }
  }
  
  public void resizeProblemColumnIfNecessary(int outerComponentWidth) {
    getColumnModel().getColumn(0).setMinWidth(
        Math.max(previousLargestWidth, outerComponentWidth));
    getColumnModel().getColumn(0).setMaxWidth(
        Math.max(previousLargestWidth, outerComponentWidth));
    getColumnModel().getColumn(0).setPreferredWidth(
        Math.max(previousLargestWidth, outerComponentWidth));
    setMinimumSize(
      new Dimension(
          (int) getPreferredSize().getWidth(),
          0
      )    
    );
  }
  
  private MessageTableModel getMessageModel() {
    return (MessageTableModel) getModel();
  }
  
  public void reset() {
    setModel(new MessageTableModel());
    previousLargestWidth = 0;
  }
  
  private int getMessageWidth(String message) {
    return getFontMetrics(getFont()).stringWidth(message) + 5;
  }
  
  public int getMessageHeight() {
    return getFontMetrics(getFont()).getHeight();
  }
  
  public Component prepareRenderer(TableCellRenderer renderer,
      int row, 
      int col) {

    JComponent component = (JComponent) super.prepareRenderer(renderer, row, col);
    JLabel componentAsLabel = (JLabel) component;
    componentAsLabel.setHorizontalAlignment(JLabel.LEFT);
    return component;
  }
}

class MessageTableModel extends AbstractTableModel {
  private LinkedList messages = new LinkedList();
  
  private static final String[] COLUMN_LABELS = { 
    "Problem"
  };
  
  public static final int PROBLEM_COLUMN          = 0;

  public int getColumnCount() {
    return COLUMN_LABELS.length;
  }

  public String getColumnName(int columnIndex) {
    return null;
  }
  
  public int getRowCount() {
    if (messages != null) {
      return messages.size();
    }
    return 0;
  }

  public Object getValueAt(int row, int col) {
    switch (col) {
      case PROBLEM_COLUMN:  {
        return messages.get(row);
      }
    }
    return null;
  }

  public void addMessage(String message) {
    messages.add(message);
    this.fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
  }
}