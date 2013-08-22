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
public class AddUserDefinedAttributeDialog extends JDialog
        implements ActionListener, CaretListener {

    private boolean cancelled;
    private JComboBox cbxTypes;
    private JTextField txtName;
    private JTextField txtEnumerations;
    private JButton btnOK;


    public AddUserDefinedAttributeDialog(Window parent) {
        super(parent);
        setModal(true);
        setTitle("Add a User-Defined Extended Attribute");
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        add(getContent());
        this.setPreferredSize(new Dimension(370, 120));
        pack();
    }


    private JPanel getContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.add(getNamePanel(), BorderLayout.NORTH);
        content.add(getEnumPanel(), BorderLayout.CENTER);
        content.add(getButtonBar(), BorderLayout.SOUTH);
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

    private JPanel getButtonBar() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(5,5,10,5));
        panel.add(createButton("Cancel"));
        btnOK = createButton("OK");
        btnOK.setEnabled(false);
        panel.add(btnOK);
        return panel;
    }


    private JButton createButton(String caption) {
        JButton btn = new JButton(caption);
        btn.setActionCommand(caption);
        btn.setPreferredSize(new Dimension(75,25));
        btn.addActionListener(this);
        return btn;
    }


    public void actionPerformed(ActionEvent event) {
        cancelled = ! event.getActionCommand().equals("OK");
        setVisible(false);
    }


    public void caretUpdate(CaretEvent caretEvent) {
        boolean enable = ! txtName.getText().isEmpty();
        if (isEnumerationTypeSelected()) {
            String values = txtEnumerations.getText();
            int commaPos = values.lastIndexOf(',');
            enable = enable && commaPos > -1 && commaPos < values.length() - 1;
        }
        btnOK.setEnabled(enable);
    }

    public boolean isCancelled() {
        return cancelled;
    }


    public String getName() {
        return cancelled ? null : txtName.getText();
    }

    public String getType() {
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
