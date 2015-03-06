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

package org.yawlfoundation.yawl.worklet.dialog;

import org.yawlfoundation.yawl.worklet.exception.ExletAction;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

/**
 * @author Michael Adams
 * @date 30/09/2014
 */
public class ExletActionCellEditor extends ExletCellEditor {

    public ExletActionCellEditor() { super(); }


    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row,
                                                 int column) {
        super.getTableCellEditorComponent(table, value, isSelected, row, column);
        return newComboInstance(table, value);
    }


    protected Vector<ExletAction> getItemsForContext(ConclusionTable table) {
        Vector<ExletAction> actions = new Vector<ExletAction>();
        RuleType selectedRule = table.getSelectedRuleType();
        if (selectedRule == RuleType.ItemSelection) {
            actions.add(ExletAction.Select);          // only valid action for selection
        }
        else {                                        // exception rule, so:
            actions.add(ExletAction.Continue);        // add general actions
            actions.add(ExletAction.Suspend);
            actions.add(ExletAction.Remove);
            actions.add(ExletAction.Compensate);
            if (selectedRule.isItemLevelType()) {     // add item only actions
                actions.add(ExletAction.Restart);
                actions.add(ExletAction.Complete);
                actions.add(ExletAction.Fail);
            }

            // sort on action string equivalents
            Collections.sort(actions, new Comparator<ExletAction>() {
                public int compare(ExletAction xa1, ExletAction xa2) {
                    return xa1.toString().compareTo(xa2.toString());
                }
            });
        }

        return actions;
    }

}
