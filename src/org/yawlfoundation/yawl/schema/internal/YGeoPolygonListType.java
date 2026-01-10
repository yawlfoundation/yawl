/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.schema.internal;

import org.jdom2.Element;

/**
 * Defines a data type for a document (id and string)
 *
 * @author: Michael Adams
 */

class YGeoPolygonListType extends YGeoLatLongType implements YDataType {

    private static final String SCHEMA =
            "\n\t<xs:complexType name=\"YGeoPolygonListType\">\n" +
                    "\t\t<xs:sequence>\n" +
                    "\t\t\t<xs:element name=\"polygon\"" +
                    " maxOccurs=\"unbounded\">\n" +
                    "\t\t\t\t<xs:complexType>\n" +
                    "\t\t\t\t\t<xs:sequence>\n" +
                    "\t\t\t\t\t\t<xs:element name=\"vertex\" minOccurs=\"2\" maxOccurs=\"unbounded\"/>\n" +
                    "\t\t\t\t\t\t\t<xs:complexType>\n" +
                    INNER_SCHEMA_STRING +
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
        Element ePolygon = addElement(sequence, "element");
        ePolygon.setAttribute("name", "polygon");
        ePolygon.setAttribute("maxOccurs", "unbounded");

        Element complex2 = addElement(ePolygon, "complexType");
        Element sequence2 = addElement(complex2, "sequence");
        Element eVertex = addElement(sequence2, "element");
        eVertex.setAttribute("name", "vertex");
        eVertex.setAttribute("minOccurs", "2");
        eVertex.setAttribute("maxOccurs", "unbounded");
        addBaseSchema(eVertex);

        return element;
    }
    
}