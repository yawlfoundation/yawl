package org.yawlfoundation.yawl.schema.internal;

import org.jdom2.Element;

/**
 *
 * @author Michael Adams
 * @date 8/1/2026
 */
public class YGeoLatLongListType extends YGeoLatLongType implements YDataType{

    private static final String SCHEMA =
            "\n\t<xs:complexType name=\"YGeoLatLongListType\">\n" +
                    "\t\t<xs:sequence>\n" +
                    "\t\t\t<xs:element name=\"point\"" +
                    " maxOccurs=\"unbounded\">\n" +
                    "\t\t\t\t<xs:complexType>\n" +
                    INNER_SCHEMA_STRING +
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

        Element ePoint = addElement(sequence, "element");
        ePoint.setAttribute("name", "point");
        ePoint.setAttribute("maxOccurs", "unbounded");
        addBaseSchema(ePoint);
        
        return element;
    }

}
