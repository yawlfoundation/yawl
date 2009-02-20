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

import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.swing.data.AbstractXMLStyledDocument;
import org.yawlfoundation.yawl.editor.swing.data.ValidityEditorPane;
import org.yawlfoundation.yawl.editor.YAWLEditor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

public interface YAWLEngineProxyInterface {
  
    public static final Preferences prefs =
      Preferences.userNodeForPackage(YAWLEditor.class);    

    public static final String DEFAULT_ENGINE_URI =
    "http://localhost:8080/yawl/ia";
  
  public static final String DEFAULT_ENGINE_ADMIN_USER = "admin";
  public static final String DEFAULT_ENGINE_ADMIN_PASSWORD = "YAWL";
  
  public static final int UNRECOGNISED_DATA_TYPE_COMPLEXITY  = 0;
  public static final int SIMPLE_DATA_TYPE_COMPLEXITY        = 1;
  public static final int COMPLEX_DATA_TYPE_COMPLEXITY       = 2;

  public void engineFormatFileExport(SpecificationModel specification);

  public void engineFormatFileImport();

  public void engineFormatFileImport(String fileName);

  public void validate(SpecificationModel specification); 
  
  public void connect();
  
  public boolean testConnection(String engineURL, String engineUserID, String engineUserPassword);

  public void disconnect();

  public boolean isConnectable();
  
  public HashMap getRegisteredYAWLServices();

  public LinkedList getSchemaValidationResults(String schema);
  
  public void setDataTypeSchema(String schema);
  
  public boolean hasValidDataTypeDefinition();
  
  public Set getPrimarySchemaTypeNames();
  
  public int getDataTypeComplexity(String dataType);
  
  public String createSchemaForVariable(String variableName, String dataType);
  
  public String validateBaseDataTypeInstance(String typeDefinition, String schemeInstance);
  
  public String validateUserSuppliedDataTypeInstance(String variableName, String typeDefinition, String schemeInstance);
  
  public AbstractXMLStyledDocument getXQueryEditorDocument(ValidityEditorPane editor, String extraParseText);
  
  public LinkedList getEngineParametersForRegisteredService(String registeredYAWLServiceURI);

  public List getAnalysisResults(SpecificationModel editorSpec);
}
