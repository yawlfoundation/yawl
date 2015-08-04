package org.yawlfoundation.yawl.schema.internal;

import org.jdom2.Element;

/**
 * Defines a data type for a document (id and string)
 *
 * @author: Michael Adams
 * @date: 04/2012
 */
class YDocumentType implements YDataType {

    private static final String SCHEMA =
            "\n\t<xs:complexType name=\"YDocumentType\">\n" +
                    "\t\t<xs:sequence>\n" +
                    "\t\t\t<xs:element name=\"id\" type=\"xs:long\" minOccurs=\"0\"/>\n" +
                    "\t\t\t<xs:element name=\"name\" type=\"xs:string\"/>\n" +
                    "\t\t</xs:sequence>\n" +
                    "\t</xs:complexType>\n";


    public String getSchemaString() { return SCHEMA; }


    public Element getSchema(String name) {
        Element element = new Element("element", YAWL_NAMESPACE);
        element.setAttribute("name", name);

        Element complex = addElement(element, "complexType");
        Element sequence = addElement(complex, "sequence");
        Element id = addElement(sequence, "element");
        id.setAttribute("name", "id");
        id.setAttribute("type", "xs:long");
        id.setAttribute("minOccurs", "0");

        Element eName = addElement(sequence, "element");
        eName.setAttribute("name", "name");
        eName.setAttribute("type", "xs:string");

        return element;
    }

    private Element addElement(Element parent, String name) {
        Element element = new Element(name, YAWL_NAMESPACE);
        parent.addContent(element);
        return element;
    }

}