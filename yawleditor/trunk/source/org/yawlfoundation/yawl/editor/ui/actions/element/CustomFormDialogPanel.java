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

package org.yawlfoundation.yawl.editor.ui.actions.element;

import javax.swing.*;
import java.awt.*;

/**
 * Author: Michael Adams
 * Creation Date: 4/09/2009
 */
public class CustomFormDialogPanel extends JPanel {

    private JTextArea textArea;

    public CustomFormDialogPanel() {
        setLayout(new BorderLayout(5, 5));
        setMinimumSize(new Dimension(300, 100));
        setPreferredSize(new Dimension(300, 100));
        JLabel label = new JLabel("Please enter a URI for the Custom Form:");
        add(label, BorderLayout.NORTH);
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane);
    }

    public void setURI(String text) {
        textArea.setText(text);
    }

    public String getURI() {
        return textArea.getText();
    }








    
}
