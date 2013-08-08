package org.yawlfoundation.yawl.editor.ui.repository.dialog;

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

    JButton btnOK;

    JTextField txtName;
    JTextArea txtDescription;

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
        btnOK.setEnabled(! (txtName.getText().length() == 0 ||
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
        content.add(createButtonBar(), BorderLayout.SOUTH);
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

    private JPanel createButtonBar() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));
        panel.add(createButton("Cancel"));
        btnOK = createButton("OK");
        btnOK.setEnabled(false);
        panel.add(btnOK);
        return panel;
    }


    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.setActionCommand(label);
        button.setMnemonic(label.charAt(0));
        button.setPreferredSize(new Dimension(70,25));
        button.addActionListener(this);
        return button;
    }

}
