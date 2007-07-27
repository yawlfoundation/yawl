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
import java.util.List;

import java.util.Iterator;
import java.util.prefs.Preferences;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.query.QueryParser;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.xpath.XPathException;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.data.WebServiceDecomposition;
import au.edu.qut.yawl.editor.swing.data.AbstractXMLStyledDocument;
import au.edu.qut.yawl.editor.swing.data.ValidityEditorPane;
import au.edu.qut.yawl.engine.interfce.InterfaceA_EnvironmentBasedClient;
import au.edu.qut.yawl.engine.interfce.InterfaceB_EngineBasedClient;
import au.edu.qut.yawl.schema.XMLToolsForYAWL;
import au.edu.qut.yawl.schema.ElementCreationInstruction;
import au.edu.qut.yawl.unmarshal.SchemaForSchemaValidator;

import au.edu.qut.yawl.elements.YAWLServiceReference;
import au.edu.qut.yawl.elements.data.YParameter;

public class AvailableEngineProxyImplementation implements
  YAWLEngineProxyInterface {

  private static final Preferences prefs = 
    Preferences.userNodeForPackage(YAWLEditor.class);
  
  private static final ResetNetAnalysisResultsParser ANALYSIS_RESULTS_PARSER = new ResetNetAnalysisResultsParser();
  
  private static final SchemaForSchemaValidator schemaValidator = SchemaForSchemaValidator.getInstance();
  private static final InstanceSchemaValidator instanceValidator = InstanceSchemaValidator.getInstance();
  
  private String sessionID = "";
  
  private InterfaceA_EnvironmentBasedClient clientInterfaceA;
  
  private XMLToolsForYAWL xmlTools;
  
  public void engineFormatFileExport() {
    EngineSpecificationHandler.getInstance().engineFormatFileExport();
  }
  
  public void engineFormatFileImport() {
    EngineSpecificationHandler.getInstance().engineFormatFileImport();
  }
  
  public void validate() {
    EngineSpecificationHandler.getInstance().validate();
  }
  
  public void connect() {
    try {
      if (!connected()) {
        tryConnect();
      } 
    } catch (Exception e) {
      //e.printStackTrace();
      sessionID = "";
    }
  } 
  
  public boolean testConnection(String engineURL, String engineUserId, String engineUserPassword) {
   String testSessionID = null;
    try {
      testSessionID = tryConnect(engineURL, engineUserId, engineUserPassword);
    } catch (Exception e) {
      testSessionID = null;
    }
    return checkConnectionForSessionID(testSessionID);
  } 
  
  public void disconnect() {
    sessionID = "";
    clientInterfaceA = null;
  }
  
  private String tryConnect(String engineURI, String engineUserID, String engineUserPassword) {
    String sessionID;
    try {
      if (clientInterfaceA == null) {
        clientInterfaceA = new InterfaceA_EnvironmentBasedClient(
          engineURI
        );
      }
      
      sessionID = clientInterfaceA.connect(
          engineUserID,
          engineUserPassword
      );
    } catch (Exception e) {
      // e.printStackTrace();
      return "";
    } 
    
    return sessionID;
    // System.out.println("Connection to engine attempted. ID = \"" + sessionID + "\"");
  }
  
  private void tryConnect() {
    sessionID = tryConnect(
        prefs.get("engineURI", 
            DEFAULT_ENGINE_URI),
        prefs.get("engineUserID", 
            DEFAULT_ENGINE_ADMIN_USER),
        prefs.get("engineUserPassword", 
             DEFAULT_ENGINE_ADMIN_PASSWORD)
    );
  }

  public HashMap getRegisteredYAWLServices() {
    HashMap servicesForEditor = new HashMap();

    servicesForEditor.put(
        WebServiceDecomposition.DEFAULT_ENGINE_SERVICE_NAME,
        null
    );
    
    if (!connected()) {
      connect();
    }

    if (connected()) {
      Set services = clientInterfaceA.getRegisteredYAWLServices(sessionID);
//      System.out.println(services.size() + " services retrieved from engine");
      
      Iterator servicesIterator = services.iterator();
      while(servicesIterator.hasNext()) {
        YAWLServiceReference serviceReference = (YAWLServiceReference) servicesIterator.next();
        
        // Short and sweet description for the editor.
        
        if (serviceReference.getDocumentation().equals(
            "This YAWL Service enables suitably declared workflow tasks to" +
            " invoke RPC style service on the Web.")) {
          serviceReference.setDocumentation("RPC-Style Web Service Invoker");
        }
        
        servicesForEditor.put(serviceReference.getDocumentation(), serviceReference.getURI());
      }
    }
    return servicesForEditor;
  }

  public boolean connected() {
    if (sessionID == null || sessionID.equals("")) {
      return false;
    }
    
    if (checkConnectionForSessionID(sessionID)) {
      return true;
    }
    
   sessionID = "";
   return false;
  }
  
  private boolean checkConnectionForSessionID(String sessionID) {
    String simplePing = clientInterfaceA.checkConnection(sessionID);
    // System.out.println("Checking engine connection. Engine returned \"" + simplePing + "\"");
  
    if (simplePing != null && simplePing.trim().equals("<response>Permission Granted</response>")) {
      return true;
    }
    return false;
  }
  
  public LinkedList getSchemaValidationResults(String schema) {
    try {
      String errors = schemaValidator.validateSchema(schema);

      if (errors == null || errors.trim().equals("")) {
        return null;
      }
      
      String[] errorsAsArray = errors.split("\n");
      LinkedList errorList = new LinkedList();
      
      for(int i = 0; i < errorsAsArray.length; i++) {
        errorList.add(errorsAsArray[i]);
      }
      return errorList;
      
    } catch (Exception e) {} 
    return null;
  }
  
  public void setDataTypeSchema(String schema) {
    // System.out.println("Setting Data Type Schema with:\n-----\n\n" + schema);
    try {
      xmlTools = new XMLToolsForYAWL();
      xmlTools.setPrimarySchema(schema); 
    } catch (Exception e) {
      xmlTools = null;
    }
  }
  
  public boolean hasValidDataTypeDefinition() {
    if (xmlTools != null) {
      return true;
    }
    return false;
  }

  public Set getPrimarySchemaTypeNames() {
    if (hasValidDataTypeDefinition()) {
      return xmlTools.getPrimarySchemaTypeNames();
    }
    return null;
  }
  
  public String createSchemaForVariable(String variableName, String dataType) {
    try {
      String schema = xmlTools.createYAWLSchema(
          new ElementCreationInstruction[] 
            { new ElementCreationInstruction(variableName, dataType, false) },
          "data"
      ); 
      return schema;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  
  public int getDataTypeComplexity(String dataType) {
    int complexity = UNRECOGNISED_DATA_TYPE_COMPLEXITY;
    
    Set schemaTypeNames = getPrimarySchemaTypeNames();
    Iterator i = schemaTypeNames.iterator();
    while(i.hasNext()) {
      String knownTypeName = (String) i.next();
      if (knownTypeName.equals(dataType)) {
        String dataTypeSchema = createSchemaForVariable("testVar", knownTypeName);
        if (dataTypeSchema != null) {
          if (dataTypeSchema.indexOf("simpleType") != -1) {
            complexity = SIMPLE_DATA_TYPE_COMPLEXITY;
          } else {
            complexity = COMPLEX_DATA_TYPE_COMPLEXITY;
          }
        }
      }
    }
    return complexity;
  }
  
  public String validateBaseDataTypeInstance(String typeDefinition, String schemeInstance) {
    return instanceValidator.validateBaseDataTypeInstance(typeDefinition, schemeInstance);
  }
  
  public String validateUserSuppliedDataTypeInstance(String variableName, String typeDefinition, String schemeInstance) {
    return instanceValidator.validateUserSuppliedDataTypeInstance(variableName, typeDefinition, schemeInstance);
  }
  
  public AbstractXMLStyledDocument getXQueryEditorDocument(ValidityEditorPane editor, String extraParseText) {
    return new XQueryStyledDocument(editor);
  }
  
  public LinkedList getEngineParametersForRegisteredService(String registeredYAWLServiceURI) {
    LinkedList dataVariableList = new LinkedList();
    
    YAWLServiceReference registeredService = null;

//   System.out.println("registeredYAWLServiceURI: " + registeredYAWLServiceURI);

    if(!connected()) {
      connect();
    }

    if (connected()) {
      Set services = clientInterfaceA.getRegisteredYAWLServices(sessionID);
//      System.out.println(services.size() + " services retrieved from engine.");
    
      Iterator servicesIterator = services.iterator();
      while(servicesIterator.hasNext()) {
        YAWLServiceReference serviceReference = (YAWLServiceReference) servicesIterator.next();
//        System.out.println(serviceReference.getURI());
        
        if (serviceReference.getURI().equals(registeredYAWLServiceURI)) {
          registeredService = serviceReference;
          continue;
        }
      }
      if (registeredService == null) {
        return null;
      }

      YParameter[] engineParametersForService;
      
      try {
          engineParametersForService = InterfaceB_EngineBasedClient.getRequiredParamsForService(
              registeredService        
          );
//          System.out.println("YParameter number returned from InterfaceB = " + engineParametersForService.length);
      } catch (Exception ioe) {
        return null;
      }
      
      // TODO: What about data-types that the editor is currently unaware of?
      // TODO: What about when the engine is unavailable?

      for(int i = 0; i < engineParametersForService.length; i++) {
        DataVariable editorVariable = new DataVariable();
          
        editorVariable.setDataType(engineParametersForService[i].getDataTypeName());
        editorVariable.setName(engineParametersForService[i].getName());
        editorVariable.setInitialValue(engineParametersForService[i].getInitialValue());
        editorVariable.setUserDefined(false);
 
         if (engineParametersForService[i].isInput()) {
           editorVariable.setUsage(DataVariable.USAGE_INPUT_ONLY);
         }
         if (engineParametersForService[i].isOutput()) {
           editorVariable.setUsage(DataVariable.USAGE_OUTPUT_ONLY);
         }
       
         dataVariableList.add(editorVariable);
      }
      
      // There's gotta be a nicer way to do this -- I'm having a blond day.
      
      Object[] dataVariableArray = dataVariableList.toArray();
      
      for(int i = 0; i < dataVariableArray.length; i++) {
        for(int j = 0; j < dataVariableArray.length; j++) {
          DataVariable iVariable = (DataVariable) dataVariableArray[i];
          DataVariable jVariable = (DataVariable) dataVariableArray[j];
          
          if (i != j && iVariable.getName() != null && jVariable.getName()!= null && 
              iVariable.getName().equals(jVariable.getName())) {
            
//            System.out.println(iVariable.toString());
//            System.out.println(jVariable.toString());
            
            // assumption: same name more than once means that it's two paramaters
            // of same name and type, one for input and one for output.  That's
            // a safe assumption for the most part, but the engine DOES allow same
            // name different types as a possibility. 
            
            iVariable.setUsage(DataVariable.USAGE_INPUT_AND_OUTPUT);
            jVariable.setName(null);
          }
        }
      }
      
      // turfing the unnecessary variables.
      
      dataVariableList = new LinkedList();

      for(int i = 0; i < dataVariableArray.length; i++) {
        DataVariable iVariable = (DataVariable) dataVariableArray[i];
        if (iVariable.getName() != null) {
          dataVariableList.add(dataVariableArray[i]); 
        }
      }
    }
    return dataVariableList;
  }
  
  public List getAnalysisResults() {
    return ANALYSIS_RESULTS_PARSER.getAnalysisResults();
  }

}

/* If I had a better grip on XQuery via Saxon, I might be able to 
 * do more than just well-formedness checks. Smarts will have to wait until later.
 * TODO: Make JXqueryEditorPane a more context-aware, useful to the user experience.
 */

class XQueryStyledDocument extends AbstractXMLStyledDocument{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final SimpleXQueryParser parser = new SimpleXQueryParser();

  private String preEditorText;
  private String postEditorText;
  
  private String parseError = null;

  private static final IgnoreBadCharactersFilter IGNORE_BAD_CHARACTERS_FILTER 
      = new IgnoreBadCharactersFilter();
  
  public XQueryStyledDocument(ValidityEditorPane editor) {
    super(editor);
    setDocumentFilter(IGNORE_BAD_CHARACTERS_FILTER);
    setPreAndPostEditorText("","");
  }
  
  public void setPreAndPostEditorText(String preEditorText, String postEditorText) {
    this.preEditorText = preEditorText;
    this.postEditorText = postEditorText;
  }
    
  public void checkValidity() {
    if (getEditor().getText().equals("")) {
      setContentValid(true);
      return;
    }
    
    try {
      parser.parse(preEditorText + getEditor().getText() + postEditorText);
      setContentValid(true);
      parseError = null;
    } catch (XPathException e) {
      parseError = e.getMessageAndLocation();
      setContentValid(false);
    } 
  }
  
  public List getProblemList() {
    LinkedList problemList = new LinkedList();
    problemList.add(parseError);
    return problemList;
  }
}

class SimpleXQueryParser extends QueryParser {
  private static final StaticQueryContext context =
     new StaticQueryContext();
  
  public Expression parse(String query) throws XPathException {
    return super.parse(query,context);
  }
  
  public Expression parseForExpression(String query) throws XPathException {
    return super.parseForExpression();
  }
}

// TODO: This is a quick-fix to stop users from inputting double quote (") and (&) characters into 
// XQuery expressions. BETA4-6 of the engine does not do ("), and the (&) character can allow 
// users to hurt themselves with engine validation.

class IgnoreBadCharactersFilter extends DocumentFilter {

  public void replace(DocumentFilter.FilterBypass bypass,
                      int offset,
                      int length,
                      String text,
                      AttributeSet attributes) throws BadLocationException {
    if (isValidText(text))
      super.replace(bypass,offset,length,text,attributes);
  }

  protected boolean isValidText(String text) {
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i)== '\"' || text.charAt(i)=='&') {
        return false;
      }
    }
    return true;
  }
}
