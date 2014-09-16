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

import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

public class JAlternatingRowColorTable extends JTable {

    private static final Color DEFAULT_EVEN_ROW_COLOR = Color.WHITE;
    private static final Color DEFAULT_ODD_ROW_COLOR = Color.decode("0xEDF3FE");

    private Color evenRowColor = DEFAULT_EVEN_ROW_COLOR;
    private Color oddRowColor  = DEFAULT_ODD_ROW_COLOR;


    public JAlternatingRowColorTable() {
        super();
    }

    public JAlternatingRowColorTable(TableModel model) {
        super(model);
    }

    public void setEvenRowColor(Color evenRowColor) {
        this.evenRowColor = evenRowColor;
    }

    public void setOddRowColor(Color oddRowColor) {
        this.oddRowColor = oddRowColor;
    }

    public Color getEvenRowColor() {
        return evenRowColor;
    }

    public Color getOddRowColor() {
        return oddRowColor;
    }

    public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
        JComponent theRenderer = (JComponent) super.prepareRenderer(renderer, row, col);
        theRenderer.setBackground(row %2 == 0 ? evenRowColor : oddRowColor);
        if (isRowSelected(row)) {
            theRenderer.setBackground(getSelectionBackground());
        }
        if (!MenuUtilities.isMacOS()) theRenderer.setBorder(null);

        return theRenderer;
    }
}
