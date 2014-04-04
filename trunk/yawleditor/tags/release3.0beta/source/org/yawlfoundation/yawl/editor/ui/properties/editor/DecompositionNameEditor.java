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

package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.propertysheet.Property;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YAWLServiceGateway;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;

/**
* @author Michael Adams
* @date 13/07/12
*/
public class DecompositionNameEditor extends ComboPropertyEditor {

    public DecompositionNameEditor() {
        super();
        java.util.List<String> items = new ArrayList<String>();
        for (YAWLServiceGateway gateway :
                SpecificationModel.getHandler().getControlFlowHandler().getTaskDecompositions()) {
            items.add(gateway.getID());
        }
        Collections.sort(items);
        items.add(0, "Rename...");
        items.add(0, "New...");
        items.add(0, "None");
        setAvailableValues(items.toArray());
    }


    public void rationaliseItems(Property property) {
        if ("None".equals(property.getValue())) {
            ((JComboBox) editor).removeItemAt(2);   // Rename...
        }
    }

}
