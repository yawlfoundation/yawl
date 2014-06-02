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

package org.yawlfoundation.yawl.editor.ui.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class JSingleSelectTable extends JAlternatingRowColorTable {

    private static final int DEFAULT_ROWS = 5;

    public JSingleSelectTable() {
        super();
        initialise(DEFAULT_ROWS);
    }

    public JSingleSelectTable(int rows) {
        super();
        initialise(rows);
    }

    private void initialise(int rows) {
        lockDownColumns();
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setRowSelectionAllowed(true);
        setPreferredScrollableViewportSize(new Dimension(300, getRowHeight()*rows));
    }

    private void lockDownColumns() {
        getTableHeader().setReorderingAllowed(false);
        setColumnSelectionAllowed(false);
    }

    public void selectRow(int row) {
        if (getRowCount() > 0 && row < getRowCount() ) {
            getSelectionModel().setSelectionInterval(row,row);
            repositionToKeepSelectedRowVisible();
        }
    }

    private void repositionToKeepSelectedRowVisible() {
        try {
            JScrollPane pane = (JScrollPane) this.getParent().getParent();
            JViewport viewport = pane.getViewport();
            Rectangle viewRect = viewport.getViewRect();

            Rectangle rowRectangle = getCellRect(getSelectedRow(), 0, false);
            Point viewPoint = rowRectangle.getLocation();
            if (viewPoint.getY() < viewRect.getY()) {
                viewport.setViewPosition(viewPoint);
            }
            if (viewPoint.getY() + rowRectangle.getHeight() >
                    viewRect.getY()  + viewRect.getHeight()) {
                viewport.scrollRectToVisible(rowRectangle);
            }
        } catch (Exception e) {}
    }

    /**
     * This method adds a custom action to prevent wrapping to the first table row
     * when the enter key is pressed while on the last table row - that is, it
     * overrides default enter key behaviour and stays on the last row.
     *
     * Based on code sourced from stackoverflow.com
     */
    protected void consumeEnterKeyWraps() {
        Object key = getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
            .get(KeyStroke.getKeyStroke("ENTER"));
        final Action action = getActionMap().get(key);

        Action custom = new AbstractAction("wrap") {
            public void actionPerformed(ActionEvent e) {
                int row = getSelectionModel().getLeadSelectionIndex();
                if (row == getRowCount() - 1) {
                    if (isEditing()) getCellEditor().stopCellEditing();
                    return;     // stop wrapping to top of table
                }
                action.actionPerformed(e);
            }

        };
        getActionMap().put(key, custom);
    }
}
