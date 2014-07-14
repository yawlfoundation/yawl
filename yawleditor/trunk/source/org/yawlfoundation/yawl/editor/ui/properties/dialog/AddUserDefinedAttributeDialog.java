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

package org.yawlfoundation.yawl.editor.ui.properties.dialog;

import org.yawlfoundation.yawl.editor.ui.properties.UserDefinedAttributes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Vector;

/**
 * Author: Michael Adams
 * Creation Date: 8/04/2010
 */
public class AddUserDefinedAttributeDialog extends PropertyDialog
        implements ActionListener, CaretListener {

    private boolean cancelled;
    private JComboBox cbxTypes;
    private JTextField txtName;
    private JTextField txtEnumerations;
    private ExtendedAttributesDialog parentDialog;


    public AddUserDefinedAttributeDialog(ExtendedAttributesDialog parent) {
        super(parent);
        parentDialog = parent;
        setTitle("Add a User-Defined Extended Attribute");
        setPreferredSize(new Dimension(370, 120));
        pack();
    }


    protected JPanel getContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.add(getNamePanel(), BorderLayout.NORTH);
        content.add(getEnumPanel(), BorderLayout.CENTER);
        content.add(getButtonBar(this), BorderLayout.SOUTH);
        return content;
    }


    private JPanel getNamePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.add(new JLabel("Name: "));
        txtName = new JTextField();
        txtName.setPreferredSize(new Dimension(150, 25));
        txtName.addCaretListener(this);
        panel.add(txtName);
        panel.add(new JLabel("Type: "));
        panel.add(createTypeCombo());
        return panel;
    }

    private JPanel getEnumPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(5,13,5,13));
        txtEnumerations = new JTextField();
        txtEnumerations.setToolTipText("Enter comma-separated enumerated values");
        txtEnumerations.addCaretListener(this);
        panel.add(new JLabel("Values: "), BorderLayout.WEST);
        panel.add(txtEnumerations, BorderLayout.CENTER);
        return panel;
    }


    private JComboBox createTypeCombo() {
        Vector<String> items = new Vector<String>(UserDefinedAttributes.VALID_TYPE_NAMES);
        items.add("enumeration");
        Collections.sort(items);
        cbxTypes = new JComboBox(items);
        cbxTypes.setPreferredSize(new Dimension(100, 25));
        cbxTypes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                setSize(370, (isEnumerationTypeSelected() ? 155 : 120));
                caretUpdate(null);
            }
        });
        return cbxTypes;
    }


    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        if (cmd.equals("OK") && ! parentDialog.isUniqueName(getName())) {
            JOptionPane.showMessageDialog(this,
                    "An attribute with that name already exists.",
                    "Unable to add attribute", JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus();
            txtName.selectAll();
        }
        else {
            cancelled = ! cmd.equals("OK");
            setVisible(false);
        }
    }


    public void caretUpdate(CaretEvent caretEvent) {
        boolean enable = ! txtName.getText().isEmpty();
        if (isEnumerationTypeSelected()) {
            String values = txtEnumerations.getText();
            int commaPos = values.lastIndexOf(',');
            enable = enable && commaPos > -1 && commaPos < values.length() - 1;
        }
        getOKButton().setEnabled(enable);
    }

    public boolean isCancelled() {
        return cancelled;
    }


    public String getName() {
        return cancelled ? null : txtName.getText();
    }

    public String getSelectedType() {
        if (cancelled) return null;
        String selectedType = (String) cbxTypes.getSelectedItem();
        if (isEnumerationTypeSelected(selectedType)) {
            return constructEnumeration();
        }
        return selectedType;
    }


    private String constructEnumeration() {
        StringBuilder s = new StringBuilder("enumeration{");
        s.append(txtEnumerations.getText());
        s.append('}');
        return s.toString();
    }


    private boolean isEnumerationTypeSelected() {
        return isEnumerationTypeSelected((String) cbxTypes.getSelectedItem());
    }


    private boolean isEnumerationTypeSelected(String selectedType) {
        return selectedType != null && selectedType.equals("enumeration");
    }

}
