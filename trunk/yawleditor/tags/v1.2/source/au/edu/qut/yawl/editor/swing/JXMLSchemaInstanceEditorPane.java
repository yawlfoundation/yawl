/*
 * Created on 16/05/2004
 * YAWLEditor v1.03 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.thirdparty.engine.YAWLEngineProxy;

public class JXMLSchemaInstanceEditorPane extends XMLEditorPane {

  private String variableName;
  private String variableType;
  
  public JXMLSchemaInstanceEditorPane() {
    super();
    this.setDocument(new XMLSchemaInstanceStyledDocument(this));
  }
  
  public String getVariableType() {
    return this.variableType;
  }
  
  public String getVariableName() {
    return this.variableName;
  }
  
  public void setVariableType(String variableType) {
    this.variableType = variableType;
    validate();
  }
  
  public void setVariableName(String variableName) {
    this.variableName = variableName;
    validate();
  }
  
  public String getTypeDefinition() {
    return "<element name=\"" + 
      this.variableName + 
      "\" type=\"" +
      this.variableType.toLowerCase() + "\"/>";
  }
  
  public String getSchemaInstance() {
    if (DataVariable.isSimpleDataType(variableType)) {
      return "<" + this.variableName + ">\n" +
             this.getText().trim() + 
             "\n</" + this.variableName + ">";
    } 
    return this.getText();
  }
}

class XMLSchemaInstanceStyledDocument extends  AbstractXMLStyledDocument {
  
  public XMLSchemaInstanceStyledDocument(JXMLSchemaInstanceEditorPane editor) {
    super(editor);
  }
  
  public void checkValidity() {
    if (getEditor().getText().equals("") || 
        getInstanceEditor().getVariableType() == null) {
      setValid(true);
      return;
    }

    if (DataVariable.isSimpleDataType(getInstanceEditor().getVariableType())) {
      validateSimpleInstance();        
    } else {
      validateComplexInstance();
    }
  }

  private void validateSimpleInstance() {
    try {
      String errors = YAWLEngineProxy.getInstance().validateSimpleSchemaInstance(
        getInstanceEditor().getTypeDefinition(), 
        getInstanceEditor().getSchemaInstance()
      );
      if (errors.equals("")) {
        setValid(true);
      } else {
        setValid(false);
      }
    } catch (Exception e) {
      setValid(false);
    } 
  }
  
  private void validateComplexInstance() {
    try {
      String errors = YAWLEngineProxy.getInstance().validateComplexSchemaInstance(
        getInstanceEditor().getVariableName(), 
        getInstanceEditor().getVariableType(), 
        getInstanceEditor().getSchemaInstance()
      );
      if (errors.equals("")) {
        setValid(true);
      } else {
        setValid(false);
      }
    } catch (Exception e) {
      setValid(false);
    } 
  }
  
  private JXMLSchemaInstanceEditorPane getInstanceEditor() {
    return (JXMLSchemaInstanceEditorPane) getEditor();
  }
}
