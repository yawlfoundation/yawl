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

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.awt.event.*;

/**
 * Author: Michael Adams
 * Creation Date: 8/04/2010
 */
public class TextAreaDialog extends PropertyDialog implements ActionListener {

    private JTextArea _textArea;
    private String _text;
    private KeyListener _consumer;

    public TextAreaDialog(Window parent, String text) {
        super(parent);
        setTitle("Update text");
        _text = text;
        add(getContent());
        setPreferredSize(new Dimension(420, 280));
        getOKButton().setEnabled(true);
        pack();
        _textArea.requestFocus();
    }


    protected JPanel getContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(7,7,0,7));
        _textArea = new JTextArea(5, 20);
        _textArea.setLineWrap(true);
        _textArea.setWrapStyleWord(true);
        _textArea.setText(_text);
        JScrollPane scrollPane = new JScrollPane(_textArea);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        content.add(scrollPane, BorderLayout.CENTER);
        content.add(getButtonBar(this), BorderLayout.SOUTH);
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


    public void setEditable(boolean editable) {
        if (editable) {
            _textArea.removeKeyListener(_consumer);
        }
        else {
            _consumer = new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    e.consume();  // ignore event
                }
            };
            _textArea.addKeyListener(_consumer);
        }
    }


    public void setSelection(int start, int end) {
        _textArea.select(start, end);
        try {
            _textArea.getHighlighter().addHighlight(start, end,
                    DefaultHighlighter.DefaultPainter);
        } catch (BadLocationException e) {
            // just don't highlight
        }
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
