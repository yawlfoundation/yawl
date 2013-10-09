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

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.properties.NetTaskPair;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.FlowConditionDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class SplitConditionPropertyEditor extends DialogPropertyEditor {

    private NetTaskPair netTaskPair;


    public SplitConditionPropertyEditor() {
        super(new DefaultCellRenderer());
    }

    public Object getValue() {
        return netTaskPair;
    }

    public void setValue(Object value) {
        netTaskPair = (NetTaskPair) value;
        String simpleText = netTaskPair != null ? netTaskPair.getSimpleText() : "None";
        ((DefaultCellRenderer) label).setValue(simpleText);
    }


    protected void showDialog() {
        FlowConditionDialog dialog = new FlowConditionDialog(YAWLEditor.getInstance(),
                netTaskPair.getTask(), netTaskPair.getGraph());
        dialog.setVisible(true);
        NetTaskPair oldPair = netTaskPair;
        netTaskPair = new NetTaskPair(oldPair.getTask(), oldPair.getGraph());
        netTaskPair.setSimpleText(getText(oldPair.getTask()));
        firePropertyChange(oldPair, netTaskPair);
    }


    private String getText(YAWLTask task) {
        if (task == null || task.getOutgoingFlowCount() == 0) return "None";
        List<String> predicates = new ArrayList<String>();
        for (YAWLFlowRelation flow : task.getOutgoingFlows()) {
            String predicate = flow.getPredicate();
            if (predicate != null) predicates.add(predicate);
        }
        return predicates.isEmpty() ? "None" : predicates.toString();
    }

}

