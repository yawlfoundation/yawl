/*
 * Created on 28/09/2004
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

package au.edu.qut.yawl.editor.thirdparty.engine;

import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.LinkedList;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.swing.data.AbstractXMLStyledDocument;
import au.edu.qut.yawl.editor.swing.data.ValidityEditorPane;

import au.edu.qut.yawl.editor.swing.specification.ProblemMessagePanel;

import au.edu.qut.yawl.editor.thirdparty.wofyawl.WofYAWLProxy;

public class YAWLEngineProxy implements YAWLEngineProxyInterface {
  
  private transient static final YAWLEngineProxy INSTANCE 
    = new YAWLEngineProxy();

  private YAWLEngineProxyInterface implementation;

  public static YAWLEngineProxy getInstance() {
    return INSTANCE; 
  }
    
  private YAWLEngineProxy() {
    if (engineLibrariesAvailable()) {
      implementation = new AvailableEngineProxyImplementation();
    } else {
      implementation = new UnavailableEngineProxyImplementation();
    }
  }
  
  public void connect() {
    implementation.connect();
  }
  
  public boolean testConnection(String engineURL, String engineUserID, String engineUserPassword) {
    return implementation.testConnection(engineURL, engineUserID, engineUserPassword);
  }
  
  public void disconnect() {
    implementation.disconnect();
  }
  
  public void engineFormatFileExport() {
    implementation.engineFormatFileExport();
  }

  public void engineFormatFileImport() {
    implementation.engineFormatFileImport();
  }
  
  public void validate() {
    implementation.validate();
  }
  
  public HashMap getRegisteredYAWLServices() {
    return implementation.getRegisteredYAWLServices();
  }
  
  public LinkedList getSchemaValidationResults(String schema) {
    return implementation.getSchemaValidationResults(schema);
  }
  
  public void setDataTypeSchema(String schema) {
    implementation.setDataTypeSchema(schema);
  }
  
  public boolean hasValidDataTypeDefinition() {
    return implementation.hasValidDataTypeDefinition();
  }
  
  public Set getPrimarySchemaTypeNames() {
    return implementation.getPrimarySchemaTypeNames();
  }
  
  public int getDataTypeComplexity(String dataType) {
    return implementation.getDataTypeComplexity(dataType);
  }
  
  public String createSchemaForVariable(String variableName, String dataType) {
    return implementation.createSchemaForVariable(variableName, dataType);
  }
  
  public String validateBaseDataTypeInstance(String typeDefinition, String schemeInstance) {
    return implementation.validateBaseDataTypeInstance(typeDefinition, schemeInstance);
  }
  
  public String validateUserSuppliedDataTypeInstance(String variableName, String typeDefinition, String schemeInstance) {
    return implementation.validateUserSuppliedDataTypeInstance(variableName, typeDefinition, schemeInstance);
  }
  
  public static boolean engineLibrariesAvailable() {
    // assumption: If we can find YSpecification, we can find everything we
    //             need from the engine libraries.
    try {
      Class.forName("au.edu.qut.yawl.elements.YSpecification");
      return true;
    } catch (Exception e) {
      return false;
    }
  }
  
  public AbstractXMLStyledDocument getXQueryEditorDocument(ValidityEditorPane editor, String extraParseText) {
    return implementation.getXQueryEditorDocument(editor, extraParseText);
  }
  
  public LinkedList getEngineParametersForRegisteredService(String registeredYAWLServiceURI) {
    return implementation.getEngineParametersForRegisteredService(registeredYAWLServiceURI);
  }
  
  public List getAnalysisResults() {
    return implementation.getAnalysisResults();
  }
  
  public void analyse() {
    YAWLEditor.getInstance().progressStatusBarOverSeconds(2);
    
    List analysisResults = getAnalysisResults();
    
    if (WofYAWLProxy.wofYawlAvailable()) {
      
      try {
        analysisResults.addAll(
            WofYAWLProxy.getInstance().getAnalysisResults()  
          );
        if (analysisResults.size() == 0) {
          analysisResults.add("No problems were discovered in the analysis of this specification.");
        }
      } catch (Exception e) {
        LinkedList<String> stackMessageList = new LinkedList<String>();
        stackMessageList.add(e.getMessage());
        
        ProblemMessagePanel.getInstance().setProblemList(
            "Programming Exception with Specification Analysis",
            stackMessageList
        );
      }
    }

    ProblemMessagePanel.getInstance().setProblemList(
      "Specification Analysis Problems",
      analysisResults 
    );
    YAWLEditor.getInstance().resetStatusBarProgress();
  }
}
