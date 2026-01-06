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
class YGeoLatLongType implements YDataType {

    protected static final String INNER_SCHEMA_STRING =
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

    private static final String SCHEMA =
            "\n\t<xs:complexType name=\"YGeoLatLong\">\n" +
                    INNER_SCHEMA_STRING  + "\t</xs:complexType>\n";


    public String getSchemaString() { return SCHEMA; }


    public Element getSchema(String name) {
        Element element = new Element("element", YAWL_NAMESPACE);
        element.setAttribute("name", name);
        addBaseSchema(element);
        return element;
    }


    protected void addBaseSchema(Element element) {
        Element complex = addElement(element, "complexType");
        Element sequence = addElement(complex, "sequence");

        addSimpleTypeElement(sequence, "latitude", "-90", "90");
        addSimpleTypeElement(sequence, "longitude", "-180", "180");
    }


    protected void addSimpleTypeElement(Element sequence, String name, String min, String max) {
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