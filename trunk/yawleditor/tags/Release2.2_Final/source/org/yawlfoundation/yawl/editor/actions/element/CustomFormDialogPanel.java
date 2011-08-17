package org.yawlfoundation.yawl.editor.actions.element;

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
