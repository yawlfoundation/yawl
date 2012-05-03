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
import org.jdom2.Element;
import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.swing.data.AbstractXMLStyledDocument;
import org.yawlfoundation.yawl.editor.swing.data.ValidityEditorPane;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EnvironmentBasedClient;
import org.yawlfoundation.yawl.resourcing.util.DataSchemaBuilder;
import org.yawlfoundation.yawl.schema.SchemaHandler;
import org.yawlfoundation.yawl.schema.YDataValidator;
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
  
  private static final InstanceSchemaValidator instanceValidator = InstanceSchemaValidator.getInstance();
  
  protected String sessionID = "";
  
  protected InterfaceA_EnvironmentBasedClient clientInterfaceA;

  protected String engineURI;

  private SchemaHandler schemaHandler;
  
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
  
  public boolean connect() { return false; } 
  
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

  public Map<String, String> getExternalDataGateways() {
      return new Hashtable<String, String>();
  }


  public boolean connected() {
   return false;
  }
  

  public LinkedList getSchemaValidationResults(String schema) {
    LinkedList<String> errorList = new LinkedList<String>();
    YDataValidator validator = new YDataValidator(schema);
    if (validator.validateSchema()) return null;      // OK - no errors

    errorList.addAll(validator.getMessages());
    return errorList;
  }
  
  public void setDataTypeSchema(String schema) {
      schemaHandler = new SchemaHandler(schema);
  }

  public String getDataTypeSchema() {
      if (hasValidDataTypeDefinition()) {
          return schemaHandler.getSchema();
      }
      return null;
  }
  
  public boolean hasValidDataTypeDefinition() {
      return schemaHandler.compileSchema();
  }

  public Set<String> getPrimarySchemaTypeNames() {
      return schemaHandler.getPrimaryTypeNames();
  }

    public boolean isDefinedTypeName(String name) {
        return getPrimarySchemaTypeNames().contains(name);
    }
  
  public String createSchemaForVariable(String variableName, String dataType) {
      DataSchemaBuilder dsb = new DataSchemaBuilder(schemaHandler.getTypeMap());
      return dsb.buildSchema("data", variableName, dataType);
  }
  
  public int getDataTypeComplexity(String dataType) {
      Element definition = schemaHandler.getDataTypeDefinition(dataType);
      if (definition.getName().endsWith("complexType")) {
          return COMPLEX_DATA_TYPE_COMPLEXITY;
      }
      else if (definition.getName().endsWith("simpleType")) {
          return SIMPLE_DATA_TYPE_COMPLEXITY;
      }
      else return UNRECOGNISED_DATA_TYPE_COMPLEXITY;
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
          parseError = "Query required";
        setContentValid(AbstractXMLStyledDocument.Validity.INVALID);
        return;
      }

      if (getEditor().getText().matches(
              "^\\s*timer\\(\\w+\\)\\s*!?=\\s*'(dormant|active|closed|expired)'\\s*$")) {
          setContentValid(AbstractXMLStyledDocument.Validity.VALID);
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
