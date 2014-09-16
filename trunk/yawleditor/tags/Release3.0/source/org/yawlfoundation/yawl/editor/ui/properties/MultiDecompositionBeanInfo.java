/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLCompositeTask;
import org.yawlfoundation.yawl.editor.ui.properties.editor.*;

/**
 * @author Michael Adams
 * @date 13/06/14
 */
public class MultiDecompositionBeanInfo extends MultiCellBeanInfo {

    public MultiDecompositionBeanInfo(Object[] cells) {
        super(MultiDecompositionProperties.class, cells);
        addProperties();
    }


    private void addProperties() {
        String category = "Decomposition";
        addProperty("startLogPredicate", category, "Log Entry on Start",
                "Logs the specified entry text when the task starts at runtime")
                .setPropertyEditorClass(TextPropertyEditor.class);
        addProperty("completionLogPredicate", category, "Log Entry on Completion",
                "Logs the specified entry text when the task completes at runtime")
                .setPropertyEditorClass(TextPropertyEditor.class);

        if (! allInstanceOf(YAWLCompositeTask.class)) {
            addProperty("Automated", category, null,
                   "Set to false if the task should be placed on a user's work list");
            addProperty("CustomService", category, "Custom Service",
                   "The Custom Service that will execute the  task at runtime")
                   .setPropertyEditorClass(ServicesPropertyEditor.class);
            addProperty("Codelet", category, null, "Select a codelet for this automated task")
                   .setPropertyEditorClass(CodeletPropertyEditor.class);
            addProperty("ExtAttributes", category, "Ext. Attributes",
                   "Set attributes to assign to the decomposition at runtime")
                   .setPropertyEditorClass(ExtendedAttributesPropertyEditor.class);
        }
    }

}
