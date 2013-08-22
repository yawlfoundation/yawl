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
        addProperty("BackgroundColour", CATEGORY, "Background Colour",
                "Set the background colour of the dynamically generated " + effect);
        addProperty("Font", CATEGORY, null, "Set the default font for labels and" +
                "text on the dynamically generated " + effect);
        addProperty("Justify", CATEGORY, null,
                "Justify text display on the dynamically generated " + effect)
                .setPropertyEditorClass(JustifyEditor.class);
        addProperty("Label", CATEGORY, null,
                "Set the label for the default prompt on the dynamically generated " + effect);
        addProperty("ReadOnly", CATEGORY, "Read Only",
                "Set data fields to be uneditable on the dynamically generated " + effect);
    }


    private void addDecompositionProperties() {
        addCommonProperties("form");
        addProperty("BackgroundAltColour", CATEGORY, "Background Alt Colour",
                "Set the alternate background colour of the dynamically generated form");
        addProperty("HeaderFont", CATEGORY, "Heading Font",
                "Set the font for the heading on the dynamically generated form");
        addProperty("HideBanner", CATEGORY, "Hide Banner",
                "Hide the YAWL banner at the top of the page when dynamic forms are displayed");
        addProperty("PageBackgroundColour", CATEGORY, "Page Background Colour",
                "Set the background colour of the page behind the dynamically generated form");
        addProperty("PageBackgroundImage", CATEGORY, "Page Background Image",
                "Choose an image to display as a background on the page behind the dynamically generated form")
                .setPropertyEditorClass(ImageFilePropertyEditor.class);
        addProperty("Title", CATEGORY, null,
                "Set the Title for the top of the dynamically generated form")
                .setPropertyEditorClass(TextPropertyEditor.class);
    }

    private void addVariableProperties() {
        addCommonProperties("field");
        addProperty("Alert", CATEGORY, null,
                "Set a tailored validation error message for the field");
        addProperty("Blackout", CATEGORY, null,
                "Show the field as blacked out (unviewable)");
        addProperty("FractionDigits", CATEGORY, null,
                "Set the number of digits to show after the decimal point for the field");
        addProperty("Hide", CATEGORY, null, "Hide (remove) the field from view");
        addProperty("HideIf", CATEGORY, null, "Hide (remove) the field from view, " +
                "if the XQuery expression provided evaluates to true")
                .setPropertyEditorClass(XQueryPropertyEditor.class);
        addProperty("ImageAbove", CATEGORY, "Image Above",
                "Set the URL of an image to show above the field");
        addProperty("ImageAboveAlign", CATEGORY, "Image Above Align",
                "Set the alignment of an image above the field")
                .setPropertyEditorClass(JustifyEditor.class);
        addProperty("ImageBelow", CATEGORY, "Image Below",
                "Set the URL of an image to show below the field");
        addProperty("ImageBelowAlign", CATEGORY, "Image Below Align",
                "Set the alignment of an image below the field")
                .setPropertyEditorClass(JustifyEditor.class);
        addProperty("LineAbove", CATEGORY, "Line Above",
                "Draw a horizontal line above the field");
        addProperty("Length", CATEGORY, null,
                "Set the exact number of characters required by the field");
        addProperty("LineBelow", CATEGORY, "Line Below",
                "Draw a horizontal line below the field");
        addProperty("MaxExclusive", CATEGORY, "Max Exclusive",
                "One less than the upper range of valid numeric values accepted");
        addProperty("MaxInclusive", CATEGORY, "Max Inclusive",
                "The upper range of valid numeric values accepted");
        addProperty("MaxLength", CATEGORY, "Max Length",
                "Set the maximum number of characters accepted by the field");
        addProperty("MinExclusive", CATEGORY, "Min Exclusive",
                "One more than the lower range of valid numeric values accepted");
        addProperty("MinInclusive", CATEGORY, "Min Inclusive",
                "The lower range of valid numeric values accepted");
        addProperty("MinLength", CATEGORY, "Min Length",
                "Set the minimum number of characters accepted by the field");
        addProperty("Optional", CATEGORY, null, "Set the field to not require a value");
        addProperty("Pattern", CATEGORY, null,
                "Set regular expression that the field value must match");
        addProperty("SkipValidation", CATEGORY, "Skip Validation",
                "Set to not validate the field's value against its data schema");
        addProperty("TextAbove", CATEGORY, "Text Above", "Insert text above the field");
        addProperty("TextBelow", CATEGORY, "Text Below", "Insert text below the field");
        addProperty("TextArea", CATEGORY, "Text Area",
                "Render the field as a text area instead of a one-line field");
        addProperty("Tooltip", CATEGORY, "Tool Tip",
                "Set a tip to show when the mouse hovers over the field");
        addProperty("TotalDigits", CATEGORY, "Total Digits",
                "Set the total number of digits expected in a numeric field");
        addProperty("Whitespace", CATEGORY, null,
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
