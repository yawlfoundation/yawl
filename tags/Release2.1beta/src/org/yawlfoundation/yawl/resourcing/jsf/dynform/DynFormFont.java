package org.yawlfoundation.yawl.resourcing.jsf.dynform;

import java.awt.*;

/**
 * Author: Michael Adams
 * Creation Date: 7/04/2010
 */
public class DynFormFont {

    public static final Font _defFormFont = new Font("Helvetica", Font.PLAIN, 12);
    public static final Font _defFormHeaderFont = new Font("Helvetica", Font.BOLD, 14);
    public static final Font _formTitleFont = new Font("Helvetica", Font.PLAIN, 18);

    private Font _udFormFont;
    private String _udFormFontStyle;
    private Font _udFormHeaderFont;
    private String _udFormHeaderFontStyle;


    public DynFormFont() { }

    public Font getUserDefinedFormFont() {
        return _udFormFont;
    }

    public void setUserDefinedFormFont(Font font) {
        _udFormFont = font;
    }

    public String getUserDefinedFormFontStyle() {
        return _udFormFontStyle;
    }

    public void setUserDefinedFormFontStyle(String fontStyle) {
        _udFormFontStyle = fontStyle;
    }

    public Font getUserDefinedFormHeaderFont() {
        return _udFormHeaderFont;
    }

    public void setUserDefinedFormHeaderFont(Font font) {
        _udFormHeaderFont = font;
    }

    public String getUserDefinedFormHeaderFontStyle() {
        return _udFormHeaderFontStyle;
    }

    public void setUserDefinedFormHeaderFontStyle(String fontStyle) {
        if (! fontStyle.endsWith(";")) fontStyle += ";";
        _udFormHeaderFontStyle = fontStyle;
    }

    public Font getDefaultFormHeaderFont() {
        return _defFormHeaderFont;
    }

    public Font getDefaultFormFont() {
        return _defFormFont;
    }

    public Font getFormFont() {
        return (_udFormFont != null) ? _udFormFont : _defFormFont;
    }

    public Font getFormHeaderFont() {
        return (_udFormHeaderFont != null) ? _udFormHeaderFont : _defFormHeaderFont;
    }

    public Font getFormTitleFont() {
        return _formTitleFont;
    }


}
