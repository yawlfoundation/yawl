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
 * Defines a datatype for a string list (i.e. a container for 0 or more strings)
 *
 * @author: Michael Adams
 * @date: 04/2009
 */
class YStringListType implements YDataType {

    private static final String SCHEMA =
            "\n\t<xs:complexType name=\"YStringListType\">\n" +
                    "\t\t<xs:sequence>\n" +
                    "\t\t\t<xs:element name=\"item\" type=\"xs:string\"" +
                    " minOccurs=\"0\" maxOccurs=\"unbounded\"/>\n" +
                    "\t\t</xs:sequence>\n" +
                    "\t</xs:complexType>\n";


    public String getSchemaString() { return SCHEMA; }


    public Element getSchema(String name) {
        Element element = new Element("element", YAWL_NAMESPACE);
        element.setAttribute("name", name);

        Element complex = addElement(element, "complexType");
        Element sequence = addElement(complex, "sequence");
        Element item = addElement(sequence, "element");
        item.setAttribute("name", "item");
        item.setAttribute("type", "xs:string");
        item.setAttribute("minOccurs", "0");
        item.setAttribute("maxOccurs", "unbounded");

        return element;
    }


    private Element addElement(Element parent, String name) {
        Element element = new Element(name, YAWL_NAMESPACE);
        parent.addContent(element);
        return element;
    }
}