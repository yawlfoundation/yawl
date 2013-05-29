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
 
package org.yawlfoundation.yawl.editor.ui.swing.data;

import org.yawlfoundation.yawl.editor.core.data.*;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.schema.XSDType;

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
    if (XSDType.getInstance().isBuiltInType(variableType) || isYInternalType(variableType)) {
      return "<" + this.variableName + ">\n" +
             this.getText().trim() + 
             "\n</" + this.variableName + ">";
    } 

    return this.getText();
  }
    
    
    private boolean isYInternalType(String type) {
        for (YInternalType internalType : YInternalType.values()) {
             if (internalType.name().equals(type)) return true;
        }
        return false;
    }
}

class XMLSchemaInstanceStyledDocument extends  AbstractXMLStyledDocument {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private List problemList = new LinkedList();
  
  public XMLSchemaInstanceStyledDocument(JXMLSchemaInstanceEditor editor) {
    super(editor);
  }
  
  public void checkValidity() {
    if (isValidating()) {
      String dataType = getInstanceEditor().getVariableType();
      if (getEditor().getText().equals("") || dataType == null || dataType.equals("string")) {
        setContentValid(AbstractXMLStyledDocument.Validity.VALID);
        return;
      }

        for (YInternalType type : YInternalType.values()) {
            if (type.name().equals(dataType)) {
                setProblemList(getYInternalTypeInstanceProblems(dataType));
               setValidity();
               return;
            }
        }

        validateUserSuppliedDataTypeInstance();
    }
  }
  
  private void validateBaseDataTypeInstance() {
    setProblemList(getBaseDataTypeInstanceProblems());
    setValidity();
  }


    private void setValidity() {
        setContentValid(getProblemList().isEmpty() ?
              AbstractXMLStyledDocument.Validity.VALID :
              AbstractXMLStyledDocument.Validity.INVALID);
    }

  public List getProblemList() {
    return problemList;
  }
  
  private LinkedList getBaseDataTypeInstanceProblems() {  
    LinkedList problemList = new LinkedList();
   
    String errors = SpecificationModel.getInstance().getSchemaValidator().
            validateBaseDataTypeInstance(
        getInstanceEditor().getTypeDefinition(), 
        getInstanceEditor().getSchemaInstance()
    );
    
    if (errors != null && errors.trim().length() > 0) {
      problemList.add(errors);
    }
    
    return problemList;
  }
  
    private List getYInternalTypeInstanceProblems(String typeName) {
      List problemList = new LinkedList();
      String varName = getInstanceEditor().getVariableName();
      String validationSchema = YInternalType.valueOf(typeName).getValidationSchema(varName);

      String errors = SpecificationModel.getInstance().getSchemaValidator().
              validateBaseDataTypeInstance(
          validationSchema,  getInstanceEditor().getSchemaInstance()
      );

      if (errors != null && errors.trim().length() > 0) {
        problemList.add(errors);
      }

      return problemList;
    }

  private void setProblemList(List problemList) {
    this.problemList = problemList;
  }

  private void validateUserSuppliedDataTypeInstance() {
    setProblemList(getUserSuppliedDataTypeInstanceProblems());
    setValidity();
  }
  
  private LinkedList getUserSuppliedDataTypeInstanceProblems() {
    LinkedList problemList = new LinkedList();
    
    String errors = SpecificationModel.getInstance().getSchemaValidator().
            validateUserSuppliedDataTypeInstance(
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
