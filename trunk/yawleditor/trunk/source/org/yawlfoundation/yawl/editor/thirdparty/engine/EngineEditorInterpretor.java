/*
 * Created on 10/02/2006
 * YAWLEditor v1.4 
 *
 * @author Lindsay Bradford
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
 *
 */

package org.yawlfoundation.yawl.editor.thirdparty.engine;

import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * This is an abstract class, supplying a base environment for concrete subclasses that must
 * interpret engine objects to editor objects or vica versa.
 * @see EngineSpecificationExporter
 * @see EngineSpecificationImporter
 * @author Lindsay Bradford
 */

public abstract class EngineEditorInterpretor {

  protected static HashMap editorToEngineElementMap;
  protected static HashMap editorToEngineNetMap;
  protected static HashMap editorFlowEngineConditionMap;
  protected static boolean SpecificationParametersIncludeYTimerType ;
  protected static boolean SpecificationParametersIncludeYStringListType;
  protected static boolean SpecificationParametersIncludeYDocumentType;
  protected static final String XML_SCHEMA_URI = "http://www.w3.org/2001/XMLSchema";

  protected static final String ENGINE_RESOURCE_ALLOCATION_PARAMETER = "YawlResourceAllocationQuery";

  protected static final String ENGINE_RESOURCE_AUTHORISATION_PARAMETER = "YawlResourceAuthorisationQuery";

  protected static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMdd");
  
  public static void initialise() {
    editorToEngineElementMap = new HashMap();
    editorToEngineNetMap = new HashMap();
    editorFlowEngineConditionMap = new HashMap();
    SpecificationParametersIncludeYTimerType = false;
    SpecificationParametersIncludeYStringListType = false;    
    SpecificationParametersIncludeYDocumentType = false;
  }

  public static void reset() { initialise(); }
}
