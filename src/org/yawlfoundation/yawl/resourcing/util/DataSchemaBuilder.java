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

package org.yawlfoundation.yawl.resourcing.util;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.engine.interfce.TaskInformation;
import org.yawlfoundation.yawl.schema.XSDType;
import org.yawlfoundation.yawl.schema.internal.YInternalType;
import org.yawlfoundation.yawl.util.JDOMUtil;

import javax.xml.XMLConstants;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Expands a data schema for a case start or a task to its base xsd types for use in the
 * definition of a dynamic form.
 *
 * Author: Michael Adams
 * Creation Date: 5/04/2010
 */
public class DataSchemaBuilder {

    // a map of user defined type names to their definitions
    private Map<String, Element> _schemaMap;

    // the set of namespaces used by this schema
    private Map<String, Namespace> _nsList;


    /**
     * The constructor.
     * @param schemaMap a map of user defined types of this particular specification
     */
    public DataSchemaBuilder(Map<String, Element> schemaMap) {
        _schemaMap = schemaMap;
        _nsList = new Hashtable<String, Namespace>();
    }


    /**
     * Constructs the expanded data schema, using the user-defined-types map passed
     * in via the constructor
     * @param specData the SpecificationData object for the spec to construct a schema for
     * @return the constructed schema (as a string)
     */   
    public String build(SpecificationData specData) {
        return buildSchema(specData.getRootNetID(), specData.getInputParams()) ;
    }


    /**
     * Constructs the expanded data schema, using the user-defined-types map passed
     * in via the constructor
     * @param taskInfo the TaskInformation object for the task to construct a schema for
     * @return the constructed schema (as a string)
     */
    public String build(TaskInformation taskInfo) {
        return buildSchema(taskInfo.getDecompositionID(),
                taskInfo.getParamSchema().getCombinedParams()) ;
    }


    /**
     * Constructs the expanded data schema, using the user-defined-types map passed
     * in via the constructor.
     * @param rootName the name of the root element (task or root net name)
     * @param parameters the List of parameters to build the schema for
     * @return the constructed schema (as a string)
     */
    public String buildSchema(String rootName, List<? extends YVariable> parameters) {
        Namespace defNS = getDefaultNamespace();

        // create a new schema doc preamble (down to first sequence element)
        Element sequence = createPreamble(rootName, defNS);

        // for each param build an appropriate element
        for (YVariable param : parameters) {
             sequence.addContent(createParamElement(param,  defNS));
        }

        return completeSchema(sequence.getDocument());
    }


    /**
     * Constructs a data schema for a single variable name and data type
     * @param rootName the name to give to the root element
     * @param varName the name to give to the data element
     * @param dataType the datatype for the data element
     * @return a schema for the datatype that variables of the name supplied can be
     * validated against
     */
    public String buildSchema(String rootName, String varName, String dataType) {
        Namespace defNS = getDefaultNamespace();

        // create a new schema doc preamble (down to first sequence element)
        Element sequence = createPreamble(rootName, defNS);

        // build an appropriate element for the data type
        sequence.addContent(createDataTypeElement(varName, dataType, defNS));

        return completeSchema(sequence.getDocument());
    }


    private Element createPreamble(String rootName, Namespace defNS) {

        // create a new doc with a root element called 'schema'
        Element root = new Element("schema", defNS);
        root.setAttribute("elementFormDefault", "qualified");
        new Document(root);     // attaches a default doc as parent of root element

        // attach an element set to the supplied root name
        Element taskElem = new Element("element", defNS);
        taskElem.setAttribute("name", rootName);
        root.addContent(taskElem);

        Element complex = new Element("complexType", defNS);
        Element sequence = new Element("sequence", defNS);
        taskElem.addContent(complex);
        complex.addContent(sequence);

        return sequence;
    }


    private String completeSchema(Document doc) {

        // add all the namespaces referred to in the schema to the root element
        for (Namespace ns : _nsList.values()) {
            doc.getRootElement().addNamespaceDeclaration(ns);
        }

        return JDOMUtil.documentToString(doc);
    }

    /**
     * Constructs a schema element for a parameter
     * @param param the parameter to construct a schema for
     * @param defNS the default namespace
     * @return the constructed schema for this parameter
     */
    private Element createParamElement(YVariable param,  Namespace defNS) {
        Element element = new Element("element", defNS);
        element.setAttribute("name", param.getName());

        // simple types are defined by attribute, user-defined types are defined by
        // sub elements
        String dataType = param.getDataTypeNameUnprefixed();
        if (isXSDType(dataType)) {
            element.setAttribute("type", prefix(dataType, defNS));
        }
        else if (YInternalType.isType(dataType)) {
            element = YInternalType.getSchemaFor(dataType, param.getName());
        }
        else {
            element = createComplexType(param, element, defNS);
        }

        // set default min and max occurs for this parameter
        element.setAttribute("minOccurs", param.isOptional() ? "0" : "1");
        element.setAttribute("maxOccurs", "1");

        return element;
    }


    private Element createDataTypeElement(String varName, String dataType, Namespace defNS) {

        // internal types already have a definition 
        if (YInternalType.isType(dataType)) {
            return YInternalType.getSchemaFor(dataType, varName);
        }

        Element element = new Element("element", defNS);
        element.setAttribute("name", varName);

        // simple types are defined by attribute, user-defined types are defined by
        // sub elements
        if (isXSDType(dataType)) {
            element.setAttribute("type", prefix(dataType, defNS));
        }
        else {
            element = cloneUserDefinedType(element, dataType, defNS);
        }

        return element;
    }

    /**
     * Constructs a new complex type schema for a parameter
     * @param param the parameter with a user-defined type definition
     * @param base the base parameter element
     * @param defNS the default namespace
     * @return the constructed complex type schema
     */
    private Element createComplexType(YVariable param, Element base, Namespace defNS) {
        String URI = param.getDataTypeNameSpace();
        if (URI != null) {
            addNamespace(URI, param.getDataTypePrefix()) ;     // add any new namespace
        }
        return cloneUserDefinedType(base, param.getDataTypeName(), defNS);
    }


    /**
     * Clones a user-defined type definition to use within a schema. May be called
     * recursively.
     * @param base the 'parent' element this type is defined for
     * @param type the user-defined type name
     * @param defNS the default namespace
     * @return the user defined type definiton as a schema 
     */
    private Element cloneUserDefinedType(Element base, String type, Namespace defNS) {
        Element udType = new Element("element", defNS);
        udType.setAttribute("name", base.getAttributeValue("name"));
        Element typeDefn = _schemaMap.get(type);

        // set the first element name to the name of the udt's element (eg. sequence)
        Element parent = new Element(typeDefn.getName(), defNS);
        cloneContent(parent, typeDefn, defNS);
        udType.addContent(parent);

        // set min & max occurs for this element (if defined)
        String minOccurs = base.getAttributeValue("minOccurs");
        String maxOccurs = base.getAttributeValue("maxOccurs");
        if (minOccurs != null) udType.setAttribute("minOccurs", minOccurs);
        if (maxOccurs != null) udType.setAttribute("maxOccurs", maxOccurs);
        
        return udType;
    }


    /**
     * Clones the content of a user-defined type. May be called recursively.
     * @param base the 'parent' element this content will form the schema for
     * @param toCopy the user defined type definition
     * @param defNS the default namespace
     * @return the cloned content
     */
    private Element cloneContent(Element base, Element toCopy, Namespace defNS) {
        Element newChild;

        for (Element child : toCopy.getChildren()) {
            String type = getTypeNameUnprefixed(child);
            if (type != null) {

                // if there's a type attribute and its a simple type, set it for the clone
                if (isXSDType(type)) {
                    newChild = cloneElement(child, defNS);
                    newChild.setAttribute("type", prefix(type, defNS));
                }

                // if its a udt, recurse to define the udt subtype
                else {
                    newChild = cloneUserDefinedType(child, type, defNS);
                }
            }
            else {
                newChild = cloneElement(child, defNS);
            }

            // recurse to process this element's children (til there are no more children)
            cloneContent(newChild, child, defNS);

            base.addContent(newChild);
        }
        return base;
    }


    /**
     * Creates a new element with the same name and attributes as the old one
     * @param element the elment to clone
     * @param defNS the default namespace
     * @return the cloned element
     */
    private Element cloneElement(Element element, Namespace defNS) {
        Element cloned = new Element(element.getName(), defNS);
        cloned.setAttributes(cloneAttributes(element, defNS));
        return cloned;
    }


    /**
     * Clones a set of attributes. Needs to be done this way to (i) break the
     * parental attachment to the attribute; and (ii) to fix any errant namespace
     * prefixes
     * @param element the element with the attributes to clone
     * @param defNS the default namespace
     * @return the List of clone attributes
     */
    private List<Attribute> cloneAttributes(Element element, Namespace defNS) {
        String prefix = element.getNamespacePrefix();
        List<Attribute> cloned = new ArrayList<Attribute>();
        for (Attribute attribute : element.getAttributes()) {
            String value = getAttributeValue(attribute, prefix, defNS);
            Attribute copy = new Attribute(attribute.getName(), value);
            cloned.add(copy);
        }
        return cloned;
    }


    /**
     * Gets an attribute value, fixing its namespace prefix (if any)
     * @param attribute the attribute to get the value for
     * @param prefix the correct namespace prefix
     * @param defNS the default namespace
     * @return the attribute's value with the prefix corrected if required
     */
    private String getAttributeValue(Attribute attribute, String prefix, Namespace defNS) {
        String value = attribute.getValue();
        if (prefix.length() > 0) {

            // adding ':' to handle union member types
            value = value.replace(prefix + ":", defNS.getPrefix() + ":");
        }
        return value;
    }


    /**
     * Checks that a type name is one of the XSD base types
     * @param type the type name to check
     * @return true if it is one of the base XSD type names
     */
    private boolean isXSDType(String type) {
        return XSDType.isBuiltInType(type);
    }


    /**
     * Attaches a prefix to a type name
     * @param type the type name
     * @param ns the namespace
     * @return the type name prefixed with the namespace's prefix
     */
    private String prefix(String type, Namespace ns) {
        return String.format("%s:%s", ns.getPrefix(), type);
    }


    /**
     * Gets the value of an element's type attribute with the namespace prefix removed
     * @param e the element with the type attribute
     * @return the unprefixed type name
     */
    private String getTypeNameUnprefixed(Element e) {
        String typeName = e.getAttributeValue("type");
        if (typeName != null) {
            int pos = typeName.indexOf(":");
            if (pos > -1) {
                typeName = typeName.substring(pos + 1);
            }
        }
        return typeName;
    }


    /**
     * Creates and stores the default XSD namespace
     * @return the default XSD namespace
     */
    private Namespace getDefaultNamespace() {
        Namespace ns = Namespace.getNamespace("xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
        _nsList.put(XMLConstants.W3C_XML_SCHEMA_NS_URI, ns);
        return ns;
    }


    /**
     * Adds a new namespace to the list of namespaces ussed by this schema
     * @param URI the namespace's URI
     * @param prefix the namespace's prefix
     * @return the namespace
     */
    private Namespace addNamespace(String URI, String prefix) {
        if ((prefix == null) || prefix.length() == 0) return null;
        if (! _nsList.containsKey(URI)) {
            _nsList.put(URI, Namespace.getNamespace(prefix, URI));
        }
        return _nsList.get(URI);
    }

}
