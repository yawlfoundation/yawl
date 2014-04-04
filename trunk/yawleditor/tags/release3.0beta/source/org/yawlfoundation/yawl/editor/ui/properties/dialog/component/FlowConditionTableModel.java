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

package org.yawlfoundation.yawl.editor.ui.properties.dialog.component;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;

import javax.swing.table.AbstractTableModel;
import java.util.Collections;
import java.util.List;

/**
* @author Michael Adams
* @date 3/08/12
*/
class FlowConditionTableModel extends AbstractTableModel {

    private List<YAWLFlowRelation> flows;
    protected boolean editable;

    private static final String[] COLUMN_LABELS = {"Target", "Condition"};
    private static final int NAME_COLUMN  = 0;
    private static final int CONDITION_COLUMN  = 1;


    public FlowConditionTableModel() {
        super();
    }


    public int getRowCount() {
        return (getFlows() != null) ? getFlows().size() : 0;
    }

    public int getColumnCount() {
        return COLUMN_LABELS.length;
    }

    public String getColumnName(int column) {
        return COLUMN_LABELS[column];
    }

    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    public boolean isCellEditable(int row, int column) {
        return hasXORSplit() ? column == CONDITION_COLUMN && row < (getRowCount() - 1) :
                column == CONDITION_COLUMN;
    }

    public Object getValueAt(int row, int col) {
        YAWLFlowRelation selected = getFlowAtRow(row);
        switch (col) {
            case NAME_COLUMN:  {
                return selected.getTargetID();
            }
            case CONDITION_COLUMN:  {
                String predicate = selected.getPredicate();
                return predicate != null ? predicate : "true()";
            }
            default: {
                return null;
            }
        }
    }

    public void setValueAt(Object value, int row, int col) {
        YAWLFlowRelation selected = getFlowAtRow(row);
        if (col == CONDITION_COLUMN) {
            selected.setPredicate((String) value);
            fireTableRowsUpdated(row, row);
        }
    }


    public YAWLFlowRelation getFlowAtRow(int row) {
        if (getRowCount() == 0 || row < 0 || row >= getRowCount()) {
            return null;
        }
        return flows.get(row);
    }


    public void swapRows(int first, int second) {
        Collections.swap(flows, first, second);
        resetOrdering();
        if (defaultSwapped()) {
            resetDefault();
        }
        fireTableRowsUpdated(first, second);
    }


    public void setFlows(List<YAWLFlowRelation> list) {
        flows = list;
        sortFlows();
        fireTableRowsUpdated(0, getRowCount() - 1);
    }


    public List<YAWLFlowRelation> getFlows() {
        return flows;
    }


    public void cleanupFlows() {
        if (! flows.isEmpty() && hasXORSplit()) {
           getDefaultFlow().setPredicate(null);
        }
    }


    private void resetOrdering() {
        if (hasXORSplit()) {
            for (int i=0; i<getRowCount(); i++) {
                flows.get(i).setPriority(i);
            }
        }
    }


    private void resetDefault() {
        if (getDefaultFlowIndex() != getRowCount() - 1) {
            if (hasXORSplit()) {
                getDefaultFlow().setPredicate("true()");
            }
            getDefaultFlow().setIsDefaultFlow(false);
            setDefault();
            if (hasXORSplit()) {
                getDefaultFlow().setPredicate(null);
            }
        }
    }

    private void sortFlows() {
        if (flows == null || flows.isEmpty()) return;

        if (hasXORSplit()) {
            Collections.sort(flows);
        }
        else {  // move default flow to end of list
            Collections.swap(flows, getDefaultFlowIndex(), flows.size() -1);
        }
    }


    private boolean hasXORSplit() {
        return ! flows.isEmpty() && flows.get(0).hasXorSplitAsSource();
    }


    private int getDefaultFlowIndex() {
        for (int i=0; i < getRowCount(); i++) {
            if (flows.get(i).isDefaultFlow()) {
                return i;
            }
        }
        return 0;
    }


    private YAWLFlowRelation getDefaultFlow() {
        return flows.get(getDefaultFlowIndex());
    }


    private YAWLFlowRelation getBottomFlow() {
        return flows.get(flows.size() -1);
    }

    private void setDefault() {
        getBottomFlow().setIsDefaultFlow(true);
    }

    private boolean defaultSwapped() {
        return getDefaultFlowIndex() != flows.size() -1;
    }

}
