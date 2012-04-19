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

import org.yawlfoundation.yawl.util.DOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.util.Vector;

/**
 * This object acts a reusable Schema validator for a given schema. Once
 * the schema has been succesfully compiled, any number of XML documents can
 * be validated by calling either of the validate methods.
 *
 * @author Mike Fowler
 *         Date: 04-Jul-2006
 */
public class SchemaHandler
{
    /**
     * String representation of the Schema
     */
    private String schemaXML;

    /**
     * Java Object model of the Schema schemaXML.
     */
    private Schema schema;

    /**
     * Captures all errors and warning relating to the parsing of an XML document.
     */
    private ErrorHandler errorHandler;

    /**
     * Used to capture the last exception thrown by the hanlder or XML parser.
     */
    private String exceptionMessage;

    /**
     * Indicates if the schema has compiled succesfully.
     */
    private boolean compiled = false;

    /**
     * Constructs a new SchemaHandler based on the schema schemaXML.
     *
     * @param schemaXML Schema XML to based schemaHandler
     */
    public SchemaHandler(String schemaXML)
    {
        this.schemaXML = schemaXML;
        this.errorHandler = new ErrorHandler();
    }

    /**
     * Attempts to compile the schema. If successful, allows an XML document to be validated.
     *
     * @return true if the schema has compiled successfully.
     */
    public boolean compileSchema()
    {
        errorHandler.reset();
        exceptionMessage = null;

        try
        {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setErrorHandler(errorHandler);

            schema = factory.newSchema(new SAXSource(DOMUtil.createUTF8InputSource(schemaXML)));
            return compiled = errorHandler.isValid();
        }
        catch (Exception e)
        {
            exceptionMessage = "Validation failed with exception: " + StringUtil.convertThrowableToString(e);
            return false;
        }
    }

    /**
     * Validates the given XML document against the compiled schema.
     *
     * @param xml instance document to be validated.
     * @return true if the xml is a valid instance of the schema
     * @throws IllegalStateException if schema has not been compiled succesfully
     */
    public boolean validate(String xml)
    {
        if(!compiled)
        {
            throw new IllegalStateException("SchemaHandler must have a valid compiled schema before validation can be performed.");
        }

        errorHandler.reset();
        exceptionMessage = null;

        try
        {
            Validator validator = schema.newValidator();
            validator.setErrorHandler(errorHandler);
            validator.validate(new SAXSource(DOMUtil.createUTF8InputSource(xml)));
            return errorHandler.isValid();
        }
        catch (Exception e)
        {
            exceptionMessage = "Validation failed with exception: " + StringUtil.convertThrowableToString(e);
            return false;
        }
    }

    /**
     * Validates the given XML document against the compiled schema. Converts the
     * dom to a String and calls validate(String xml).
     *
     * @param dom document to be validated
     * @return true if the xml is a valid instance of the schema
     * @throws IllegalStateException if schema has not been compiled succesfully
     */
    public boolean validate(Document dom)
    {
        try
        {
            return validate(DOMUtil.getXMLStringFragmentFromNode(dom));
        }
        catch (TransformerException e)
        {
            exceptionMessage = "Validation failed with exception: " + StringUtil.convertThrowableToString(e);
            return false;
        }
    }

    /**
     * @return all error messages from the last validation/compilation
     */
    public Vector<String> getErrorMessages()
    {
        return errorHandler.getErrors();
    }

    /**
     * @return all warning messages from the last validation/compilation
     */
    public Vector<String> getWarningMessages()
    {
        return errorHandler.getWarnings();
    }

    /**
     * @return all messages from the last validation/compilation
     */
    public Vector<String> getMessages()
    {
        Vector<String> messages = errorHandler.getErrors();
        messages.addAll(errorHandler.getWarnings());
        if (exceptionMessage != null) messages.add(exceptionMessage);
        return messages;
    }

    /**
     * @return all messages since the last validation/compilation
     */
    public String getConcatenatedMessage()
    {
        StringBuilder builder = new StringBuilder();

        for(String string : getErrorMessages())
        {
            builder.append(string);
            builder.append("\n");
        }

        for(String string : getWarningMessages())
        {
            builder.append(string);
            builder.append("\n");
        }

        if (exceptionMessage != null) builder.append(exceptionMessage);

        return builder.toString();
    }

    /**
     * @return String representation of the schema
     */
    public String getSchema()
    {
        return schemaXML;
    }

    /**
     * @param schema new schema to use (resets everything)
     */
    public void setSchema(String schema)
    {
        schemaXML = schema;
        errorHandler.reset();
        compiled = false;
    }
}
