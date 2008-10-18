/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.unmarshal;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.yawlfoundation.yawl.elements.YSpecification;
import org.yawlfoundation.yawl.exceptions.YSchemaBuildingException;
import org.yawlfoundation.yawl.exceptions.YSyntaxException;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * 
 * @author Lachlan Aldred
 * 
 */
public class YMarshal {

    /**
     * build a list of specification objects from a XML document.
     * @param specificationSetDoc the JDom element con
     * @return a list of YSpecification objects taken from the XML document.
     */
    private static List<YSpecification> buildSpecifications(Document specificationSetDoc) throws YSchemaBuildingException, YSyntaxException {
        Element specificationSetElem = specificationSetDoc.getRootElement();
        Namespace ns = specificationSetElem.getNamespace();
        String version = specificationSetElem.getAttributeValue("version");
        if (null == version) {
            //version attribute was not mandatory in version 2
            //therefore a missing version number would likely be version 2
            version = YSpecification._Beta2;
        }
        List<YSpecification> specifications = new Vector<YSpecification>();
        List specificationElemList = specificationSetElem.getChildren("specification", ns);
        for (int i = 0; i < specificationElemList.size(); i++) {
            Element xmlSpecification = (Element) specificationElemList.get(i);

            YSpecificationParser specParse = new YSpecificationParser(xmlSpecification, version);
            YSpecification spec = specParse.getSpecification();
            specifications.add(spec);
        }
        return specifications;
    }



    public static List<YSpecification> unmarshalSpecifications(String specStr)
        throws YSyntaxException, YSchemaBuildingException, JDOMException, IOException {
        return unmarshalSpecifications(specStr, true) ;
    }

    /**
     * Returns the _specifications. Does some primary checking of the file against
     * schemas and checks well formedness of the XML.
     * @return List
     */
    public static List<YSpecification> unmarshalSpecifications(String specStr, boolean schemaValidate)
            throws YSyntaxException, YSchemaBuildingException, JDOMException, IOException {
        //first check if is well formed and build a document
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(new StringReader(specStr));
        Element specificationSetEl = document.getRootElement();

        //next get the version out as text - if possible
        String version = specificationSetEl.getAttributeValue("version");
        if (null == version) {
            //version attribute was not mandatory in version 2
            //therefore a missing version number would likely be version 2
            version = YSpecification._Beta2;
        }

        //now check the specification file against its' respective schema
        if (schemaValidate) {
            String errors = YawlXMLSpecificationValidator.getInstance().checkSchema(specStr, version);
            if (errors == null || errors.length() > 0) {
                throw new YSyntaxException(
                    " The specification file failed to verify against YAWL's Schema:\n"
                    + errors);
            }
        }

        //now build a set of specifications - verification has not yet occured.
        return buildSpecifications(document);
    }


    public static String marshal(List specificationList, String version)
                                                   throws IOException, JDOMException {
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");

        // if a beta version
        if (version.startsWith("Beta")) {
            xml.append("<specificationSet " +
                 "version=\"" + YSpecification._Beta7_1 + "\" " +
                 "xmlns=\"http://www.citi.qut.edu.au/yawl\" " +
                 "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                 "xsi:schemaLocation=\"http://www.citi.qut.edu.au/yawl " +
                 "d:/yawl/schema/YAWL_SchemaBeta7.1.xsd\">");
        }

        // else if version 2.0 or greater
        else {
            xml.append("<specificationSet " +
                "version=\"" + YSpecification._Version2_0 + "\" " +
                "xmlns=\"http://www.yawlfoundation.org/yawlschema\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xsi:schemaLocation=\"http://www.yawlfoundation.org/yawlschema " +
                "http://www.yawlfoundation.org/yawlschema/YAWL_Schema2.0.xsd\">");
        }
        for (int i = 0; i < specificationList.size(); i++) {
            YSpecification specification = (YSpecification) specificationList.get(i);
            xml.append(specification.toXML());
        }
        xml.append("</specificationSet>");

        return JDOMUtil.formatXMLStringAsDocument(xml.toString());
//        SAXBuilder builder = new SAXBuilder();
//        Document finalDoc = null;
//
//        finalDoc = builder.build(new StringReader(xml.toString()));
//        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
//        return out.outputString(finalDoc);
    }


    public static String marshal(YSpecification specification) throws IOException, JDOMException {
        String version = specification.getSchemaVersion();
        List spLst = new ArrayList();
        spLst.add(specification);
        return marshal(spLst, version);
    }


//    public static void main(String[] args) throws IOException, YSchemaBuildingException, YSyntaxException, JDOMException {
//        URL xmlFileURL = YMarshal.class.getResource("MakeRecordings.xml");
//        File file = new File(xmlFileURL.getFile());
//        List specifications = unmarshalSpecifications(file.getCanonicalPath());
//        String marshalledSpecs = marshal(specifications, YSpecification._Beta7_1);
//    }
}
