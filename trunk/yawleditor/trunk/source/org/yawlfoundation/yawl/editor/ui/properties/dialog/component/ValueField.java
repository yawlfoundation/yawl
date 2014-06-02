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

package org.yawlfoundation.yawl.editor.ui.properties.dialog.component;

import javax.swing.*;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 2/06/2014
 */
public class ValueField extends JPanel {

    JTextField _textField;

    public ValueField(ActionListener actionListener, CaretListener caretListener) {
        setLayout(new BorderLayout());
        _textField = new JTextField();
        _textField.addCaretListener(caretListener);
        JButton btnExpand = new JButton("...");
        btnExpand.setPreferredSize(new Dimension(20, 20));
        btnExpand.addActionListener(actionListener);
        btnExpand.setActionCommand("ShowDialog");
        add(btnExpand, BorderLayout.EAST);
        add(_textField, BorderLayout.CENTER);
    }


    public JTextField getTextField() { return _textField; }

    public String getText() {return _textField.getText(); }

    public void setText(String text) { _textField.setText(text); }

}
