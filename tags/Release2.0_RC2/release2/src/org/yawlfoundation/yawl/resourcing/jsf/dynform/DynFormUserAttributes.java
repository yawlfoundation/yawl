package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import java.util.Map;

/**
 * This class manages the set of user-defined (at design-time) extended attributes
 * of a workitem, some of which will affect the way the dynamic form is displayed.
 *
 * Author: Michael Adams
 * Date: 24/02/2009
 */
public class DynFormUserAttributes {

    private Map<String, String> _attributeMap ;

    public DynFormUserAttributes(Map<String, String> attributeMap) {
        _attributeMap = attributeMap ;
    }


    public String getValue(String attribute) {
        if (_attributeMap == null) return null;
        return _attributeMap.get(attribute);
    }


    public boolean hasValue(String attribute) {
        return getValue(attribute) != null;
    }


    public boolean getBooleanValue(String attribute) {
        String value = getValue(attribute);
        return (value != null) && value.equalsIgnoreCase("true");
    }


    public boolean isReadOnly() {
        return getBooleanValue("readOnly");
    }
}
