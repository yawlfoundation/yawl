package org.yawlfoundation.yawl.editor.thirdparty.engine;

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;
import org.jdom.input.SAXBuilder;

import java.io.StringReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;


/**
 /**
 * Copyright � 2003 Queensland University of Technology. All rights reserved.
 * @author Lachlan Aldred
 * Date: 4/06/2004
 * Time: 12:23:45
 * This file remains the property of the YAWL team at the Queensland University of
 * Technology (Wil van der Aalst, Arthur ter Hofstede, Lachlan Aldred, Lindsay Bradford,
 * and Marlon Dumas).
 * You do not have permission to use, view, execute or modify the source outside the terms
 * of the YAWL SOFTWARE LICENCE.
 * For more information about the YAWL SOFTWARE LICENCE refer to the 'downloads' section under
 * http://www.citi.qut.edu.au/yawl.
 */
public class InstanceSchemaValidator extends DefaultHandler {
    StringBuffer _errorsString = new StringBuffer("");
    private static InstanceSchemaValidator _myInstance;
    private SAXBuilder _builder;

    private File elementSchema;
    
    private InstanceSchemaValidator() {
        _builder = new SAXBuilder();
        _builder.setValidation(false);
    }

    public static InstanceSchemaValidator getInstance() {
        if (_myInstance == null) {
            _myInstance = new InstanceSchemaValidator();
        }
        return _myInstance;
    }

    public void warning(SAXParseException ex) {
        addMessage(ex, "Warning");
    }

    public void error(SAXParseException ex) {
        addMessage(ex, "Invalid");
    }

    public void fatalError(SAXParseException ex) {
        addMessage(ex, "Error");
    }


    private void addMessage(SAXParseException e, String errType) {
        String lineNum = getLineNumber(e);
        if (lineNum != null) {
            _errorsString.append(errType + "#" + lineNum + "# " + e.getMessage() + '\n');
        } else {
          _errorsString.append(errType + "#" + " - " + "# " + e.getMessage() + '\n');
        }
    }


    private String getLineNumber(SAXParseException e) {
        String fileURL = e.getSystemId();
        if (fileURL != null) {
            return
                    "[ln: " + e.getLineNumber() + " col: " + e.getColumnNumber() + "]";
        }
        return null;
    }


    private String checkSchema(InputSource input) {
        _errorsString.delete(0, _errorsString.length());
        try {
            XMLReader parser = setUpTypeSchemaChecker();
            parser.parse(input);
        } catch (SAXParseException e) {
           addMessage(e, "Error");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return _errorsString.toString();
    }

    private XMLReader setUpTypeSchemaChecker() throws SAXException {
        XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
        parser.setFeature("http://xml.org/sax/features/validation", true);
        parser.setFeature("http://apache.org/xml/features/validation/schema", true);
        parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
        try {
            URL schemaURL = elementSchema.toURL();
            parser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
                "" + schemaURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        parser.setContentHandler(this);
        parser.setErrorHandler(this);

        return parser;
    }
    
    public String validateBaseDataTypeInstance(String simpleTypeDefinition, String simpleTypeInstance) {
      if (!wellFormed(simpleTypeDefinition)) {
          return "Schema must be self contained and well-formed";
      }
      
      _errorsString.delete(0, _errorsString.length());
      String schema =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<schema xmlns=\"http://www.w3.org/2001/XMLSchema\">\n" +
        "<element name=\"data\">\n" +
        "  <complexType>\n" +
        "    <sequence>\n" +
        simpleTypeDefinition + "\n" +
        "    </sequence>\n" +
        "  </complexType>\n" + 
        "</element>\n" + 
        "</schema>";
      String result = validateWrappedInstance(schema, simpleTypeInstance);
      result = groomLineNumbers(result);
      return result;
    }

    
    public String validateUserSuppliedDataTypeInstance(String variableName, String userSuppliedType, String complexTypeInstance) {
      String result ;
      String schema = YAWLEngineProxy.getInstance().createSchemaForVariable(
          variableName, userSuppliedType
      );

      if (schema != null) {
          result = validateWrappedInstance(schema, "<" + variableName +">" +
                                   complexTypeInstance + "</" + variableName + ">");
          result = groomLineNumbers(result);
      }
      else {
          result = "Missing or invalid data type: defaulting to 'anyType'.";
      }
      return result;
    }

    private boolean wellFormed(String chunk) {
      try {
          XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
          parser.setErrorHandler(this);
          parser.setFeature("http://xml.org/sax/features/namespaces", false);
          parser.parse(new InputSource(new StringReader(chunk)));
      } catch (java.io.IOException e) {
          System.err.println("IOException while parsing: " + e.getMessage());
      } catch (SAXException e) {
          return false;
      }
      return true;
    }


    private String groomLineNumbers(String result) {
        StringBuffer bufferedResult = new StringBuffer(result);
        int pos = bufferedResult.indexOf("[ln: ", 0);
        while (pos != -1) {
            pos = bufferedResult.indexOf("[ln: ", pos);
            int pos2 = bufferedResult.indexOf(" ", pos + 5);
            String lineNumStr = bufferedResult.substring(pos + 5, pos2);
            int i = Integer.parseInt(lineNumStr);
            bufferedResult.replace(pos + 5, pos2, "" + (i - 1));
            pos = bufferedResult.indexOf("[ln:", pos + 10);
        }
        return bufferedResult.toString();
    }

    private String validateWrappedInstance(String schema, String instanceContent) {

      String instance =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<data>\n" + 
        instanceContent + "\n" + 
        "</data>";

      return getInstanceCheckResults(schema, instance);
    }
    
    private String getInstanceCheckResults(String schema, String instance) {
      File tempDir = new File(System.getProperty("java.io.tmpdir"));

      elementSchema = new File(tempDir, "_editorElementSchema.xsd");
      try {
          FileWriter writer = new FileWriter(elementSchema);
          writer.write(schema);
          writer.flush();
          writer.close();
      } catch (IOException ioe) {
          ioe.printStackTrace();
      }
      
      try {
        setUpTypeSchemaChecker();
      } catch (SAXException saxe) {
        saxe.printStackTrace();
      }
      
      // showPreCheckDebugState(schema, instance);
      String result = checkSchema(new InputSource(new StringReader(instance)));
      // showPostCheckDebugState(result);
      
      elementSchema.delete();

      return result;
    }
/*
    private void showPreCheckDebugState(String schema, String instance) {
      System.out.println("\n====");
      System.out.println(schema);
      System.out.println("----");
      System.out.println(instance);
      System.out.println("====\n");
    }
    
    private void showPostCheckDebugState(String result) {
      System.out.println(result);
    }
    */
}
