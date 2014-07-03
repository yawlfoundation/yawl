/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.elements.YAttributeMap;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * @author Michael Adams

 */
public class ExtendedAttributeProperties extends YPropertiesBean {

    private final UserDefinedAttributesBinder _udAttributes;
    private YAttributeMap _attributes;
    private String _varDataType;

    private ExtendedAttributeProperties(YPropertySheet sheet,
                                        UserDefinedAttributesBinder udAttributes) {
        super(sheet);
        _udAttributes = udAttributes;
    }


    public ExtendedAttributeProperties(YPropertySheet sheet,
                                       UserDefinedAttributesBinder udAttributes,
                                       YDecomposition decomposition) {
        this(sheet, udAttributes);
        _attributes = decomposition.getAttributes();
    }


    public ExtendedAttributeProperties(YPropertySheet sheet,
                                       UserDefinedAttributesBinder udAttributes,
                                       VariableRow row) {
        this(sheet, udAttributes);
        _attributes = row.getAttributes();
        _varDataType = row.getDataType();
    }


    /**************************************************************************/
    /** COMMON PROPERTIES **/

    public Color getBackgroundColour() { return getColour("background-color"); }

    public void setBackgroundColour(Color colour) {
        set("background-color", colour);
    }


    public FontColor getFont() { return getFontColorFromAttributes("font"); }

    public void setFont(FontColor font) { setAttributesFromFontColor(font, "font"); }


    public String getJustify() {
        String justify = get("justify");
        return justify != null ? justify : "left";
    }

    public void setJustify(String justify) {
        set("justify", justify.equals("left") ? null : justify);
    }


    public String getLabel() { return get("label"); }

    public void setLabel(String label) { set("label", label); }


    public boolean isReadOnly() { return getBoolean("readOnly"); }

    public void setReadOnly(boolean readOnly) { set("readOnly", readOnly); }


    /**************************************************************************/
    /** DECOMPOSITION PROPERTIES **/

    public Color getBackgroundAltColour() {
        return getColour("background-alt-color");
    }

    public void setBackgroundAltColour(Color colour) {
        set("background-alt-color", colour);
    }


    public FontColor getHeaderFont() {
        return getFontColorFromAttributes("header-font");
    }

    public void setHeaderFont(FontColor font) {
        setAttributesFromFontColor(font, "header-font");
    }


    public boolean isHideBanner() { return getBoolean("hideBanner"); }

    public void setHideBanner(boolean hideBanner) { set("hideBanner", hideBanner); }


    public Color getPageBackgroundColour() {
        return getColour("page-background-color");
    }

    public void setPageBackgroundColour(Color colour) {
        set("page-background-color", colour);
    }


    public File getPageBackgroundImage() {
        return getFile("page-background-image");
    }

    public void setPageBackgroundImage(File path) {
        set("page-background-image", path);
    }


    public String getTitle() { return get("title"); }

    public void setTitle(String title) { set("title", title); }


    /**************************************************************************/
    /** PARAMETER PROPERTIES **/

    public String getAlert() { return get("alert"); }

    public void setAlert(String alert) { set("alert", alert); }


    public String getBlackout() { return get("blackout"); }

    public void setBlackout(String blackout) { set("blackout", blackout); }


    public boolean isHide() { return getBoolean("hide"); }

    public void setHide(boolean value) { set("hide", value); }


    public String getHideIf() { return get("hide"); }

    public void setHideIf(String value) { set("hide", value); }


    public String getImageAbove() { return get("image-above"); }

    public void setImageAbove(String value) { set("image-above", value); }


    public String getImageBelow() { return get("image-below"); }

    public void setImageBelow(String value) { set("image-below", value); }


    public String getImageAboveAlign() {
        String align = get("image-above-align");
        return align != null ? align : "left";
    }

    public void setImageAboveAlign(String align) {
        set("image-above-align", align.equals("left") ? null : align);
    }


    public String getImageBelowAlign() {
        String align = get("image-below-align");
        return align != null ? align : "left";
    }

    public void setImageBelowAlign(String align) {
        set("image-below-align", align.equals("left") ? null : align);
    }


    public boolean isLineAbove() { return getBoolean("line-above"); }

    public void setLineAbove(boolean value) { set("line-above", value); }


    public boolean isLineBelow() { return getBoolean("line-below"); }

    public void setLineBelow(boolean value) { set("line-below", value); }


    public boolean isOptional() { return getBoolean("optional"); }

    public void setOptional(boolean value) { set("optional", value); }


    public boolean isSkipValidation() { return getBoolean("skipValidation"); }

    public void setSkipValidation(boolean value) { set("skipValidation", value); }


    public String getTextAbove() { return get("text-above"); }

    public void setTextAbove(String value) { set("text-above", value); }


    public String getTextBelow() { return get("text-below"); }

    public void setTextBelow(String value) { set("text-below", value); }


    public boolean isTextArea() { return getBoolean("textarea"); }

    public void setTextArea(boolean value) { set("textarea", value); }


    public String getTooltip() { return get("tooltip"); }

    public void setTooltip(String value) { set("tooltip", value); }


    /***************************************************************************/
    /** VARIABLE FACET PROPERTIES - FOR SIMPLE TYPES ONLY **/

    public NonNegativeInteger getFractionDigits() {
        return new NonNegativeInteger(get("fractionDigits"));
    }

    public void setFractionDigits(NonNegativeInteger digits) {
        showError(digits);
        set("fractionDigits", digits.getValue());
    }


    public NonNegativeInteger getLength() {
        return new NonNegativeInteger(get("length"));
    }

    public void setLength(NonNegativeInteger value) {
        showError(value);
        set("length", value.getValue());
    }


    public TypeValuePair getMaxExclusive() {
        return new TypeValuePair(_varDataType, get("maxExclusive"));
    }

    public void setMaxExclusive(TypeValuePair pair) {
        showError(pair);
        set("maxExclusive", pair.getValue());
    }


    public TypeValuePair getMinExclusive() {
        return new TypeValuePair(_varDataType, get("minExclusive"));
    }

    public void setMinExclusive(TypeValuePair pair) {
        showError(pair);
        set("minExclusive", pair.getValue());
    }


    public TypeValuePair getMaxInclusive() {
        return new TypeValuePair(_varDataType, get("maxExclusive"));
    }

    public void setMaxInclusive(TypeValuePair pair) {
        showError(pair);
        set("maxExclusive", pair.getValue());
    }


    public TypeValuePair getMinInclusive() {
        return new TypeValuePair(_varDataType, get("minInclusive"));
    }

    public void setMinInclusive(TypeValuePair pair) {
        showError(pair);
        set("minInclusive", pair.getValue());
    }


    public NonNegativeInteger getMaxLength() {
        return new NonNegativeInteger(get("maxLength"));
    }

    public void setMaxLength(NonNegativeInteger value) {
        showError(value);
        set("maxLength", value.getValue());
    }


    public NonNegativeInteger getMinLength() {
        return new NonNegativeInteger(get("minLength"));
    }

    public void setMinLength(NonNegativeInteger value) {
        showError(value);
        set("minLength", value.getValue());
    }


    public RegexString getPattern() { return new RegexString(get("pattern")); }

    public void setPattern(RegexString pattern) {
        showError(pattern);
        set("pattern", pattern.getValue());
    }


    public NonNegativeInteger getTotalDigits() {
        return new NonNegativeInteger(get("totalDigits"));
    }

    public void setTotalDigits(NonNegativeInteger digits) {
        showError(digits);
        set("totalDigits", digits.getValue());
    }


    public boolean isWhitespace() { return getBoolean("whitespace"); }

    public void setWhitespace(boolean value) { set("whitespace", value); }


    /**************************************************************************/

    public Object getUdAttributeValue() {
        return _udAttributes.getValue();
    }

    public void setUdAttributeValue(Object value) {
        _udAttributes.setValue(value);
    }


    /****************************************************************************/

    private String get(String key) { return _attributes.get(key); }

    private boolean getBoolean(String key) {
        String value = get(key);
        return value != null && value.equalsIgnoreCase("true");
    }

    private Integer getInt(String key) {
        String value = get(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            }
            catch (NumberFormatException nfe) {
                // fall through
            }
        }
        return null;
    }

    private Color getColour(String key) {
        String colourStr = get(key);
        return colourStr != null ? PropertyUtil.hexToColor(colourStr) : Color.WHITE;
    }

    private File getFile(String key) {
        String path = get(key);
        return path != null ? new File(path) : null;
    }


    private void set(String key, String value) {
        if (! StringUtil.isNullOrEmpty(value)) _attributes.put(key, value);
        else remove(key);
    }

    private void set(String key, boolean value) {
        set(key, value ? "true" : null);
    }

    private void set(String key, Integer value) {
        set(key, value != null ? value.toString() : null);
    }

    private void set(String key, Color colour) {
        set(key, colour != null ? PropertyUtil.colorToHex(colour) : null);
    }

    private void set(String key, File file) {
        set(key, file != null ? file.getAbsolutePath() : null);
    }


    private void remove(String key) { _attributes.remove(key); }


    private void setAttributesFromFontColor(FontColor fontColor, String prefix) {
        Font font = fontColor.getFont();

        String family = font.getFamily();
        if (UIManager.getDefaults().getFont("Label.font").getFamily().equals(family)) {
            family = null;
        }
        set(prefix + "-family", family);

        Integer size = font.getSize();
        if (size == UserSettings.getFontSize()) {
            size = null;
        }
        set(prefix + "-size", size);

        set(prefix + "-style", intToFontStyle(font.getStyle()));

        Color colour = fontColor.getColour();
        set(prefix + "-color", colour.equals(Color.BLACK) ? null : colour);
    }


    private FontColor getFontColorFromAttributes(String prefix) {
        String family = get(prefix + "-family");
        if (family == null) family = UIManager.getDefaults().getFont("Label.font").getFamily();

        Integer size = getInt(prefix + "-size");
        if (size == null) size = UserSettings.getFontSize();

        int style = fontStyleToInt(get(prefix + "-style"));

        String colourStr = get(prefix + "-color");
        Color colour = StringUtil.isNullOrEmpty(colourStr) ? Color.BLACK
                : PropertyUtil.hexToColor(colourStr);
        return new FontColor(new Font(family, style, size), colour);
    }


    private int fontStyleToInt(String style) {
        if (style == null) return Font.PLAIN;
        if (style.equals("bold")) return Font.BOLD;
        if (style.equals("italic")) return Font.ITALIC;
        if (style.equals("bold,italic")) return Font.BOLD | Font.ITALIC;
        return Font.PLAIN;
    }


    private String intToFontStyle(int style) {
        switch (style) {
            case Font.BOLD : return "bold";
            case Font.ITALIC : return "italic";
            case Font.BOLD | Font.ITALIC : return "bold,italic";
            default : return null;
        }
    }


    private void showError(NonNegativeInteger value) {
        if (value != null && value.hasError()) {
            ((ExtendedAttributesPropertySheet) getSheet()).showStatus(value.getError());
        }
    }

}
