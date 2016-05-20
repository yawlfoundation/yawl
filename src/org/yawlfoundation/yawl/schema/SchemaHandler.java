/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.schema;

import org.jdom2.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This object acts as a reusable Schema validator for a given schema. Once
 * the schema has been successfully compiled, any number of XML documents can
 * be validated by calling either of the validate methods.
 *
 * @author Mike Fowler
 *         Date: 04-Jul-2006
 */
public class SchemaHandler {

    // Raw schema source - can be initiated as a String, InputStream or a URL to the xsd
    private Source schemaSource;

    // Object model of the Schema
    private Schema schema;

    // String version of the Schema - needed by calling classes
    private String schemaString;

    // a map of complex-type names to their element definitions
    private Map<String, Element> typeMap;

    // Captures all errors and warning relating to the parsing of an XML document
    private ErrorHandler errorHandler;

    // Used to capture the last exception thrown by the handler or XML parser.
    private String exceptionMessage;

    // Indicates if the schema has compiled successfully.
    private boolean compiled = false;


    /**
     * Private no-argument constructor
     */
    private SchemaHandler() {
        errorHandler = new ErrorHandler();
    }

    /**
     * Constructs a new SchemaHandler
     * @param xml XML String representing the schema this handler will use for validation
     */
    public SchemaHandler(String xml) {
        this();
        schemaString = xml;
        setSchema(schemaString);
    }

    /**
     * Constructs a new SchemaHandler
     * @param is a Stream representing the schema this handler will use for validation
     */
    public SchemaHandler(InputStream is) {
        this();
        schemaString = StringUtil.streamToString(is);
        setSchema(schemaString);
    }

    /**
     * Constructs a new SchemaHandler
     * @param url a URL to the XSD representing the schema this handler will use for validation
     */
    public SchemaHandler(URL url) {
        this();
        schemaString = streamToString(url);
        schemaSource = new StreamSource(url.toExternalForm());
    }


    /**
     * Compiles the schema, and if successful validates an XML String against it
     * @param xml the XML String to validate against Schema
     * @return true if the Schema compiles without error AND the XML is a valid instance
     * of the Schema
     */
    public boolean compileAndValidate(String xml) {
        return compileSchema() && validate(xml);
    }


    /**
     * Attempts to compile the schema. If successful, allows an XML document to be validated.
     * @return true if the schema has compiled successfully.
     */
    public boolean compileSchema() {
        if (compiled) return true;

        errorHandler.reset();
        exceptionMessage = null;

        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setErrorHandler(errorHandler);
        //    factory.setResourceResolver(ResourceResolver.getInstance());
            schema = factory.newSchema(schemaSource);
            return compiled = errorHandler.isValid();
        }
        catch (Exception e) {
            exceptionMessage = "Schema compile failed with exception: " + e.getMessage();
            return false;
        }
    }

    /**
     * Validates the given XML document against the compiled schema.
     * @param xml instance document to be validated.
     * @return true if the xml is a valid instance of the schema
     * @throws IllegalStateException if schema has not been compiled successfully
     */
    public boolean validate(String xml) {
        if (! compiled) {
            throw new IllegalStateException("Schema must first have been successfully " +
                    "compiled before validation can be performed.");
        }

        errorHandler.reset();
        exceptionMessage = null;

        try {
            Validator validator = schema.newValidator();
            validator.setErrorHandler(errorHandler);
            validator.validate(stringToSource(xml));
            return errorHandler.isValid();
        }
        catch (Exception e) {
            exceptionMessage = "Validation failed with exception: " + e.getMessage();
            return false;
        }
    }


    /**
     * @return all error messages from the last validation/compilation
     */
    public List<String> getErrorMessages() {
        return errorHandler.getErrors();
    }

    /**
     * @return all warning messages from the last validation/compilation
     */
    public List<String> getWarningMessages() {
        return errorHandler.getWarnings();
    }

    /**
     * @return all messages from the last validation/compilation
     */
    public List<String> getMessages() {
        List<String> messages = errorHandler.getErrors();
        messages.addAll(errorHandler.getWarnings());
        if (exceptionMessage != null) messages.add(exceptionMessage);
        return messages;
    }

    /**
     * @return all messages since the last validation/compilation
     */
    public String getConcatenatedMessage() {
        StringBuilder builder = new StringBuilder();
        for (String msg : getMessages()) {
            builder.append(msg).append("\n");
        }
        return builder.toString();
    }

    /**
     * @return String representation of the schema
     */
    public String getSchema() {
        return schemaString;
    }

    /**
     * @param schema new schema to use (resets everything)
     */
    public void setSchema(String schema) {
        schemaString = schema;
        try {
            schemaSource = stringToSource(schema);
        }
        catch (UnsupportedEncodingException uee) {
            schemaSource = new StreamSource(new StringReader(schema));  // fallback
        }
        errorHandler.reset();
        typeMap = null;
        compiled = false;
    }

    /**
     * @return the set of (first-level) type names defined in this schema
     */
    public Set<String> getPrimaryTypeNames() {
        assembleMap();
        return typeMap.keySet();
    }

    /**
     * Gets the schema element definition of a data type by name
     * @param typeName the data type name
     * @return the corresponding definition, or null if the type name is unknown
     */
    public Element getDataTypeDefinition(String typeName) {
        assembleMap();
        return typeMap.get(typeName);
    }

    /**
     * Gets the map of data type names to their definitions
     * @return the map of names to definitions
     */
    public Map<String, Element> getTypeMap() {
        assembleMap();
        return typeMap;
    }

    /**
     * Converts a string to a UTF-8 encoded InputSource
     * @param xml the XML string
     * @return a Source object to the String
     * @throws UnsupportedEncodingException
     */
    private Source stringToSource(String xml) throws UnsupportedEncodingException {
        return new StreamSource(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }


    /**
     * Reads the contents at a URL into a String
     * @param url the URL resource
     * @return a String containing the resource at the URL
     */
    private String streamToString(URL url) {
        try {
            return StringUtil.streamToString(url.openStream());
        }
        catch (IOException ioe) {       // when opening stream
            return null;
        }
    }


    /**
     * Creates a map of type names to their elements.
     */
    private void assembleMap() {
        if (typeMap == null) {
            typeMap = new Hashtable<String, Element>();
            if (schemaString != null) {
                Element dataSchema = JDOMUtil.stringToElement(getSchema());
                for (Element child : dataSchema.getChildren()) {
                    String name = child.getAttributeValue("name");
                    if (name != null) {
                        typeMap.put(name, child);
                    }
                }
            }
        }
    }

}
