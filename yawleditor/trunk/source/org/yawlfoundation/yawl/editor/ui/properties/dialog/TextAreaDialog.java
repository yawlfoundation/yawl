package org.yawlfoundation.yawl.editor.ui.properties.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Author: Michael Adams
 * Creation Date: 8/04/2010
 */
public class TextAreaDialog extends PropertyDialog implements ActionListener {

    private JTextArea _textArea;
    private String _text;

    public TextAreaDialog(Window parent, String title, String text) {
        super(parent);
        setTitle(title);
        _text = text;
        add(getContent());
        setPreferredSize(new Dimension(420, 270));
        pack();
    }


    protected JPanel getContent() {
        JPanel content = new JPanel();
        _textArea = new JTextArea(5, 20);
        _textArea.setLineWrap(true);
        _textArea.setWrapStyleWord(true);
        _textArea.setText(_text);
        JScrollPane scrollPane = new JScrollPane(_textArea);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        content.add(scrollPane);
        content.add(getButtonBar(this));
        return content;
    }

    public String showDialog() {
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
