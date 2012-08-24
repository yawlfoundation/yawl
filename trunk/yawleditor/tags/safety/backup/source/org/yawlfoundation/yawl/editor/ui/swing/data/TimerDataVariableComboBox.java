/*
 * Created on 20/09/2004
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

import org.yawlfoundation.yawl.editor.core.data.YInternalType;
import org.yawlfoundation.yawl.editor.ui.data.DataVariable;

import java.util.Iterator;

/**
 * A Data variable combo-box that shows only variables in the given
 * decomposition scope and usage type that are of the XMLSchema 'duration' tupe.
 * @author bradforl
 *
 */

public class TimerDataVariableComboBox extends DataVariableComboBox {

  private static final long serialVersionUID = 1L;
  
  public TimerDataVariableComboBox() {
    super();
  }
  
  public TimerDataVariableComboBox(int validUsageType) {
    super();
    initialise(validUsageType);
  }
  
  public void setEnabled(boolean enabled) {
    if (enabled && getItemCount() > 0 ) {
      super.setEnabled(enabled);
    } else if (!enabled) {
      super.setEnabled(enabled);
    }
  }
  
  protected void addDataVariables() {
    if (getDecomposition() == null) {
      return;
    }
    
    Iterator variableIterator = getUsageBasedIterator();
    
    while(variableIterator.hasNext()) {
      DataVariable variable = 
        (DataVariable) variableIterator.next();
      if (YInternalType.YTimerType.name().equals(variable.getDataType())) {
        addItem(variable.getName());
      }
    }
  }
  
}
