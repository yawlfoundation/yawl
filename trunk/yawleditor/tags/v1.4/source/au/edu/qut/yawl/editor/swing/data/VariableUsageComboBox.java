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

package au.edu.qut.yawl.editor.swing.data;

import javax.swing.JComboBox;
import au.edu.qut.yawl.editor.data.DataVariable;

public class VariableUsageComboBox extends JComboBox {
  
  private int scope = DataVariable.SCOPE_NET;

  public VariableUsageComboBox() {
    super();
    setScope(DataVariable.SCOPE_NET);
  }
  
  public void setScope(int scope) {
    this.scope = scope;
    reset();
  }
  
  private void reset() {
    this.removeAllItems();
    addItems();
  }
  
  private void addItems() {
    addItem(DataVariable.usageToString(DataVariable.USAGE_INPUT_AND_OUTPUT));
    addItem(DataVariable.usageToString(DataVariable.USAGE_INPUT_ONLY));
    addItem(DataVariable.usageToString(DataVariable.USAGE_OUTPUT_ONLY));
    
    if (scope == DataVariable.SCOPE_NET) {
      addItem(DataVariable.usageToString(DataVariable.USAGE_LOCAL));
    }
  }
}
