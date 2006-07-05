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

import java.util.Set;
import java.util.HashMap;
import java.util.LinkedList;

import au.edu.qut.yawl.editor.swing.AbstractXMLStyledDocument;
import au.edu.qut.yawl.editor.swing.XMLEditorPane;

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
  
  public void disconnect() {
    implementation.disconnect();
  }
  
  public void export() {
    implementation.export();
  }
  
  public void validate() {
    implementation.validate();
  }
  
  public HashMap getRegisteredYAWLServices() {
    return implementation.getRegisteredYAWLServices();
  }
  
  public boolean isValidSchema(String schema) {
    return implementation.isValidSchema(schema);
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
  
  public AbstractXMLStyledDocument getXQueryEditorDocument(XMLEditorPane editor, String extraParseText) {
    return implementation.getXQueryEditorDocument(editor, extraParseText);
  }
  
  public LinkedList getEngineParametersForRegisteredService(String registeredYAWLServiceURI) {
    return implementation.getEngineParametersForRegisteredService(registeredYAWLServiceURI);
  }
}
