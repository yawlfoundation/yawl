package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import net.sf.saxon.s9api.SaxonApiException;
import org.jdom.Document;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.SaxonUtil;

import java.awt.*;
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

    public boolean hasHideIfQuery() {
        return (hasValue("hideIf"));
    }

    public boolean isHideIf(String data) {
        boolean hide = false;
        String query = getValue("hideIf");
        if (query != null) {
            try {
                Document dataDoc = JDOMUtil.stringToDocument(data);
                String queryResult = SaxonUtil.evaluateQuery(query, dataDoc);
                hide = queryResult.equalsIgnoreCase("true");
            }
            catch (SaxonApiException sae) {
                // nothing to do, will default to false
            }
        }
        return hide;
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

    public boolean isTextArea() {
        return getBooleanValue("textarea");
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
        if (fontSize != null) style += String.format(";font-size: %spx", fontSize);
        String fontStyle = getValue("font-style");
        if (fontStyle != null) {
            if (fontStyle.contains("bold")) style += ";font-weight: bold";
            if (fontStyle.contains("italic")) style += ";font-style: italic";
        }
        return style;
    }

    public Font getUserDefinedFont() {
        String fontFamily = getValue("font-family");
        String family = (fontFamily != null)  ?  fontFamily : "Helvetica";

        int fontSize = getIntegerValue("font-size");
        int size = (fontSize > -1) ? fontSize : 12;

        int style = Font.PLAIN;
        String fontStyle = getValue("font-style");
        if (fontStyle != null) {
            if (fontStyle.contains("bold") && fontStyle.contains("italic"))
                style = Font.BOLD | Font.ITALIC;
            else if (fontStyle.contains("bold"))
                style = Font.BOLD;
            else if (fontStyle.contains("italic"))
                style = Font.ITALIC;
        }
        return new Font(family, style, size);
    }


    public String getImageAbove() {
        return getValue("image-above");
    }

    public String getImageBelow() {
        return getValue("image-below");
    }

    public String getImageAboveAlign() {
        return getValue("image-above-align");
    }

    public String getImageBelowAlign() {
        return getValue("image-below-align");
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
