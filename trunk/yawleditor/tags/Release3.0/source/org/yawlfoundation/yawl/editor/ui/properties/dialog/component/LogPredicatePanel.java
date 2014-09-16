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
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * @author Michael Adams
 * @date 26/06/2014
 */
public class LogPredicatePanel extends JPanel {

    private JTextPane _textPane;


    public LogPredicatePanel(String title) {
        _textPane = new JTextPane(new LogPredicateStyledDocument());
        JScrollPane scrollPane = new JScrollPane(_textPane);
        scrollPane.setPreferredSize(new Dimension(350, 100));
        add(scrollPane);
        setBorder(new TitledBorder(title));
    }


    public void setScope(LogPredicateScope scope) {
        ((LogPredicateStyledDocument) _textPane.getDocument()).setPredicateScope(scope);
    }

    public void setFocus() { _textPane.requestFocus(); }

    public String getText() { return _textPane.getText(); }

    public void setText(String text) { _textPane.setText(text); }

}
