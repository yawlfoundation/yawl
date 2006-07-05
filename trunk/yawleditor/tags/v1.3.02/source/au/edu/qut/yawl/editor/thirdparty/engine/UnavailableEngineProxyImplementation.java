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

package au.edu.qut.yawl.editor.thirdparty.engine;

import java.util.Set;
import java.util.HashMap;
import java.util.LinkedList;

import au.edu.qut.yawl.editor.data.WebServiceDecomposition;
import au.edu.qut.yawl.editor.swing.AbstractXMLStyledDocument;
import au.edu.qut.yawl.editor.swing.XMLEditorPane;

public class UnavailableEngineProxyImplementation implements
    YAWLEngineProxyInterface {

  public void export() {}

  public void validate() {}

  public void connect() {}

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
  
  public boolean isValidSchema(String schema) {
    return false;
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

  public AbstractXMLStyledDocument getXQueryEditorDocument(XMLEditorPane editor, String extraParseText) {
    return new AlwaysFalseDocument(editor, extraParseText);
  }
  
  public LinkedList getEngineParametersForRegisteredService(String registeredYAWLServiceURI) {
    return null;
  }
}

class AlwaysFalseDocument extends AbstractXMLStyledDocument{
  public AlwaysFalseDocument(XMLEditorPane editor, String extraParseText) {
    super(editor);
  }
    
  public void checkValidity() {
    setValid(false);
  }
  
  public void setPreAndPostEditorText(String preEditorText, String postEditorText) {
    // deliberately does nothing.
  }
}
