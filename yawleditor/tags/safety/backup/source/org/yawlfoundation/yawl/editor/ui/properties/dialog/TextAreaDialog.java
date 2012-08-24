package org.yawlfoundation.yawl.editor.ui.properties.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Author: Michael Adams
 * Creation Date: 8/04/2010
 */
public class TextAreaDialog extends JDialog implements ActionListener {

    private JTextArea _textArea;
    private String _text;
    private Component _parent;
    private boolean _shownBefore;

    public TextAreaDialog(Component parent, String title, String text) {
        super();
        setModal(true);
        setTitle(title);
        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        _text = text;
        _parent = parent;
        add(getContent());
        this.setPreferredSize(new Dimension(420, 270));
        pack();
    }


    private JPanel getContent() {
        JPanel content = new JPanel();
        _textArea = new JTextArea(5, 20);
        _textArea.setLineWrap(true);
        _textArea.setWrapStyleWord(true);
        _textArea.setText(_text);
        JScrollPane scrollPane = new JScrollPane(_textArea);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        JButton btnOK = new JButton("OK");
        btnOK.setActionCommand("OK");
        btnOK.addActionListener(this);
        JButton btnCancel = new JButton("Cancel");
        btnCancel.setActionCommand("Cancel");
        btnCancel.addActionListener(this);
        content.add(scrollPane);
        content.add(btnCancel);
        content.add(btnOK);
        return content;
    }

    public String showDialog() {
        if (! _shownBefore) {
            setLocationRelativeTo(_parent);
            _shownBefore = true;
        }
        setVisible(true);
        return _text;
    }


    public String getText() {
        return _text;
    }


    public void setText(String text) {
        _text = text;
    }


    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("OK")) {
            _text = _textArea.getText();
        }
        else {
            _text = null;
        }
        setVisible(false);
    }
}
