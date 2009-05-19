/*
 * Created on 16/05/2004
 * YAWLEditor v1.03 
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
 
package org.yawlfoundation.yawl.editor.swing.data;

import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.internal.YStringListType;
import org.yawlfoundation.yawl.editor.data.internal.YTimerType;
import org.yawlfoundation.yawl.editor.thirdparty.engine.YAWLEngineProxy;

import java.util.LinkedList;
import java.util.List;

public class JXMLSchemaInstanceEditor extends ValidityEditorPane {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private String variableName;
  private String variableType;
  
  public JXMLSchemaInstanceEditor() {
    super();
    setDocument(new XMLSchemaInstanceStyledDocument(this));
    subscribeForValidityEvents();
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
  
  public void validate() {
    if (this.variableName == null || this.variableType == null) {
      return;
    }
   super.validate();
  }
  
  public String getTypeDefinition() {
    return "<element name=\"" + 
      this.variableName + 
      "\" type=\"" +
      this.variableType + 
      "\"/>";
  }
  
  public String getSchemaInstance() {
    if (DataVariable.isBaseDataType(variableType) || variableType.equals("YTimerType")) {
      return "<" + this.variableName + ">\n" +
             this.getText().trim() + 
             "\n</" + this.variableName + ">";
    } 

    return this.getText();
  }
}

class XMLSchemaInstanceStyledDocument extends  AbstractXMLStyledDocument {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private LinkedList problemList = new LinkedList();
  
  public XMLSchemaInstanceStyledDocument(JXMLSchemaInstanceEditor editor) {
    super(editor);
  }
  
  public void checkValidity() {
    if (getEditor().getText().equals("") || 
        getInstanceEditor().getVariableType() == null) {
      setContentValid(
          AbstractXMLStyledDocument.Validity.VALID
      );
      return;
    }
    String dataType = getInstanceEditor().getVariableType();
    if (dataType.equals("YTimerType")) {
        validateYTimerTypeInstance();
    }
    else if (dataType.equals("YStringListType")) {
        validateYStringListTypeInstance();
    }
    else if (DataVariable.isBaseDataType(dataType)) {
        validateBaseDataTypeInstance();        
    }
    else {
      validateUserSuppliedDataTypeInstance();
    }
  }
  
  private void validateBaseDataTypeInstance() {
    setProblemList(getBaseDataTypeInstanceProblems());
    setValidity();
  }

    private void validateYTimerTypeInstance() {
      setProblemList(getYInternalTypeInstanceProblems("YTimerType"));
      setValidity();
    }

    private void validateYStringListTypeInstance() {
      setProblemList(getYInternalTypeInstanceProblems("YStringListType"));
      setValidity();
    }

    private void setValidity() {
        if (getProblemList().size() == 0) {
          setContentValid(
              AbstractXMLStyledDocument.Validity.VALID
          );
        } else {
          setContentValid(
              AbstractXMLStyledDocument.Validity.INVALID
          );
        }
    }

  public List getProblemList() {
    return problemList;
  }
  
  private LinkedList getBaseDataTypeInstanceProblems() {  
    LinkedList problemList = new LinkedList();
   
    String errors = YAWLEngineProxy.getInstance().validateBaseDataTypeInstance(
        getInstanceEditor().getTypeDefinition(), 
        getInstanceEditor().getSchemaInstance()
    );
    
    if (errors != null && errors.trim().length() > 0) {
      problemList.add(errors);
    }
    
    return problemList;
  }
  
    private LinkedList getYInternalTypeInstanceProblems(String typeName) {
      LinkedList problemList = new LinkedList();
      String varName = getInstanceEditor().getVariableName();
      String validationSchema = "" ;
      if (typeName.equals("YTimerType")) {
          validationSchema = YTimerType.getValidationSchema(varName);
      }
      else if (typeName.equals("YStringListType")) {
          validationSchema = YStringListType.getValidationSchema(varName);
      }

      String errors = YAWLEngineProxy.getInstance().validateBaseDataTypeInstance(
          validationSchema,  getInstanceEditor().getSchemaInstance()
      );

      if (errors != null && errors.trim().length() > 0) {
        problemList.add(errors);
      }

      return problemList;
    }

  private void setProblemList(LinkedList problemList) {
    this.problemList = problemList;
  }

  private void validateUserSuppliedDataTypeInstance() {
    setProblemList(getUserSuppliedDataTypeInstanceProblems());
    setValidity();
  }
  
  private LinkedList getUserSuppliedDataTypeInstanceProblems() {
    LinkedList problemList = new LinkedList();
    
    String errors = YAWLEngineProxy.getInstance().validateUserSuppliedDataTypeInstance(
        getInstanceEditor().getVariableName(), 
        getInstanceEditor().getVariableType(), 
        getInstanceEditor().getSchemaInstance()
    );
    
    if (errors != null && errors.trim().length() > 0) {
      problemList.add(errors);
    }
    
    return problemList;
  }
  
  
  private JXMLSchemaInstanceEditor getInstanceEditor() {
    return (JXMLSchemaInstanceEditor) getEditor();
  }
  
  public void setPreAndPostEditorText(String preEditorText, String postEditorText) {
    // deliberately does nothing.
  }
}
