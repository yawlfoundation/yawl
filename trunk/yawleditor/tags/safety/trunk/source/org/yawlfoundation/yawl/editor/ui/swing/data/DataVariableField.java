/*
 * Created on 23/01/2005
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2004-5 Queensland University of Technology
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
 *
 */

package org.yawlfoundation.yawl.editor.ui.swing.data;

import org.yawlfoundation.yawl.editor.ui.data.DataVariable;
import org.yawlfoundation.yawl.editor.ui.data.DataVariableSet;
import org.yawlfoundation.yawl.editor.ui.swing.JFormattedAlphaNumericField;

import javax.swing.*;

public class DataVariableField extends JFormattedAlphaNumericField {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private DataVariable variable;
  
  private final VariableVerifier verifier = new VariableVerifier();
  
  public DataVariableField(int columns) {
    super(columns);
    allowXMLNames();
    setInputVerifier(verifier);
  }
  
  public void setVariable(DataVariable variable) {
    this.variable = variable;
    this.setText(variable.getName());
  }
  
  public DataVariable getVariable() {
    return this.variable;
  }
  
  public DataVariableSet getVariableScope() {
    assert this.variable.getScope() != null : "null scope set for variable";
    return this.variable.getScope();
  }
}

class VariableVerifier extends InputVerifier {

  public VariableVerifier() {
    super();
  }

  public boolean verify(JComponent component) {
    assert component instanceof DataVariableField;

    DataVariableField field = (DataVariableField) component;

    if (field.getText().equals(null) || field.getText().equals("")) {
      return false;
    }
    if (field.getText().equals(field.getVariable().getName())) {
      return true;
    }
    if (field.getVariableScope().isValidUserDefinedName(field.getText())) {
      return true;
    }
    
    return false;
  }

  public boolean shouldYieldFocus(JComponent component) {
    boolean isValid = verify(component);
    DataVariableField field = (DataVariableField) component;
    if (!isValid) {
      field.invalidEdit();
    } 
    return isValid;
  }
}


