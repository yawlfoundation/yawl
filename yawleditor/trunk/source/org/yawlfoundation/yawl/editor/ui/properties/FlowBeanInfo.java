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

import org.yawlfoundation.yawl.editor.ui.properties.editor.ComboPropertyEditor;

/**
 * Loads the properties for a FlowProperties backing Bean
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
        addProperty("Predicate", category, null, "The control-flow predicate");
         //       .setPropertyEditorClass(FlowPredicatePropertyEditor.class);
        addProperty("Ordering", category, "Index",
                "The evaluation ordering index (XOR-split flows only)");
        addProperty("Default", category, null,
                "Is the default flow from the source element");
    }


    public static class StyleEditor extends ComboPropertyEditor {

        public StyleEditor() { super(FlowProperties.STYLES); }
    }

}
