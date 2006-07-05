/*
 * Created on 5/11/2004
 * YAWLEditor v1.01
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

package au.edu.qut.yawl.editor.thirdparty.engine;

import java.util.Set;
import java.util.HashMap;

import au.edu.qut.yawl.editor.swing.AbstractXMLStyledDocument;
import au.edu.qut.yawl.editor.swing.XMLEditorPane;

public interface YAWLEngineProxyInterface {
  
  public static final String DEFAULT_ENGINE_URI = 
    "http://localhost:8080/yawl/ia";
  
  public static final String DEFAULT_ENGINE_ADMIN_USER = "admin";
  public static final String DEFAULT_ENGINE_ADMIN_PASSWORD = "YAWL";

  public void export();
  
  public void validate(); 
  
  public void connect();
  
  public HashMap getRegisteredYAWLServices();

  public boolean isValidSchema(String schema);
  
  public void setDataTypeSchema(String schema);
  
  public boolean hasValidDataTypeDefinition();
  
  public Set getPrimarySchemaTypeNames();
  
  public String createSchemaForVariable(String variableName, String dataType);
  
  public String validateSimpleSchemaInstance(String typeDefinition, String schemeInstance);
  
  public String validateComplexSchemaInstance(String variableName, String typeDefinition, String schemeInstance);
  
  public AbstractXMLStyledDocument getXQueryEditorDocument(XMLEditorPane editor, String extraParseText);
}
