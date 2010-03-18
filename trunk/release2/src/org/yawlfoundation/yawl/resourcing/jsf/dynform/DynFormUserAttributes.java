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


    public boolean isSkipValidation() {
        return getBooleanValue("skipValidation");
    }


    public boolean isBlackout() {
        return getBooleanValue("blackout");
    }

    public boolean isMandatory() {
        return getBooleanValue("mandatory");
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


    public String getTextJustify() {
        String justify = getValue("justify");
        if (justify != null) {
            String[] validValues = {"center", "right", "left"};
            for (int i=0; i<3; i++) {
                if (validValues[i].equals(justify.toLowerCase())) {
                    return validValues[i];
                }
            }
        }
        return null;
    }


    public String getBackgroundColour() {
        return getValue("background-color");         
    }


    public String getUserDefinedFontStyle() {
        String style = "";
        String fontColour = getValue("font-color");
        if ((fontColour != null) && (! isBlackout())) {
            style += String.format(";color: %s", fontColour);
        }
        String fontFamily = getValue("font-family");
        if (fontFamily != null) style += String.format(";font-family: %s", fontFamily);
        String fontSize = getValue("font-size");
        if (fontSize != null) style += String.format(";font-size: %s", fontSize);
        String fontStyle = getValue("font-style");
        if (fontStyle != null) {
            if (fontStyle.contains("bold")) style += ";font-weight: bold";
            if (fontStyle.contains("italic")) style += ";font-style: italic";
        }
        return style;
    }


    public String getImageAbove() {
        return getValue("image-above");
    }

    public String getImageBelow() {
        return getValue("image-below");
    }

    public boolean isLineAbove() {
        return getBooleanValue("line-above");
    }

    public boolean isLineBelow() {
        return getBooleanValue("line-below");
    }

    public String getTextAbove() {
        return getValue("text-above");
    }

    public String getTextBelow() {
        return getValue("text-below");
    }

}
