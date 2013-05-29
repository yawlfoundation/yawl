package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.editor.ui.properties.editor.ComboPropertyEditor;

/**
 * Loads the properties for a CellProperties backing Bean
 *
 * @author Michael Adams
 * @date 4/07/12
 */
public class FlowBeanInfo extends NetBeanInfo {

    public FlowBeanInfo() {
        super(FlowProperties.class);
        addCommonProperties();
    }


    private void addCommonProperties() {
        String category = "Flow";
        addProperty("LineStyle", category, "Line Style", "Style of the line")
                .setPropertyEditorClass(StyleEditor.class);
        addProperty("Source", category, null, "Source element");
        addProperty("Target", category, null, "Target element");
    }


    public static class StyleEditor extends ComboPropertyEditor {

        public StyleEditor() { super(FlowProperties.STYLES); }
    }

}
