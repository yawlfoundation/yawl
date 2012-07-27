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

package org.yawlfoundation.yawl.editor.ui.swing.data;

import org.yawlfoundation.yawl.editor.ui.data.DataVariable;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractDoneDialog;

public class TaskDataVariableUpdateDialog extends DataVariableUpdateDialog {

  private static final long serialVersionUID = 1L;

  public TaskDataVariableUpdateDialog(AbstractDoneDialog parent) {
    super(parent);
    getVariableValueEditorLabel().setText(
        "Default " +
        getVariableValueEditorLabel().getText()
    );
  }


  protected void enableVariableValueEditorIfAppropriate() {
    if (getUsageComboBox().isEnabled() && getUsageComboBox().getSelectedItem() != null) {
      if (((String)getUsageComboBox().getSelectedItem()).equals(
          DataVariable.usageToString(DataVariable.USAGE_OUTPUT_ONLY))) {
        getVariableValueEditor().setEnabled(true);
      } else {
        getVariableValueEditor().setEnabled(false);
      }
    }
  }

  protected void setEditorValueFromVariable() {
    getVariableValueEditor().setText(
        getVariable().getDefaultValue()
    );
  }

  protected void setVariableValueFromEditorContent() {
    getVariable().setDefaultValue(
        getVariableValueEditor().getText()
    );
  }

  protected int getVariableScope() {
    return DataVariable.SCOPE_TASK;
  }
}
