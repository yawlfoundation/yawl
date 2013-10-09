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

import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import org.yawlfoundation.yawl.editor.ui.properties.editor.ImageFilePropertyEditor;
import org.yawlfoundation.yawl.editor.ui.properties.editor.JustifyEditor;
import org.yawlfoundation.yawl.editor.ui.properties.editor.TextPropertyEditor;
import org.yawlfoundation.yawl.editor.ui.properties.editor.XQueryPropertyEditor;

/**
 * @author Michael Adams
 * @date 16/07/12
 */
public class ExtendedAttributesBeanInfo extends YBeanInfo {

    private static final String CATEGORY = "Ext. Attributes";


    public ExtendedAttributesBeanInfo(UserDefinedAttributesBinder udAttributes) {
        super(ExtendedAttributeProperties.class);
        switch (udAttributes.getOwnerClass()) {
            case Decomposition: addDecompositionProperties(); break;
            case Parameter: addVariableProperties(); break;

        }
        addUserDefinedAttributes(udAttributes);
    }


    private void addCommonProperties(String effect) {
        addProperty("backgroundColour", CATEGORY, "Background Colour",
                "Set the background colour of the dynamically generated " + effect);
        addProperty("font", CATEGORY, null, "Set the default font for labels and" +
                "text on the dynamically generated " + effect);
        addProperty("justify", CATEGORY, null,
                "Justify text display on the dynamically generated " + effect)
                .setPropertyEditorClass(JustifyEditor.class);
        addProperty("label", CATEGORY, null,
                "Set the label for the default prompt on the dynamically generated " + effect);
        addProperty("readOnly", CATEGORY, "Read Only",
                "Set data fields to be uneditable on the dynamically generated " + effect);
    }


    private void addDecompositionProperties() {
        addCommonProperties("form");
        addProperty("backgroundAltColour", CATEGORY, "Background Alt Colour",
                "Set the alternate background colour of the dynamically generated form");
        addProperty("headerFont", CATEGORY, "Heading Font",
                "Set the font for the heading on the dynamically generated form");
        addProperty("hideBanner", CATEGORY, "Hide Banner",
                "Hide the YAWL banner at the top of the page when dynamic forms are displayed");
        addProperty("pageBackgroundColour", CATEGORY, "Page Background Colour",
                "Set the background colour of the page behind the dynamically generated form");
        addProperty("pageBackgroundImage", CATEGORY, "Page Background Image",
                "Choose an image to display as a background on the page behind the dynamically generated form")
                .setPropertyEditorClass(ImageFilePropertyEditor.class);
        addProperty("title", CATEGORY, null,
                "Set the Title for the top of the dynamically generated form")
                .setPropertyEditorClass(TextPropertyEditor.class);
    }

    private void addVariableProperties() {
        addCommonProperties("field");
        addProperty("alert", CATEGORY, null,
                "Set a tailored validation error message for the field");
        addProperty("blackout", CATEGORY, null,
                "Show the field as blacked out (unviewable)");
        addProperty("fractionDigits", CATEGORY, null,
                "Set the number of digits to show after the decimal point for the field");
        addProperty("hide", CATEGORY, null, "Hide (remove) the field from view");
        addProperty("hideIf", CATEGORY, null, "Hide (remove) the field from view, " +
                "if the XQuery expression provided evaluates to true")
                .setPropertyEditorClass(XQueryPropertyEditor.class);
        addProperty("imageAbove", CATEGORY, "Image Above",
                "Set the URL of an image to show above the field");
        addProperty("imageAboveAlign", CATEGORY, "Image Above Align",
                "Set the alignment of an image above the field")
                .setPropertyEditorClass(JustifyEditor.class);
        addProperty("imageBelow", CATEGORY, "Image Below",
                "Set the URL of an image to show below the field");
        addProperty("imageBelowAlign", CATEGORY, "Image Below Align",
                "Set the alignment of an image below the field")
                .setPropertyEditorClass(JustifyEditor.class);
        addProperty("lineAbove", CATEGORY, "Line Above",
                "Draw a horizontal line above the field");
        addProperty("length", CATEGORY, null,
                "Set the exact number of characters required by the field");
        addProperty("lineBelow", CATEGORY, "Line Below",
                "Draw a horizontal line below the field");
        addProperty("maxExclusive", CATEGORY, "Max Exclusive",
                "One less than the upper range of valid numeric values accepted");
        addProperty("maxInclusive", CATEGORY, "Max Inclusive",
                "The upper range of valid numeric values accepted");
        addProperty("maxLength", CATEGORY, "Max Length",
                "Set the maximum number of characters accepted by the field");
        addProperty("minExclusive", CATEGORY, "Min Exclusive",
                "One more than the lower range of valid numeric values accepted");
        addProperty("minInclusive", CATEGORY, "Min Inclusive",
                "The lower range of valid numeric values accepted");
        addProperty("minLength", CATEGORY, "Min Length",
                "Set the minimum number of characters accepted by the field");
        addProperty("optional", CATEGORY, null, "Set the field to not require a value");
        addProperty("pattern", CATEGORY, null,
                "Set regular expression that the field value must match");
        addProperty("skipValidation", CATEGORY, "Skip Validation",
                "Set to not validate the field's value against its data schema");
        addProperty("textAbove", CATEGORY, "Text Above", "Insert text above the field");
        addProperty("textBelow", CATEGORY, "Text Below", "Insert text below the field");
        addProperty("textArea", CATEGORY, "Text Area",
                "Render the field as a text area instead of a one-line field");
        addProperty("tooltip", CATEGORY, "Tool Tip",
                "Set a tip to show when the mouse hovers over the field");
        addProperty("totalDigits", CATEGORY, "Total Digits",
                "Set the total number of digits expected in a numeric field");
        addProperty("whitespace", CATEGORY, null,
                "Normalise any whitespace characters in the field's value");
    }


    private void addUserDefinedAttributes(UserDefinedAttributesBinder udAttributes) {
        for (String name : udAttributes.getNames()) {
            ExtendedPropertyDescriptor property =
                    addProperty("UdAttributeValue", CATEGORY, name, null);
            property.setPropertyEditorClass(udAttributes.getEditorClass(name));
            property.setPropertyTableRendererClass(udAttributes.getRendererClass(name));
        }
    }


}
