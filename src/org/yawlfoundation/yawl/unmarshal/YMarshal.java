/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.schema.SchemaHandler;
import org.yawlfoundation.yawl.schema.YSchemaVersion;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Marshals & Unmarshals specifications
 *
 * @author Lachlan Aldred
 * @author Michael Adams (refactored for v2.0)
 * 
 */
public class YMarshal {

    /**
     * Builds a list of specification objects from a XML document.
     * @param specificationSetElem the JDOM Element containing the specification XML
     * @param ns specificationSetElem namespace
     * @param version the schema version of the specification set 
     * @return a list of YSpecification objects taken from the XML document.
     * @throws YSyntaxException if a parsed specification doesn't validate against
     * schema
     */
    private static List<YSpecification> buildSpecifications(
            Element specificationSetElem, Namespace ns, YSchemaVersion version)
            throws YSyntaxException {
        List<YSpecification> specifications = new ArrayList<YSpecification>();

        // parse each specification element into a YSpecification
        for (Element xmlSpecification : specificationSetElem.getChildren("specification", ns)) {
            YSpecificationParser specParser = new YSpecificationParser(xmlSpecification, version);
            specifications.add(specParser.getSpecification());
        }
        return specifications;
    }


    /**
     * Builds a list of specification objects from a XML string. This method is
     * equivalent to calling unmarshalSpecifications(String, boolean) with the
     * 'schemaValidate' flag set to true.
     * @param specStr the XML string describing the specification set
     * @return a list of YSpecification objects taken from the XML string.
     * @throws YSyntaxException if a parsed specification doesn't validate against
     * schema
     */
    public static List<YSpecification> unmarshalSpecifications(String specStr)
            throws YSyntaxException {
        return unmarshalSpecifications(specStr, true) ;
    }


    /**
     * Builds a list of specification objects from a XML string.
     * @param specStr the XML string describing the specification set
     * @param schemaValidate when true, will cause the specifications to be
     * validated against schema while being parsed
     * @return a list of YSpecification objects taken from the XML string.
     * @throws YSyntaxException if a parsed specification doesn't validate against
     * schema
     */
    public static List<YSpecification> unmarshalSpecifications(String specStr,
                                                               boolean schemaValidate)
            throws YSyntaxException {
        
        List<YSpecification> result = null;

        // first check if the xml string is well formed and build a document
        Document document = JDOMUtil.stringToDocument(specStr);
        if (document != null) {
            Element specificationSetEl = document.getRootElement();
            YSchemaVersion version = getVersion(specificationSetEl);
            Namespace ns = specificationSetEl.getNamespace();

            // strip layout element, if any (the engine doesn't use it)
            specificationSetEl.removeChild("layout", ns);

            // now check the specification file against its respective schema
            if (schemaValidate) {
                SchemaHandler validator = new SchemaHandler(version.getSchemaURL());
                if (! validator.compileAndValidate(specStr)) {
                    throw new YSyntaxException(
                      " The specification file failed to verify against YAWL's Schema:\n"
                            + validator.getConcatenatedMessage());
                }
            }

            // now build a set of specifications - verification has not yet occurred.
            result = buildSpecifications(specificationSetEl, ns, version);
        }
        else {
            throw new YSyntaxException("Invalid XML specification.");
        }
        return result;
    }


    /**
     * Builds an XML Document from a list of specifications
     * @param specificationList the list of specifications to build into an XML document
     * 'specification set'
     * @param version the appropriate schema version to use
     * @return the XML Document, rendered as a String
     */
    public static String marshal(List<YSpecification> specificationList,
                                 YSchemaVersion version) {
        StringBuilder xml = new StringBuilder(version.getHeader());
        for (YSpecification specification : specificationList) {
            xml.append(specification.toXML());
        }
        xml.append("</specificationSet>");

        return JDOMUtil.formatXMLStringAsDocument(xml.toString());
    }


    /**
     * Builds an XML Document from a specification
     * @param specification the specification to build into an XML document
     * 'specification set'
     * @return the XML Document, rendered as a String
     */
    public static String marshal(YSpecification specification) {
        List<YSpecification> spLst = new ArrayList<YSpecification>();
        spLst.add(specification);
        return marshal(spLst, specification.getSchemaVersion());
    }


    /**
     * Gets the specification's schema version form its root Element
     * @param specRoot the root Element of a specification set Document
     * @return the specification's schema version
     */
    private static YSchemaVersion getVersion(Element specRoot) {
        String version = specRoot.getAttributeValue("version");

        // version attribute was not mandatory in version 2
        // therefore a missing version number would likely be version 2
        return (null == version) ? YSchemaVersion.Beta2 : YSchemaVersion.fromString(version);
    }

}
