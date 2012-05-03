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

package org.yawlfoundation.yawl.editor.thirdparty.engine;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.swing.data.AbstractXMLStyledDocument;
import org.yawlfoundation.yawl.editor.swing.data.ValidityEditorPane;
import org.yawlfoundation.yawl.editor.swing.specification.ProblemMessagePanel;
import org.yawlfoundation.yawl.editor.thirdparty.wofyawl.WofYAWLProxy;

import java.util.*;

public class YAWLEngineProxy implements YAWLEngineProxyInterface {
  
   private transient static final YAWLEngineProxy INSTANCE
    = new YAWLEngineProxy();

  private YAWLEngineProxyInterface implementation;

  public static YAWLEngineProxy getInstance() {
    return INSTANCE; 
  }

    private YAWLEngineProxy() {
        setImplementation(prefs.get("engineURI", DEFAULT_ENGINE_URI));
    }

    public YAWLEngineProxyInterface getImplementation() {
        return implementation;
    }

    public void setImplementation(YAWLEngineProxyInterface impl) {
        implementation = impl;
    }

    public void setImplementation(String engineURI) {
        if (engineLibrariesAvailable()) {
            try {
                if (ServerLookup.isReachable(engineURI)) {
                    implementation = new ConnectableEngineProxyImplementation();
                }
            }
            catch (Exception e) {
                implementation = new AvailableEngineProxyImplementation();
            }
        }
        else {
            implementation = new UnavailableEngineProxyImplementation();
        }
    }
  
  public boolean connect() {
    return implementation.connect();
  }
  
  public boolean testConnection(String engineURL, String engineUserID, String engineUserPassword) {
    return implementation.testConnection(engineURL, engineUserID, engineUserPassword);
  }
  
  public void disconnect() {
    implementation.disconnect();
  }

    public boolean isConnectable() {
      return (implementation.isConnectable());
    }

  
  public void engineFormatFileExport(SpecificationModel editorSpec) {
    implementation.engineFormatFileExport(editorSpec);
  }

  public void engineFormatFileImport() {
    implementation.engineFormatFileImport();
  }

  public void engineFormatFileImport(String fileName) {
    implementation.engineFormatFileImport(fileName);
  }

  public void validate(SpecificationModel specification) {
    implementation.validate(specification);
  }
  
  public HashMap getRegisteredYAWLServices() {
    return implementation.getRegisteredYAWLServices();
  }

    public Map<String, String> getExternalDataGateways() {
        return implementation.getExternalDataGateways();
    }

  
  public LinkedList getSchemaValidationResults(String schema) {
    return implementation.getSchemaValidationResults(schema);
  }
  
  public void setDataTypeSchema(String schema) {
    implementation.setDataTypeSchema(schema);
  }

  public String getDataTypeSchema() {
    return implementation.getDataTypeSchema();
  }

  public boolean isDefinedTypeName(String name) {
    return implementation.isDefinedTypeName(name);
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
      Class.forName("org.yawlfoundation.yawl.elements.YSpecification");
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

    public List getAnalysisResults(SpecificationModel editorSpec) {
        LinkedList<String> results = new LinkedList<String>();
        try {
            results.addAll(implementation.getAnalysisResults(editorSpec));
            if (WofYAWLProxy.wofYawlAvailable()) {
                results.addAll(WofYAWLProxy.getInstance().getAnalysisResults(editorSpec));
                if (results.size() == 0) {
                    results.add("No problems were discovered in the analysis of this specification.");
                }
            }
        } catch (Exception e) {
            results.add(e.getMessage());
        }
        catch (Throwable e) {
            results.add(e.getMessage());
        }
        return results;
    }
  
  public void analyse(SpecificationModel editorSpec) {
    YAWLEditor.getInstance().progressStatusBarOverSeconds(2);
    

    ProblemMessagePanel.getInstance().setProblemList(
      "Specification Analysis Problems",
      getAnalysisResults(editorSpec) 
    );
    
    YAWLEditor.getInstance().resetStatusBarProgress();
  }
}
