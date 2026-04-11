package org.yawlfoundation.yawl.schema.internal;

import org.jdom2.Element;

/**
 *
 * @author Michael Adams
 * @date 23/1/2026
 */
abstract class YGeoType implements YDataType {

    protected static String LATLONG_SCHEMA_STRING =
            "\t\t<xs:sequence>\n" +
                    "\t\t\t<xs:element name=\"latitude\">\n" +
                    "\t\t\t\t<xs:simpleType>\n" +
                    "\t\t\t\t\t<xs:restriction base=\"xs:decimal\">\n" +
                    "\t\t\t\t\t\t<xs:minInclusive value=\"-90\"/>\n" +
                    "\t\t\t\t\t\t<xs:maxInclusive value=\"90\"/>\n" +
                    "\t\t\t\t\t</xs:restriction>\n" +
                    "\t\t\t\t</xs:simpleType>\n" +
                    "\t\t\t</xs:element>\n" +
                    "\t\t\t<xs:element name=\"longitude\">\n" +
                    "\t\t\t\t<xs:simpleType>\n" +
                    "\t\t\t\t\t<xs:restriction base=\"xs:decimal\">\n" +
                    "\t\t\t\t\t\t<xs:minInclusive value=\"-180\"/>\n" +
                    "\t\t\t\t\t\t<xs:maxInclusive value=\"180\"/>\n" +
                    "\t\t\t\t\t</xs:restriction>\n" +
                    "\t\t\t\t</xs:simpleType>\n" +
                    "\t\t\t</xs:element>\n" +
                    "\t\t</xs:sequence>\n";


    abstract void addInnerSchema(Element parent);

    
    public Element getSchema(String name) {
        Element element = new Element("element", YAWL_NAMESPACE);
        element.setAttribute("name", name);
        addInnerSchema(element);
        return element;
    }


    protected void addLatLongSchema(Element parent) {
        Element complex = addElement(parent, "complexType");
        Element sequence = addElement(complex, "sequence");

        addSimpleTypeElement(sequence, "latitude", "-90", "90");
        addSimpleTypeElement(sequence, "longitude", "-180", "180");
    }


    protected Element addLabelElement(Element parent) {
        Element eLabel = addElement(parent, "element");
        eLabel.setAttribute("name", "label");
        eLabel.setAttribute("type", "xs:string");
        eLabel.setAttribute("minOccurs", "0");
        return eLabel;
    }


    private void addSimpleTypeElement(Element sequence, String name, String min, String max) {
        Element e = addElement(sequence, "element");
        e.setAttribute("name", name);
        Element eSimple = addElement(e, "simpleType");
        Element eRestriction = addElement(eSimple, "restriction");
        eRestriction.setAttribute("base", "xs:decimal");
        Element eMinInclusive = addElement(eRestriction, "minInclusive");
        eMinInclusive.setAttribute("value", min);
        Element eMaxInclusive = addElement(eRestriction, "maxInclusive");
        eMaxInclusive.setAttribute("value", max);
    }
}
