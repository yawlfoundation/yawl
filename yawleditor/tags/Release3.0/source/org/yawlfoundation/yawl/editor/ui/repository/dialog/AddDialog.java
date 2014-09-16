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

package org.yawlfoundation.yawl.editor.ui.repository.dialog;

import org.yawlfoundation.yawl.editor.ui.properties.dialog.component.ButtonBar;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 6/08/13
 */
public class AddDialog extends JDialog implements ActionListener, CaretListener {

    private JTextField txtName;
    private JTextArea txtDescription;
    private ButtonBar _buttonBar;

    public AddDialog(JDialog owner, String defaultText) {
        super(owner);
        initialise();
        add(getContent());
        txtName.setText(defaultText);
        setPreferredSize(new Dimension(400, 320));
        pack();
    }

    public String getRecordName() { return txtName.getText(); }

    public String getRecordDescription() { return txtDescription.getText(); }


    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (action.equals("Cancel")) {
            txtName.setText(null);
        }
        setVisible(false);
    }

    public void caretUpdate(CaretEvent caretEvent) {
        _buttonBar.setOKEnabled(! (txtName.getText().length() == 0 ||
                txtDescription.getText().length() == 0));
    }


    private void initialise() {
        setTitle("Add to Repository");
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationByPlatform(true);
    }

    private JPanel getContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(5,5,5,5));
        content.add(createNamePanel(), BorderLayout.NORTH);
        content.add(createDescriptionPanel(), BorderLayout.CENTER);
        _buttonBar = new ButtonBar(this);
        content.add(_buttonBar, BorderLayout.SOUTH);
        return content;
    }


    private JPanel createNamePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new CompoundBorder(new EmptyBorder(5,5,5,5),
                new TitledBorder("Name")));
        txtName = new JTextField();
        txtName.addCaretListener(this);
        txtName.setPreferredSize(new Dimension(250,25));
        panel.add(txtName, BorderLayout.CENTER);
        return panel;
    }


    private JPanel createDescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new CompoundBorder(new EmptyBorder(5,5,5,5),
                        new TitledBorder("Description")));
        txtDescription = new JTextArea();
        txtDescription.setWrapStyleWord(true);
        txtDescription.setLineWrap(true);
        txtDescription.addCaretListener(this);
        JScrollPane pane = new JScrollPane(txtDescription);
        pane.setPreferredSize(new Dimension(300, 150));
        panel.add(pane, BorderLayout.CENTER);
        return panel;
    }

}
