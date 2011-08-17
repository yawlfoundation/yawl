/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.logging;

import org.jdom.Element;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

/**
 * Author: Michael Adams
 * Creation Date: 6/04/2009
 */
public class YLogDataItem {

    private String name ;
    private String value ;
    private String dataTypeName;
    private String dataTypeDefinition;

    // any meaningful string that describes the class, category or group of the item
    private String descriptor ;


    public YLogDataItem() { }

    public YLogDataItem(String descriptor, String name, String value, String dataType) {
        this.name = name;
        this.value = value;
        this.dataTypeName = dataType;
        this.descriptor = descriptor;
        this.dataTypeDefinition = dataType;           // deliberate, default duplication
    }

    public YLogDataItem(String descriptor, String name, String value, String dataType,
                        String dataTypeDefinition) {
        this(descriptor, name, value, dataType);
        this.dataTypeDefinition = dataTypeDefinition;
    }


    public YLogDataItem(String xml) {
        fromXML(xml);
    }

    public YLogDataItem(Element xml) {
        fromXML(xml);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setValue(Object value) {
        this.value = String.valueOf(value);
    }

    public String getDataTypeName() {
        return dataTypeName;
    }

    public void setDataTypeName(String dataTypeName) {
        this.dataTypeName = dataTypeName;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public String getDataTypeDefinition() {
        return dataTypeDefinition;
    }

    public void setDataTypeDefinition(String dataTypeDefinition) {
        this.dataTypeDefinition = dataTypeDefinition;
    }

    public String toXML() {
        StringBuilder xml = new StringBuilder(250);
        xml.append("<logdataitem>");
        xml.append(toXMLShort());
        xml.append(StringUtil.wrapEscaped(dataTypeName, "datatype"));
        xml.append(StringUtil.wrapEscaped(dataTypeDefinition, "datatypedefinition"));
        xml.append("</logdataitem>");
        return xml.toString();
    }

    
    public String toXMLShort() {
        StringBuilder xml = new StringBuilder(250);
        xml.append(StringUtil.wrapEscaped(name, "name"))
           .append(StringUtil.wrapEscaped(value, "value"))
           .append(StringUtil.wrapEscaped(descriptor, "descriptor"));
        return xml.toString();
    }


    private void fromXML(String xml) {
        fromXML(JDOMUtil.stringToElement(xml));
    }


    private void fromXML(Element e) {
        if (e != null) {
            name = JDOMUtil.decodeEscapes(e.getChildText("name"));
            value = JDOMUtil.decodeEscapes(e.getChildText("value"));
            descriptor = JDOMUtil.decodeEscapes(e.getChildText("descriptor"));
            dataTypeName = JDOMUtil.decodeEscapes(e.getChildText("datatype"));
            dataTypeDefinition =  JDOMUtil.decodeEscapes(e.getChildText("datatypedefinition"));
        }
    }

}
