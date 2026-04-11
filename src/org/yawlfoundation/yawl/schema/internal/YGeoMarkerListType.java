package org.yawlfoundation.yawl.schema.internal;

import org.jdom2.Element;

/**
 *
 * @author Michael Adams
 * @date 8/1/2026
 */
public class YGeoMarkerListType extends YGeoMarkerType implements YDataType{

    private static final String SCHEMA =
            "\n\t<xs:complexType" + YDataType.getNameSpaceStrings() + "name=\"YGeoMarkerListType\">\n" +
                    "\t\t<xs:sequence>\n" +
                    "\t\t\t<xs:element name=\"marker\"" +
                    " maxOccurs=\"unbounded\">\n" +
                    "\t\t\t\t<xs:complexType>\n" +
                    "\t\t\t\t\t<xs:sequence>\n" +
                    "\t\t\t\t\t\t<xs:element name=\"label\" type=\"xs:string\" minOccurs=\"0\"/>\n" +
                    "\t\t\t\t\t\t<xs:element name=\"point\">\n" +
                    "\t\t\t\t\t\t\t<xs:complexType>\n" +
                    LATLONG_SCHEMA_STRING +
                    "\t\t\t\t\t\t\t</xs:complexType>\n" +
                    "\t\t\t\t\t\t</xs:element>\n" +
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
        
        Element eMarker = addElement(sequence, "element");
        eMarker.setAttribute("name", "marker");
        eMarker.setAttribute("maxOccurs", "unbounded");

        addInnerSchema(eMarker);

        return element;
    }

}
