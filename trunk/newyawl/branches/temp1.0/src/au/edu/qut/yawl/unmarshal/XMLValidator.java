/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.unmarshal;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 * @author Lachlan Aldred
 * 
 */

public class XMLValidator extends DefaultHandler {
    StringBuffer _errorsString = new StringBuffer("");
    private String _tempDataFileName = "data.xml";
    private String _tempSchemaFileName = "theSchema.xsd";
    private File _tempSchema;
    private File _tempData;


    public XMLValidator() {
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
        _errorsString.append(errType + "#" + lineNum + "# " + e.getMessage() + '\n');
    }


    private String getLineNumber(SAXParseException e) {
        String fileURL = e.getSystemId();
        if (fileURL != null) {
            return
                    //fileURL.substring(fileURL.lastIndexOf("/") + 1) +
                    "[ln: " + e.getLineNumber() + " col: " + e.getColumnNumber() + "]";
        }
        return "";
    }


    public String checkSchema(String theSchemaAsString, String theXMLDataAsString) {
        _errorsString.delete(0, _errorsString.length());
        try {
            XMLReader parser = setUpChecker(theSchemaAsString);

            _tempData = new File(_tempDataFileName);
            FileWriter fw = new FileWriter(_tempData);
            fw.write(theXMLDataAsString);
            fw.flush();
            fw.close();

            parser.parse(_tempData.getAbsolutePath());
        } catch (SAXParseException e) {
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            _tempData.delete();
            _tempSchema.delete();
        }
        return _errorsString.toString();
    }


    /**
     * Sets the checker up for a run.
     * @param theSchemaAsString the version of the schema
     * @return a reader configured to do the checking.
     * @throws SAXException
     */
    private XMLReader setUpChecker(String theSchemaAsString) throws SAXException, IOException {
        XMLReader parser = XMLReaderFactory.createXMLReader(
                "org.apache.xerces.parsers.SAXParser");

        _tempSchema = new File(_tempSchemaFileName);

        FileWriter fw = new FileWriter(_tempSchema);
        fw.write(theSchemaAsString);
        fw.flush();
        fw.close();
        parser.setProperty("http://apache.org/xml/properties/schema/external" +
                "-noNamespaceSchemaLocation",
                "" + _tempSchema.toURI());

        parser.setContentHandler(this);
        parser.setErrorHandler(this);
        parser.setFeature("http://xml.org/sax/features/validation", true);
        parser.setFeature("http://apache.org/xml/features/validation/schema", true);
        parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
        return parser;
    }
}
