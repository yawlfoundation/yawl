package org.yawlfoundation.yawl.schema.internal;

import org.jdom2.Element;

/**
 * Defines a data structure for a timer
 *
 * @author: Michael Adams
 * @date: 16/06/2008
 */
class YTimerType implements YDataType {

    private static final String SCHEMA =
            "\n\t<xs:complexType name=\"YTimerType\">\n" +
                    "\t\t<xs:sequence>\n" +
                    "\t\t\t<xs:element name=\"trigger\">\n" +
                    "\t\t\t\t<xs:simpleType>\n" +
                    "\t\t\t\t\t<xs:restriction base=\"xs:string\">\n" +
                    "\t\t\t\t\t\t<xs:enumeration value=\"OnEnabled\"/>\n" +
                    "\t\t\t\t\t\t<xs:enumeration value=\"OnExecuting\"/>\n" +
                    "\t\t\t\t\t</xs:restriction>\n" +
                    "\t\t\t\t</xs:simpleType>\n" +
                    "\t\t\t</xs:element>\n" +
                    "\t\t\t<xs:element name=\"expiry\" type=\"xs:string\"/>\n" +
                    "\t\t</xs:sequence>\n" +
                    "\t</xs:complexType>\n";


    public String getSchemaString() { return SCHEMA; }


    public Element getSchema(String name) {
        Element element = new Element("element", YAWL_NAMESPACE);
        element.setAttribute("name", name);

        Element complex = addElement(element, "complexType");
        Element sequence = addElement(complex, "sequence");
        Element trigger = addElement(sequence, "element");
        trigger.setAttribute("name", "trigger");

        Element simple = addElement(trigger, "simpleType");
        Element restriction = addElement(simple, "restriction");
        restriction.setAttribute("base", "xs:string");

        Element enum1 = addElement(restriction, "enumeration");
        enum1.setAttribute("value", "OnEnabled");

        Element enum2 = addElement(restriction, "enumeration");
        enum2.setAttribute("value", "OnExecuting");

        Element expiry = addElement(sequence, "element");
        expiry.setAttribute("name", "expiry");
        expiry.setAttribute("type", "xs:string");

        return element;
    }

    private Element addElement(Element parent, String name) {
        Element element = new Element(name, YAWL_NAMESPACE);
        parent.addContent(element);
        return element;
    }

}
