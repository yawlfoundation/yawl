package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import net.sf.saxon.s9api.SaxonApiException;
import org.yawlfoundation.yawl.util.SaxonUtil;

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

    public DynFormUserAttributes() { }

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


    public int getIntegerValue(String attribute) {
        String value = getValue(attribute);
        int intValue = -1;                                        // default
        if (value != null) {
            try {
                intValue = new Integer(value);
            }
            catch (NumberFormatException nfe) {
                // nothing to do
            }
        }
        return intValue;
    }

    // *** the standard attributes ***//

    public boolean isReadOnly() {
        return getBooleanValue("readOnly");
    }


    public boolean isHidden() {
        return getBooleanValue("hide");
    }


    public boolean isShowIf() {
        boolean show = true;
        String query = getValue("showif");
        if (query != null) {
            try {
                String queryResult = SaxonUtil.evaluateQuery(query, null);
                show = queryResult.equalsIgnoreCase("true");
            }
            catch (SaxonApiException sae) {
                // nothing to do
            }
        }
        return show;
    }


    public String getAlertText() {
        return getValue("alert");                        // a validation error message
    }


    public String getLabelText() {
        return getValue("label");
    }


    public String getToolTipText() {
        return getValue("tooltip");
    }


    public int getMaxLength() {
        int max = getIntegerValue("maxlength");
        if (max == -1) max = Integer.MAX_VALUE;            // default to max if no value
        return max;
    }

}
