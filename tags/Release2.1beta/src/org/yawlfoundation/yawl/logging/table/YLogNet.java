package org.yawlfoundation.yawl.logging.table;

import org.yawlfoundation.yawl.util.StringUtil;

/**
 * One row of the logNet table, representing a unique net 'template' of a specification 
 *
 * Author: Michael Adams
 * Creation Date: 9/04/2009
 */
public class YLogNet {

    private long netID;                                       // PK - auto generated 
    private String name;
    private long specKey;                                     // FK to YLogSpecification

    public YLogNet() { }

    public YLogNet(String name, long specKey) {
        this.name = name;
        this.specKey = specKey;
    }


    public long getSpecKey() {
        return specKey;
    }

    public void setSpecKey(long specKey) {
        this.specKey = specKey;
    }

    public long getNetID() {
        return netID;
    }

    public void setNetID(long netID) {
        this.netID = netID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String toXML() {
        StringBuilder xml = new StringBuilder(90);
        xml.append(String.format("<net key=\"%d\">", netID)) ;
        xml.append(StringUtil.wrap(name, "name"));
        xml.append(StringUtil.wrap(String.valueOf(specKey), "specKey"));
        xml.append("</net>");
        return xml.toString();
    }

}
