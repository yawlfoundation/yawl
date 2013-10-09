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

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;
import com.l2fprod.common.swing.ComponentFactory;
import com.l2fprod.common.swing.PercentLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public abstract class DialogPropertyEditor extends AbstractPropertyEditor {

    protected JComponent label;

    public DialogPropertyEditor(JComponent renderer) {
        label = renderer;
        label.setOpaque(false);

        JButton button = ComponentFactory.Helper.getFactory().createMiniButton();
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDialog();
            }
        });

        JPanel cell = new JPanel(new PercentLayout(PercentLayout.HORIZONTAL, 0));
        cell.add("*", label);
        cell.add(button);
        cell.setOpaque(false);

        editor = cell;
    }


    protected abstract void showDialog();

}

