/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.miscellaneousPrograms;





/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 18/06/2004
 * Time: 15:16:12
 * 
 */
public class TestPSVI {
/*
    private static String document =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<document xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
            "</document>";


    public static void main(String[] args) throws Error{

        DocumentBuilderFactory dbf = new DocumentBuilderFactory.newInstance();

//dbf is a JAXP DocumentBuilderFactory

// all of the following features must be set:
        dbf.setNamespaceAware(true);
        dbf.setValidating(true);
        dbf.setAttribute("http://apache.org/xml/features/validation/schema",
            Boolean.TRUE);

// you also must specify Xerces PSVI DOM implementation
// "org.apache.xerces.dom.PSVIDocumentImpl"
        dbf.setAttribute("http://apache.org/xml/properties/dom/document-class-name",
           "org.apache.xerces.dom.PSVIDocumentImpl");
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document doc = null;
        try {
            doc = db.parse(new InputSource(new StringReader(document)));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Element documentElement = doc.getDocumentElement() ;
        if (documentElement.isSupported("psvi", "1.0")){
            ElementPSVI psviElem = (ElementPSVI)doc.getDocumentElement();
            XSModel model = psviElem.getSchemaInformation();
            XSElementDeclaration decl = psviElem.getElementDeclaration();

        }


    }
*/
}
