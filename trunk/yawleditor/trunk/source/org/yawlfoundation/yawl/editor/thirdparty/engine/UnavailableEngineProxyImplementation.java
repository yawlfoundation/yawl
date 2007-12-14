/*
 * Created on 5/11/2004
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

package org.yawlfoundation.yawl.editor.thirdparty.engine;

import java.util.Set;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.yawlfoundation.yawl.editor.data.WebServiceDecomposition;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.swing.data.AbstractXMLStyledDocument;
import org.yawlfoundation.yawl.editor.swing.data.ValidityEditorPane;

public class UnavailableEngineProxyImplementation implements
    YAWLEngineProxyInterface {

  public void engineFormatFileExport(SpecificationModel spec) {}

  public void engineFormatFileImport() {}

  public void validate(SpecificationModel editorSpec) {}

  public void connect() {}
  
  public boolean testConnection(String engineURL, String engineUserID, String engineUserPassword) {
    return false;
  }

  public void disconnect() {}

  public HashMap getRegisteredYAWLServices() {
    HashMap services= new HashMap();
    services.put(
        WebServiceDecomposition.DEFAULT_ENGINE_SERVICE_NAME,
        null
    );
    
    return services;
  }
  
  public String getRegisteredYAWLServiceFromDescription(String description) {
    return null;
  }
  
  public LinkedList getSchemaValidationResults(String schema) {
    return null;
  }

  public void setDataTypeSchema(String schema) {}
  
  public boolean hasValidDataTypeDefinition() {
    return false;
  }
  
  public Set getPrimarySchemaTypeNames() {
    return null;
  }
  
  public String createSchemaForVariable(String variableName, String dataType) {
    return "";
  }
  
  public int getDataTypeComplexity(String dataType) {
    return UNRECOGNISED_DATA_TYPE_COMPLEXITY;
  }

  public String validateBaseDataTypeInstance(String typeDefinition, String SchemeInstance) {
    return "always in error";
  }
  
  public String validateUserSuppliedDataTypeInstance(String variableName, String typeDefinition, String SchemeInstance) {
    return "always in error";
  }

  public AbstractXMLStyledDocument getXQueryEditorDocument(ValidityEditorPane editor, String extraParseText) {
    return new UncertainValidityDocument(editor, extraParseText);
  }
  
  public LinkedList getEngineParametersForRegisteredService(String registeredYAWLServiceURI) {
    return null;
  }
  
  public List getAnalysisResults(SpecificationModel editorSpec) {
    return null;
  }
}

class UncertainValidityDocument extends AbstractXMLStyledDocument{

  private static final long serialVersionUID = 1L;
  private static final List problemList = generateProblemList();
  
  public UncertainValidityDocument(ValidityEditorPane editor, String extraParseText) {
    super(editor);
    
  }
    
  public void checkValidity() {
    setContentValid(
        AbstractXMLStyledDocument.Validity.UNCERTAIN
    );
  }
  
  private static List generateProblemList() {
    LinkedList problemList = new LinkedList();
    problemList.add("No engine libraries are present to determine validity.");
    return problemList;
  }
  
  public void setPreAndPostEditorText(String preEditorText, String postEditorText) {
    // deliberately does nothing.
  }
  
  public List getProblemList() {
    return problemList;
  }
}
