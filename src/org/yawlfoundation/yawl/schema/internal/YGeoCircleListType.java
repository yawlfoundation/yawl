package org.yawlfoundation.yawl.schema.internal;

import org.jdom2.Element;

/**
 *
 * @author Michael Adams
 * @date 8/1/2026
 */
public class YGeoCircleListType extends YGeoLatLongType implements YDataType{

    private static final String SCHEMA =
            "\n\t<xs:complexType name=\"YGeoCircleListType\">\n" +
                    "\t\t<xs:sequence>\n" +
                    "\t\t\t<xs:element name=\"circle\"" +
                    " maxOccurs=\"unbounded\">\n" +
                    "\t\t\t\t<xs:complexType>\n" +
                    "\t\t\t\t\t<xs:sequence>\n" +
                    "\t\t\t\t\t\t<xs:element name=\"center\">\n" +
                    "\t\t\t\t\t\t\t<xs:complexType>\n" +
                    INNER_SCHEMA_STRING +
                    "\t\t\t\t\t\t\t</xs:complexType>\n" +
                    "\t\t\t\t\t\t</xs:element>\n" +
                    "\t\t\t\t\t\t<xs:element name=\"radius\" type=\"xs:float\"/>\n" +
                    "\t\t\t\t\t</xs:sequence>\n" +
                    "\t\t\t\t</xs:complexType>\n" +
                    "\t\t\t</xs:element>\n" +
                    "\t\t</xs:sequence>\n" +
                    "\t</xs:complexType>\n";


    public String getSchemaString() { return SCHEMA; }


    public Element getSchema(String name) {
        Element element = new Element("element", YAWL_NAMESPACE);
        element.setAttribute("name", name);

        Element complex = addElement(element, "complexType");
        Element sequence = addElement(complex, "sequence");
        Element eCircle = addElement(sequence, "element");
        eCircle.setAttribute("name", "circle");
        eCircle.setAttribute("maxOccurs", "unbounded");

        Element complex2 = addElement(eCircle, "complexType");
        Element sequence2 = addElement(complex2, "sequence");

        Element eCentre = addElement(sequence2, "element");
        eCentre.setAttribute("name", "center");
        addBaseSchema(eCentre);

        Element eRadius = addElement(sequence2, "element");
        eRadius.setAttribute("name", "radius");
        eRadius.setAttribute("type", "xs:float");

        return element;
    }

}
