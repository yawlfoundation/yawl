/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.elements;

import org.jdom2.Attribute;
import org.yawlfoundation.yawl.util.DynamicValue;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.*;

/**
 * An extended Hashtable of key=attribute pairs.
 * @author Michael Adams
 * @since 2.1
 * @date 11/11/2009
 */
public class YAttributeMap extends Hashtable<String, String> {

    // a map of dynamically constructed values (each time the attributes are read)
    Map<String, DynamicValue> _dynamics;

    /**
     * Construct an (initially) empty attribute map.
     */
    public YAttributeMap() {
        _dynamics = new Hashtable<String, DynamicValue>();
    }

    /**
     * Construct an attribute map, and initialise it with the specified attributes.
     * @param attributes a map of key=value pairs to initialise the map.
     */
    public YAttributeMap(Map<String, String> attributes) {
        this();
        if (attributes != null) putAll(attributes);
    }


    /**
     * Replace the stored attributes (if any) with those specified.
     * @param attributes a map of key=value pairs to place in this map, replacing
     * any previous contents.
     */
    public void set(Map<String, String> attributes) {
        clear();
        _dynamics.clear();
        putAll(attributes);
    }


    /**
     * Get the boolean value matching the stored key.
     * @param key the attribute key to retrieve the value of.
     * @return true iff the attribute map contains a key matching the String
     * specified AND that attribute key has a corresponding value that matches
     * the String "true" (case-insensitive).
     */
    public boolean getBoolean(String key) {
        String value = getValue(key);
        return value != null && value.equalsIgnoreCase("true");
    }


    /**
     * Adds a key + DynamicValue to the dynamic map
     * @param key the key
     * @param value the DynamicValue
     * @return the added DynamicValue
     */
    public DynamicValue put(String key, DynamicValue value) {
        return _dynamics.put(key, value);
    }


    /**
     * Gets the stored value for the key (as a String). If the key refers to a
     * DynamicValue object, its value is dynamically evaluated
     * @param key the key
     * @return the stored String value
     */
    public String get(String key) {
        return getValue(key);
    }


    /**
     * Removes a value the store
     * @param key the key that refers to a DynamicValue
     * @return
     */
    public void remove(String key) {
        if (containsKey(key)) super.remove(key);
        else _dynamics.remove(key);
    }


    /**
     * Get the contents of the attribute map as a set of JDOM attributes.
     * @return a Set of populated JDOM Attribute objects.
     */
    public Set<Attribute> toJDOM() {
        Set<Attribute> result = new HashSet<Attribute>();
        for (String key : keySet()) {
            result.add(new Attribute(key, this.get(key)));
        }
        for (String key : _dynamics.keySet()) {
            result.add(new Attribute(key, _dynamics.get(key).toString()));
        }
        return result;
    }


    /**
     * Replace the stored attributes (if any) with the JDOM attributes specified.
     * @param jdomAttributes a List of JDOM Attribute objects to convert to key=value
     * pairs and place in this map, replacing any previous contents.
     */
    public void fromJDOM(List<Attribute> jdomAttributes) {
        if (jdomAttributes != null) {
            clear();
            for (Attribute attribute : jdomAttributes) {
                put(attribute.getName(), attribute.getValue());
            }
        }
    }


    public void transformDynamicValues(Object owner) {
        for (String key : keySet()) {
            String value = super.get(key);
            if (value.startsWith("dynamic{")) {
                super.remove(key);
                value = value.substring(8, value.lastIndexOf('}') -1);
                put(key, new DynamicValue(value, owner));
            }
        }
    }


    /**
     * Write a specified key=value pair in XML attribute format: key="value"
     * @param key the key to write out, with its corresponding value.
     * @return if the key exists in the attribute map, a representation of it in
     * XML attribute format, otherwise an empty String.
     */
    public String toXML(String key) {
        String xml = "";
        String value = getValue(key);
        if (value != null) xml = String.format("%s=\"%s\"", key, value);
        return xml;
    }


    /**
     * Write a specified key=value pair in XML element format: &lt;key&gt;value&lt;/key&gt;
     * @param key the key to write out, with its corresponding value.
     * @return if the key exists in the attribute map, a representation of it in
     * XML element format, otherwise an empty String.
     */
    public String toXMLElement(String key) {
        String xml = "";
        String value = getValue(key);
        if (value != null) xml = StringUtil.wrap(value, key);
        return xml;
    }

    /**
     * Write the full set of attributes stored in the map, in XML attribute format,
     * for insertion into an XML element.
     * @return a space separated list of this attribute map's key="value" pairs.
     */
    public String toXML() {
        String xml = "";
        for (String key : keySet()) {
             xml += " " + toXML(key);
        }
        return xml;
    }


    /**
     * Write the full set of attributes stored in the map, in XML element format.
     * @return a space separated list of this attribute map's
     * &lt;key&gt;value&lt;/key&gt; pairs.
     */
    public String toXMLElements() {
        String xml = "";
        for (String key : keySet()) {
             xml += " " + toXMLElement(key);
        }
        return xml;
    }


    // Gets the stored value for a key - tries the basic table first, then the dynamic one
    private String getValue(String key) {
        String value = super.get(key);
        if (value == null) {
            Object objValue = _dynamics.get(key);
            if (objValue != null) value = objValue.toString();
        }
        return value;
    }

}
