package org.yawlfoundation.yawl.elements;

import org.jdom.Attribute;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.*;

/**
 * Author: Michael Adams
 * Creation Date: 11/11/2009
 */
public class YAttributeMap extends Hashtable<String, String> {

    public YAttributeMap() { }

    public YAttributeMap(Map<String, String> attributes) {
        if (attributes != null) this.putAll(attributes);
    }

    
    public void set(Map<String, String> attributes) {
        this.clear();
        this.putAll(attributes);
    }


    public Set<Attribute> toJDOM() {
        Set<Attribute> result = new HashSet<Attribute>();
        for (String key : this.keySet()) {
            result.add(new Attribute(key, this.get(key)));
        }
        return result;
    }
    

    public void fromJDOM(List jdomAttributes) {
        if (jdomAttributes != null) {
            this.clear();
            for (Object o : jdomAttributes) {
                Attribute attribute = (Attribute) o;
                this.put(attribute.getName(), attribute.getValue());
            }
        }
    }


    public String toXML(String key) {
        String xml = "";
        String value = this.get(key);
        if (value != null) xml = String.format("%s=\"%s\"", key, value);
        return xml;
    }


    public String toXMLElement(String key) {
        String xml = "";
        String value = this.get(key);
        if (value != null) xml = StringUtil.wrap(value, key);
        return xml;
    }

    /**
     * @return a space separated string of attribute key="value" pairs
     */
    public String toXML() {
        String xml = "";
        for (String key : this.keySet()) {
             xml += " " + toXML(key);
        }
        return xml;
    }


    public String toXMLElements() {
        String xml = "";
        for (String key : this.keySet()) {
             xml += " " + toXMLElement(key);
        }
        return xml;
    }

}
