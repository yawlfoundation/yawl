/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.unmarshal;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

/**
 * 
 * @author Lachlan Aldred
 * 
 */

public class XMLValidator extends DefaultHandler {
    StringBuilder _errorsString = new StringBuilder("");
    private String _schemaLocation;


    public XMLValidator() {
    }

    /**
     *
     * @param location a location of the form "URI absolute_package_path_to_file"
     *   E.g. "http://www.yawlfoundation.org/yawlschema /org/yawlfoundation/yawl/cost/costmodel.xsd"
     */
    public void setSchemaLocation(String location) {
        _schemaLocation = location;
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
        _errorsString.append(
                 String.format("%s#%s#%s\n", errType, getLineNumber(e), e.getMessage()));
    }


    private String getLineNumber(SAXParseException e) {
        return (e.getSystemId() != null) ?
                "[ln: " + e.getLineNumber() + " col: " + e.getColumnNumber() + "]" : "";
    }

    
    public String checkSchema(URL schemaLocation, String XMLData) {
        return (schemaLocation != null) ? checkSchema(schemaLocation.toExternalForm(), XMLData) : "";
    }

    public String checkSchema(String schemaLocation, String XMLData) {
        _errorsString.delete(0, _errorsString.length());
        try {
            XMLReader parser = setUpChecker(schemaLocation);
            parser.parse(new InputSource(new StringReader(XMLData)));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return _errorsString.toString();
    }


    /**
     * Sets the checker up for a run.
     * @param schemaLocation the version of the schema
     * @return a reader configured to do the checking.
     * @throws SAXException
     */
    private XMLReader setUpChecker(String schemaLocation) throws SAXException, IOException {
        XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
        parser.setProperty("http://apache.org/xml/properties/schema/external-noNamespaceSchemaLocation",
                                        schemaLocation);
        parser.setContentHandler(this);
        parser.setErrorHandler(this);
        parser.setFeature("http://xml.org/sax/features/validation", true);
        parser.setFeature("http://apache.org/xml/features/validation/schema", true);
        parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
        return parser;
    }
}
