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

import org.yawlfoundation.yawl.editor.ui.specification.validation.ValidationMessage;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ProblemTable extends JSingleSelectTable {

    private static final int MAX_ROW_HEIGHT = 5;

    public ProblemTable(ListSelectionListener listener) {
        super();
        initialise();
        getSelectionModel().addListSelectionListener(listener);
    }

    private void initialise() {
        setModel(new ValidationMessageTableModel());
        setFillsViewportHeight(true);
        setTableHeader(null);
    }


    public void addMessages(java.util.List<ValidationMessage> messages) {
        getTableModel().addMessages(messages);
    }

    public void setWidth(int width) {
        getColumnModel().getColumn(0).setPreferredWidth(width);
    }


    public void reset() {
        getTableModel().reset();
    }

    public String getLongMessageForSelectedRow() {
        return getTableModel().getLongMessage(getSelectedRow());
    }


    public Dimension getPreferredScrollableViewportSize() {
        Dimension preferredSize = super.getPreferredScrollableViewportSize();

        preferredSize.setSize(
                preferredSize.getWidth(),
                Math.min(
                        preferredSize.getHeight(),
                        getFontMetrics(getFont()).getHeight() * MAX_ROW_HEIGHT
                )
        );

        return preferredSize;
    }

    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        JComponent component = (JComponent) super.prepareRenderer(renderer, row, col);
        JLabel componentAsLabel = (JLabel) component;
        componentAsLabel.setHorizontalAlignment(JLabel.LEFT);
        return component;
    }


    private ValidationMessageTableModel getTableModel() {
        return (ValidationMessageTableModel) getModel();
    }
}

