package org.yawlfoundation.yawl.editor.ui.properties;

import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.properties.editor.*;

/**
 * @author Michael Adams
 * @date 16/07/12
 */
public class DecompositionBeanInfo extends CellBeanInfo {

    public DecompositionBeanInfo(YAWLVertex vertex) {
        super(DecompositionProperties.class, vertex);
        addProperties();
    }


    private void addProperties() {
        String category = "Decomposition";
        addProperty("Automated", category, null,
                "Set to false if the task should be placed on a user's work list");
        addProperty("CustomService", category, "Custom Service",
                "The Custom Service that will execute the  task at runtime")
                .setPropertyEditorClass(ServicesPropertyEditor.class);
        addProperty("startLogPredicate", category, "Log Entry on Start",
                "Logs the specified entry text when the task starts at runtime")
                .setPropertyEditorClass(TextPropertyEditor.class);
        addProperty("completionLogPredicate", category, "Log Entry on Completion",
                "Logs the specified entry text when the task completes at runtime")
                .setPropertyEditorClass(TextPropertyEditor.class);
        addProperty("Codelet", category, null, "Select a codelet for this automated task")
                .setPropertyEditorClass(CodeletPropertyEditor.class);
        addProperty("TaskDataVariables", category, "Data Variables",
                "Set data variables and mappings for the currently selected task")
                .setPropertyEditorClass(DataVariablePropertyEditor.class);

        // extended attributes
        category = "Ext. Attributes";
        addProperty("ExBackgroundColour", category, "Background Colour",
                "Set the background colour of the dynamically generated form");
        addProperty("ExBackgroundAltColour", category, "Background Alt Colour",
                "Set the alternate background colour of the dynamically generated form");
        addProperty("ExFont", category, "Font", "Set the default font for labels and" +
                "text on the dynamically generated form");
        addProperty("ExHeaderFont", category, "Heading Font",
                "Set the font for the heading on the dynamically generated form");
        addProperty("ExHideBanner", category, "Hide Banner",
                "Hide the YAWL banner at the top of the page when dynamic forms are displayed");
        addProperty("ExJustify", category, "Justify",
                "Justify text display on the dynamically generated form")
                .setPropertyEditorClass(JustifyEditor.class);
        addProperty("ExLabel", category, "Label",
                "Set the label for the task name on the dynamically generated form");
        addProperty("ExPageBackgroundColour", category, "Page Background Colour",
                "Set the background colour of the page behind the dynamically generated form");
        addProperty("ExPageBackgroundImage", category, "Page Background Image",
                "Choose an image to display as a background on the page behind the dynamically generated form")
                .setPropertyEditorClass(ImageFilePropertyEditor.class);
        addProperty("ExReadOnly", category, "Read Only",
                "Set all data fields to be uneditable on the dynamically generated form");
        addProperty("ExTitle", category, "Title",
                "Set the Title for the top of the dynamically generated form")
                .setPropertyEditorClass(TextPropertyEditor.class);

        addUserDefinedAttributes(category);
    }


    private void addUserDefinedAttributes(String category) {
        UserDefinedAttributes udAttributes = UserDefinedAttributes.getInstance();
        for (String name : udAttributes.getNames()) {
            ExtendedPropertyDescriptor property =
                    addProperty("UdAttributeValue", category, name, null);
            property.setPropertyEditorClass(udAttributes.getEditorClass(name));
            property.setPropertyTableRendererClass(udAttributes.getRendererClass(name));
        }
    }


    public static class JustifyEditor extends ComboPropertyEditor {

        public JustifyEditor() { super(new String[] { "left", "center", "right"} ); }
    }

}
