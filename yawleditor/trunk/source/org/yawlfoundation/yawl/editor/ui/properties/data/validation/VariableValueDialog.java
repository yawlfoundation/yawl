package org.yawlfoundation.yawl.editor.ui.properties.data.validation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Author: Michael Adams
 * Creation Date: 16/08/2013
 */
public class VariableValueDialog extends JDialog implements ActionListener {

    private InstanceEditorPane _editorPane;
    private String _varName;
    private String _dataType;
    private String _value;


    public VariableValueDialog(Window parent, String varName,
                               String dataType, String value) {
        super(parent);
        _varName = varName;
        _dataType = dataType;
        _value = value;
        setModal(true);
        setTitle("Edit Value for Variable " + varName);
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        add(getContent());
        setPreferredSize(new Dimension(420, 270));
        pack();
    }


    public String showDialog() {
        setVisible(true);
        return _value;
    }


    public String getText() { return _value; }

    public void setText(String text) { _value = text; }


    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("OK")) {
            _value = _editorPane.getText();
        }
        else {                                               // cancel
            _value = null;
        }
        setVisible(false);
    }


    private JPanel getContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(5,10,5,10));
        _editorPane = new InstanceEditorPane(_dataType, _value);
        content.add(_editorPane, BorderLayout.CENTER);
        content.add(createButtonBar(), BorderLayout.SOUTH);
        return content;
    }


    private JPanel createButtonBar() {
        JPanel panel = new JPanel();
        JButton btnOK = new JButton("OK");
        btnOK.setActionCommand("OK");
        btnOK.addActionListener(this);
        JButton btnCancel = new JButton("Cancel");
        btnCancel.setActionCommand("Cancel");
        btnCancel.addActionListener(this);
        panel.add(btnCancel);
        panel.add(btnOK);
        return panel;
    }

}
