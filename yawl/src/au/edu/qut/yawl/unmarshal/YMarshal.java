/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.unmarshal;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
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
    private static List buildSpecifications(Document specificationSetDoc) throws YSchemaBuildingException, YSyntaxException {
        Element specificationSetElem = specificationSetDoc.getRootElement();
        String version = specificationSetElem.getAttributeValue("version");
        if (null == version) {
            //version attribute was not mandatory in version 2
            //therefore a missing version number would likely be version 2
            version = YSpecification._Beta2;
        }
        List specifications = new Vector();
        List specificationElemList = specificationSetElem.getChildren();
        for (int i = 0; i < specificationElemList.size(); i++) {
            Element xmlSpecification = (Element) specificationElemList.get(i);

            YSpecificationParser specParse = new YSpecificationParser(xmlSpecification, version);
            YSpecification spec = specParse.getSpecification();
            specifications.add(spec);
        }
        return specifications;
    }


    /**
     * Returns the _specifications.   Does some primary checking of the file against
     * schemas and checks well formedness of the XML.
     * @return List
     */
    public static List unmarshalSpecifications(String specificationSetFileID)
            throws YSyntaxException, YSchemaBuildingException, JDOMException, IOException {
        //first check if is well formed and build a document
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(specificationSetFileID);
        Element specificationSetEl = document.getRootElement();

        //next get the version out as text - if possible
        String version = specificationSetEl.getAttributeValue("version");
        if (null == version) {
            //version attribute was not mandatory in version 2
            //therefore a missing version number would likely be version 2
            version = YSpecification._Beta2;
        }

        //now check the specification file against its' respective schema
        String errors = YawlXMLSpecificationValidator.getInstance().checkSchema(specificationSetFileID, version);
        if (errors == null || errors.length() > 0) {
            throw new YSyntaxException(
                    " The file [" + specificationSetFileID + "] failed to verify against YAWL's Schema:\n"
                    + errors);
        }

        //now build a set of specifications - verification has not yet occured.
        return buildSpecifications(document);
    }


    public static String marshal(List specificationList) throws IOException, JDOMException {
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        xml.append("<specificationSet " +
                "version=\"" + YSpecification._Beta7_1 + "\" " +
                "xmlns=\"http://www.citi.qut.edu.au/yawl\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xsi:schemaLocation=\"http://www.citi.qut.edu.au/yawl " +
                "d:/yawl/schema/YAWL_SchemaBeta7.1.xsd\">");
        for (int i = 0; i < specificationList.size(); i++) {
            YSpecification specification = (YSpecification) specificationList.get(i);
            xml.append(specification.toXML());
        }
        xml.append("</specificationSet>");

        SAXBuilder builder = new SAXBuilder();
        Document finalDoc = null;
        finalDoc = builder.build(new StringReader(xml.toString()));
        XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
        return out.outputString(finalDoc);

    }


    public static String marshal(YSpecification specification) throws IOException, JDOMException {
        List spLst = new ArrayList();
        spLst.add(specification);
        return marshal(spLst);
    }


    public static void main(String[] args) throws IOException, YSchemaBuildingException, YSyntaxException, JDOMException {
        URL xmlFileURL = YMarshal.class.getResource("MakeRecordings.xml");
        File file = new File(xmlFileURL.getFile());
        List specifications = unmarshalSpecifications(file.getCanonicalPath());
        String marshalledSpecs = marshal(specifications);
        System.out.println("\n\n" + marshalledSpecs);
    }
}
