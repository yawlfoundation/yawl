/* * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved. * The YAWL Foundation is a collaboration of individuals and * organisations who are committed to improving workflow technology. * * This file is part of YAWL. YAWL is free software: you can * redistribute it and/or modify it under the terms of the GNU Lesser * General Public License as published by the Free Software Foundation. * * YAWL is distributed in the hope that it will be useful, but WITHOUT * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General * Public License for more details. * * You should have received a copy of the GNU Lesser General Public * License along with YAWL. If not, see <http://www.gnu.org/licenses/>. */package org.yawlfoundation.yawl.schema;import org.jdom.Element;import org.yawlfoundation.yawl.elements.data.YVariable;import org.yawlfoundation.yawl.exceptions.YDataValidationException;import org.yawlfoundation.yawl.util.DOMUtil;import org.yawlfoundation.yawl.util.JDOMUtil;import javax.xml.XMLConstants;import java.util.ArrayList;import java.util.Collection;import java.util.Collections;import java.util.Vector;/** * This class serves as a validation mechanism for the specification specific * schema and the instance data from either the net or a task. This is performed * by taking the data available at the various validation points and converting * it into conventional XML which is then validated using a SchemaHandler. * * @author Mike Fowler *         Date: 05-Jul-2006 */public class YDataValidator{    /**     * Object that performs the real validation on XML documents     */    private SchemaHandler handler;    /**     * Constructs a new validator and handler. The     * handler is not ready for use until validateSchema     * has been called.     *     * @param schema W3C XML Schema     */    public YDataValidator(String schema)    {        this.handler = new SchemaHandler(schema);    }    /**     * Compiles and determines the validity of the current schema     * @return true if the schema compiled without error.     */    public boolean validateSchema()    {        return handler.compileSchema();    }    /**     * Validates a single data variable     *     * @param variable to be validated     * @param data XML representation of variable to be validated     * @param source     * @throws YDataValidationException if the data is not valid     */    public void validate(YVariable variable, Element data, String source)            throws YDataValidationException    {        Vector<YVariable> vars = new Vector<YVariable>(1);        vars.add(variable);        validate(vars, data, source);    }    /**     * Validates a collection of variables against the schema. This is achieved by     * temporarily adding a schema element declaration for the data. This avoids     * attempting to create a new schema containing only the relevant data types.     *     * @param vars variables to be validated     * @param data XML representation fo the variables to be validated     * @param source     * @throws YDataValidationException if the data is not valid     */    public void validate(Collection vars, Element data, String source)            throws YDataValidationException    {        try        {            String schema = ensurePrefixedSchema(handler.getSchema());            org.w3c.dom.Document xsd = DOMUtil.getDocumentFromString(schema);            String ns = XMLConstants.W3C_XML_SCHEMA_NS_URI;            //need to determine the prefix for the schema namespace            String prefix = ensureValidPrefix(xsd.lookupPrefix(ns));            org.w3c.dom.Element element = xsd.createElementNS(ns, prefix + "element");            element.setAttribute("name", data.getName());            org.w3c.dom.Element complex = xsd.createElementNS(ns, prefix + "complexType");            org.w3c.dom.Element sequence = xsd.createElementNS(ns, prefix + "sequence");            ArrayList varList = new ArrayList(vars);            Collections.sort(varList);               // sort on YParameter ordering value            for(Object obj : varList)            {                YVariable var = (YVariable) obj;                org.w3c.dom.Element child = xsd.createElementNS(ns, prefix + "element");                child.setAttribute("name", var.getName());                String type = var.getDataTypeName();                if (XSDType.getInstance().isBuiltInType(type))                {                    type = prefix + type;                }                child.setAttribute("type", type);                child.setAttribute("minOccurs", "0");                sequence.appendChild(child);            }            complex.appendChild(sequence);            element.appendChild(complex);            xsd.getDocumentElement().appendChild(element);            SchemaHandler handler =                          new SchemaHandler(DOMUtil.getXMLStringFragmentFromNode(xsd));            if(!handler.compileSchema())            {                throw new YDataValidationException(                    handler.getSchema(),                    data,                    handler.getConcatenatedMessage(),                    source,                    "Problem with process model.  Failed to compile schema");            }            if(!handler.validate(JDOMUtil.elementToString(data)))            {                throw new YDataValidationException(                    handler.getSchema(),                    data,                    handler.getConcatenatedMessage(),                    source,                    "Problem with process model.  Schema validation failed");            }        }        catch (Exception e)        {            if(e instanceof YDataValidationException) throw (YDataValidationException) e;        }    }    /**     * @return String representation of the schema     */    public String getSchema()    {        return handler.getSchema();    }    /**     * @return All error/warning messages relating to the last validation/compilation     */    public Vector<String> getMessages()    {        return handler.getMessages();    }    /**     * Utility method to ensure the prefix is valid (enforces : and     * defaults to xs:)     *     * @param prefix to validate     * @return validated prefix     */    private String ensureValidPrefix(String prefix)    {        if(prefix == null || prefix.trim().length() == 0)        {            return "xs:";        }        else if (!prefix.endsWith(":"))        {            return prefix + ":";        }        return prefix;    }    /**     * A schema may not have a valid prefix if a spec contains no complex types, so     * this makes sure it gets one in that case     * @param schema the schema string to check     * @return a correctly (or defaultly) prefixed schema string     */    private String ensurePrefixedSchema(String schema) {        if (schema.indexOf(":schema") == -1) {            schema = schema.replaceFirst("schema xmlns", "schema xmlns:xs");            schema = schema.replaceAll("<", "<xs:")                           .replaceAll("<xs:/", "</xs:")                           .replaceAll("type=\"", "type=\"xs:");        }            return schema ;    }}