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

class YGeoRectType extends YGeoType implements YDataType {

    private static final String SCHEMA =
            "\n\t<xs:complexType" + YDataType.getNameSpaceStrings() + "name=\"YGeoRectType\">\n" +
                    "\t\t<xs:sequence>\n" +
                    "\t\t\t<xs:element name=\"label\" type=\"xs:string\" minOccurs=\"0\"/>\n" +
                    "\t\t\t<xs:element name=\"top-left\">\n" +
                    "\t\t\t\t<xs:complexType>\n" +
                    LATLONG_SCHEMA_STRING +
                    "\t\t\t\t</xs:complexType>\n" +
                    "\t\t\t</xs:element>\n" +
                    "\t\t\t<xs:element name=\"bottom-right\">\n" +
                    "\t\t\t\t<xs:complexType>\n" +
                    LATLONG_SCHEMA_STRING +
                    "\t\t\t\t</xs:complexType>\n" +
                    "\t\t\t</xs:element>\n" +
                    "\t\t</xs:sequence>\n" +
                    "\t</xs:complexType>\n";
    
    public String getSchemaString() { return SCHEMA; }

    
    @Override
    void addInnerSchema(Element parent) {
        Element complex = addElement(parent, "complexType");
        Element sequence = addElement(complex, "sequence");

        addLabelElement(sequence);

        Element p1 = addElement(sequence, "element");
        p1.setAttribute("name", "top-left");
        addLatLongSchema(p1);

        Element p2 = addElement(sequence, "element");
        p2.setAttribute("name", "bottom-right");
        addLatLongSchema(p2);
    }

}