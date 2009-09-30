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

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Token;
import net.sf.saxon.query.QueryModule;
import net.sf.saxon.query.QueryParser;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trans.XPathException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.swing.data.AbstractXMLStyledDocument;
import org.yawlfoundation.yawl.editor.swing.data.ValidityEditorPane;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EnvironmentBasedClient;
import org.yawlfoundation.yawl.schema.ElementCreationInstruction;
import org.yawlfoundation.yawl.schema.XMLToolsForYAWL;
import org.yawlfoundation.yawl.unmarshal.SchemaForSchemaValidator;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.SaxonUtil;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.util.*;
import java.util.prefs.Preferences;

public class AvailableEngineProxyImplementation implements
  YAWLEngineProxyInterface {

  protected static final Preferences prefs = 
    Preferences.userNodeForPackage(YAWLEditor.class);
  
  private static final ResetNetAnalysisResultsParser ANALYSIS_RESULTS_PARSER = new ResetNetAnalysisResultsParser();
  
  private static final SchemaForSchemaValidator schemaValidator = SchemaForSchemaValidator.getInstance();
  private static final InstanceSchemaValidator instanceValidator = InstanceSchemaValidator.getInstance();
  
  protected String sessionID = "";
  
  protected InterfaceA_EnvironmentBasedClient clientInterfaceA;

  protected String engineURI;
  
  private XMLToolsForYAWL xmlTools;
  
  public void engineFormatFileExport(SpecificationModel editorSpec) {
    EngineSpecificationHandler.getInstance().engineFormatFileExport(editorSpec);
  }
  
  public void engineFormatFileImport() {
    EngineSpecificationHandler.getInstance().engineFormatFileImport();
  }
  
  public void engineFormatFileImport(String fileName) {
    EngineSpecificationHandler.getInstance().engineFormatFileImport(fileName);
  }

  public void validate(SpecificationModel editorSpec) {
    EngineSpecificationHandler.getInstance().validate(editorSpec);
  }
  
  public void connect() {  } 
  
  public boolean testConnection(String engineURL, String engineUserId, String engineUserPassword) {
   return false;
  } 
  
  public void disconnect() {
    sessionID = "";
    clientInterfaceA = null;
  }

  public boolean isConnectable() { return false; }

  public HashMap getRegisteredYAWLServices() {
    return new HashMap();
  }

  public boolean connected() {
   return false;
  }
  

  public LinkedList getSchemaValidationResults(String schema) {
    LinkedList<String> errorList = new LinkedList<String>();
    try {
      String errors = schemaValidator.validateSchema(schema);

      if (errors == null || errors.trim().equals("")) {
        return null;
      }
      
      String[] errorsAsArray = errors.split("\n");

      errorList.addAll(Arrays.asList(errorsAsArray));
      return errorList;
      
    } catch (Exception e) {
      errorList.add(e.toString());
      return errorList;
    } 
  }
  
  public void setDataTypeSchema(String schema) {
    try {
      xmlTools = new XMLToolsForYAWL();
      xmlTools.setPrimarySchema(schema); 
    } catch (Exception e) {
      xmlTools = null;
    }
  }

  public String getDataTypeSchema() {
      String schema = null;
      if (hasValidDataTypeDefinition()) {
          schema = xmlTools.getSchemaString();
      }
      return schema;
  }
  
  public boolean hasValidDataTypeDefinition() {
    return (xmlTools != null) ;
  }

  public Set getPrimarySchemaTypeNames() {
    if (hasValidDataTypeDefinition()) {
      return xmlTools.getPrimarySchemaTypeNames();
    }
    return null;
  }
  
  public String createSchemaForVariable(String variableName, String dataType) {
    try {
      return xmlTools.createYAWLSchema(
          new ElementCreationInstruction[] 
            { new ElementCreationInstruction(variableName, dataType, false) },
          "data"
      ); 
    }
    catch (Exception e) {
      return null;
    }    
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
            complexity = getDataSchemaComplexity(dataTypeSchema);
            break;
        }
      }
    }
    return complexity;
  }


  private int getDataSchemaComplexity(String schema) {
      Document doc = JDOMUtil.stringToDocument(schema);
      Element root = doc.getRootElement();                        // schema
      Namespace ns = root.getNamespace();

      // schemas for complex and simple types have the same prolog - read & discard
      Element element = root.getChild("element", ns) ;
      element = element.getChild("complexType", ns);
      element = element.getChild("sequence", ns);
      element = element.getChild("element", ns);

      // the next child is either simpleType or complexType
      element = element.getChild("complexType", ns);
      return (element != null) ? COMPLEX_DATA_TYPE_COMPLEXITY : SIMPLE_DATA_TYPE_COMPLEXITY;
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
    return new LinkedList();
  }
  
  public List getAnalysisResults(SpecificationModel editorSpec) {
    return ANALYSIS_RESULTS_PARSER.getAnalysisResults(editorSpec);
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
    if (isValidating()) {
      if (getEditor().getText().equals("")) {
        setContentValid(AbstractXMLStyledDocument.Validity.INVALID);
        return;
      }
    
      try {
        SaxonUtil.compileXQuery(preEditorText + getEditor().getText() + postEditorText);
        setContentValid(AbstractXMLStyledDocument.Validity.VALID);
        parseError = null;
      }
      catch (SaxonApiException e) {
        parseError = e.getMessage().split("\n")[1].trim();
        setContentValid(AbstractXMLStyledDocument.Validity.INVALID);
      }
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
     new StaticQueryContext(new Configuration());
  
  public Expression parse(String query) throws XPathException {
    return super.parse(query, 0, Token.EOF, -1, new QueryModule(context));
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
