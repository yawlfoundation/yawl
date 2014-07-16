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

package org.yawlfoundation.yawl.editor.ui.properties.extended.editor;

import com.l2fprod.common.swing.ComponentFactory;
import org.yawlfoundation.yawl.editor.ui.properties.editor.FontPropertyEditor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 16/07/2014
 */
public class AttributeFontPropertyEditor extends FontPropertyEditor {

    public AttributeFontPropertyEditor() {
        super();

        JButton btnCancel = ComponentFactory.Helper.getFactory().createMiniButton();
        btnCancel.setText("X");

        btnCancel.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
              setValue(null);
              firePropertyChange(getValue(), null);
          }
        });

        ((JPanel) editor).add(btnCancel);
    }

}
