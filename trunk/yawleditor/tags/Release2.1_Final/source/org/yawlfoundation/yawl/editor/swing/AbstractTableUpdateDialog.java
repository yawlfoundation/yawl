/*
 * Created on 11/06/2004
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

package org.yawlfoundation.yawl.editor.swing;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.yawlfoundation.yawl.editor.swing.AbstractTableUpdatePanel;

public abstract class AbstractTableUpdateDialog extends AbstractDoneDialog {
  
  private AbstractTableUpdatePanel panel;

  public AbstractTableUpdateDialog(String title, boolean modality) {
    super(title, modality);
    setContentPanel(getTablePanel());
  }
  
  private JPanel getTablePanel() {
    final AbstractTableUpdateDialog dialog = this;
    panel = new AbstractTableUpdatePanel(){
      
      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      public JOrderedSingleSelectTable buildTable() {
        return dialog.buildTable();
      }
      
      public void doCreateButtonAction() {
        dialog.doCreateButtonAction();
      }

      public void doUpdateButtonAction() {
        dialog.doUpdateButtonAction();
      }

      public void doRemoveButtonAction() {
        dialog.doRemoveButtonAction();
      }
      
      public int rowLimit() {
        return Integer.MAX_VALUE;
      }
    }; 
    
    panel.setBorder(new EmptyBorder(12,12,0,11));
    return panel;
  }
  
  public JOrderedSingleSelectTable getTable() {
    return panel.getTable();
  }
  
  public void setTable(JOrderedSingleSelectTable table) {
    panel.setTable(table);
  }
  
  public void updateState() {
    panel.updateState();
  }

  public void setState(int state) {
    panel.setState(state);
  }
  
  public boolean hasElements() {
    return panel.hasElements();
  }
  
  protected abstract JOrderedSingleSelectTable buildTable();
  
  protected abstract void doCreateButtonAction();

  protected abstract void doUpdateButtonAction();

  protected abstract void doRemoveButtonAction();
}
