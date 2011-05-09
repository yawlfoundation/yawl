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

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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
            Element specificationSetElem, Namespace ns, String version)
            throws YSyntaxException {
        List<YSpecification> specifications = new Vector<YSpecification>();
        List specificationElemList = specificationSetElem.getChildren("specification", ns);

        // parse each specification element into a YSpecification
        for (Object o : specificationElemList) {
            Element xmlSpecification = (Element) o;

            YSpecificationParser specParser =
                    new YSpecificationParser(xmlSpecification, version);
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

        //first check if the xml string is well formed and build a document
        Document document = JDOMUtil.stringToDocument(specStr);
        if (document != null) {
            Element specificationSetEl = document.getRootElement();
            String version = getVersion(specificationSetEl);
            Namespace ns = specificationSetEl.getNamespace();

            // strip layout element, if any (the engine doesn't use it)
            specificationSetEl.removeChild("layout", ns);

            //now check the specification file against its respective schema
            if (schemaValidate) {
                String errors = YawlXMLSpecificationValidator.getInstance()
                              .checkSchema(JDOMUtil.documentToString(document), version);
                if (errors == null || errors.length() > 0) {
                    throw new YSyntaxException(
                      " The specification file failed to verify against YAWL's Schema:\n"
                      + errors);
                }
            }

            //now build a set of specifications - verification has not yet occured.
            result = buildSpecifications(specificationSetEl, ns, version);
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
    public static String marshal(List<YSpecification> specificationList, String version) {

        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                          "<specificationSet version=\"%s\" xmlns=\"%s\" " +
                          "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                          "xsi:schemaLocation=\"%s\">";

        StringBuilder xml = new StringBuilder();

        // generate version-specific header
        if (version.startsWith("Beta")) {
            xml.append(String.format(header, YSpecification.Beta7_1,
                                             YSpecification.BetaNS,
                                             YSpecification.BetaSchemaLocation));
        }
        else {                                            // version 2.0 or greater
            xml.append(String.format(header, version,
                                             YSpecification.Version2NS,
                                             YSpecification.Version2SchemaLocation));
        }

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
        String version = specification.getSchemaVersion();
        List<YSpecification> spLst = new ArrayList<YSpecification>();
        spLst.add(specification);
        return marshal(spLst, version);
    }


    /**
     * Gets the specification's schema version form its root Element
     * @param specRoot the root Element of a specification set Document
     * @return the specification's schema version
     */
    private static String getVersion(Element specRoot) {
        String version = specRoot.getAttributeValue("version");

        // version attribute was not mandatory in version 2
        // therefore a missing version number would likely be version 2
        if (null == version) {
            version = YSpecification.Beta2;
        }
        return version;
    }

}
