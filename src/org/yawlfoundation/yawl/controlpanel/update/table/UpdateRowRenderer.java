/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.controlpanel.update.table;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author Michael Adams
 * @date 18/08/2014
 */
public class UpdateRowRenderer extends DefaultTableCellRenderer {

    private static final Color REMOVING = new Color(215, 24, 14);
    private static final Color UPDATEABLE = new Color(11, 70, 203);
    private static final Color ADDING = new Color(7, 167, 38);


    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        UpdateRow updateRow = ((UpdateTable) table).getTableModel().getRows().get(row);
        if (updateRow.isRemoving()) {
            setForeground(REMOVING);
        }
        else if (updateRow.isAdding()) {
            setForeground(ADDING);
        }
        else if (updateRow.hasNewVersion() && updateRow.isInstalled()) {
            setForeground(UPDATEABLE);
        }
        else setForeground(Color.BLACK);

        if (column < 2) {
            setHorizontalAlignment(SwingConstants.LEFT);
        }
        else if (column < 4) {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }
    //    if (column == UpdateTableModel.COL_INSTALL) {
        else {
            setHorizontalAlignment(SwingConstants.CENTER);
            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(updateRow.getInstallAction());
            checkBox.setEnabled(updateRow.isInstallable());
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
            return checkBox;
        }

        return this;
    }


    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        g2.setRenderingHints(rh);
        super.paint(g);
    }


}
