/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.unmarshal;

import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 4/06/2004
 * Time: 12:23:45
 * 
 */
public class SchemaForSchemaValidator extends DefaultHandler {
    StringBuffer _errorsString = new StringBuffer("");
    private static SchemaForSchemaValidator _myInstance;
    private SAXBuilder _builder;
    private File _tempSchema;

    private SchemaForSchemaValidator() {
        _builder = new SAXBuilder();
        _builder.setValidation(false);
    }

    public static SchemaForSchemaValidator getInstance() {
        if (_myInstance == null) {
            _myInstance = new SchemaForSchemaValidator();
        }
        return _myInstance;
    }


    public void warning(SAXParseException ex) {
        addMessage(ex, "Warning");
    }


    public void error(SAXParseException ex) {
        addMessage(ex, "Invalid");
    }


    public void fatalError(SAXParseException ex) throws SAXException {
        addMessage(ex, "Error");
    }


    private void addMessage(SAXParseException e, String errType) {
        String lineNum = getLineNumber(e);
        if (lineNum != null) {
            _errorsString.append(errType + "#" + lineNum + "# " + e.getMessage() + '\n');
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
            XMLReader parser = setUpChecker();
            parser.parse(input);
        } catch (SAXParseException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return _errorsString.toString();
    }


    private String checkSchema(String xmlFileURL) {
        _errorsString.delete(0, _errorsString.length());
        try {
            XMLReader parser = setUpChecker();
            parser.parse(xmlFileURL);
        } catch (SAXParseException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return _errorsString.toString();
    }


    private XMLReader setUpChecker() throws SAXException {
        XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
        parser.setContentHandler(this);
        parser.setErrorHandler(this);
        parser.setFeature("http://xml.org/sax/features/validation", true);
        parser.setFeature("http://apache.org/xml/features/validation/schema", true);
        parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
        try {
            URL schemaURL = _tempSchema.toURL();
            parser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
                    "" + schemaURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return parser;
    }


    public static void main(String[] args) throws IOException {
        SchemaForSchemaValidator tmp = SchemaForSchemaValidator.getInstance();
//        String testSchema =
//                "<xs:complexType name=\"fred\" >\n" +
//                "<xs:sequence>\n" +
//                "<xs:element name=\"name\" type=\"string\"/>\n" +
//                "<xs:element name=\"age\" type=\"nonNegativeInteger\"/>\n" +
//                "</xs:sequence>\n" +
//                "</xs:complexType>";
//        System.out.println(tmp.validateSchemaChunk(testSchema));
        String testSchema = "";
        BufferedReader br = new BufferedReader(
                new FileReader("D:\\Yawl\\schema\\SelfContainedPerson.xsd"));
        String line = null;
        while ((line = br.readLine()) != null) {
            testSchema += line;
        }
        //System.out.println("testSchema = \n" + testSchema);
    }


    /**
     * If the string is of 0 length then you know it has passed the test.
     * @param chunk
     * @return the validation failures as a string.
     */
    public String validateSchemaChunk(String chunk) {
        if (!wellFormed(chunk)) {
            return "Schema must be self contained and well-formed";
        }
        _errorsString.delete(0, _errorsString.length());
        String schema =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<schema " +
                "xmlns=\"http://www.w3.org/2001/XMLSchema\" " +
                "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" " +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                "elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\n" +
                chunk +
                "\n</schema>";
        String result = validateSchema(schema);
        result = groomLineNumbers(result);
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


    /**
     * If the string is of 0 length then you know it has passed the test.
     * @return the validation failures as a string.
     */
    public String validateSchema(String schema) {
        File userDir = new File(System.getProperty("java.io.tmpdir"));
        _tempSchema = new File(userDir, "_tempSchema.xsd");
        try {
            FileWriter writer = new FileWriter(_tempSchema);
            writer.write(schema);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String instance =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<document xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                "</document>";
        String result = checkSchema(new InputSource(new StringReader(instance)));
        _tempSchema.delete();
        return result;
    }
}
