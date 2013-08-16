package org.yawlfoundation.yawl.editor.ui.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Michael Adams
 * @date 14/08/13
 */
public class MoreDialog extends JDialog {

    public MoreDialog(Window owner, String text) {
        super(owner);
        init(owner, text);
    }

    public MoreDialog(Window owner, java.util.List<String> textList) {
        super(owner);
        init(owner, coalesceText(textList));
    }


    private void init(Window owner, String text) {
        setUndecorated(true);
        setModal(true);
        add(getContent(text));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(owner);
        pack();
    }


    private String coalesceText(java.util.List<String> textList) {
        StringBuilder s = new StringBuilder();
        if (textList != null) {
            for (String text : textList) {
                if (! text.contains("foo_bar")) {
                    s.append(text).append('\n');
                }
            }
        }
        return s.toString();
    }

    private JScrollPane getContent(String text) {
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(new Color(255, 254, 226));
        textArea.setForeground(Color.DARK_GRAY);
        textArea.setMargin(new Insets(5, 7, 5, 7));
        textArea.setText(text);
        textArea.setCaretPosition(0);
        textArea.setEditable(false);

        textArea.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent mouseEvent) {
                        setVisible(false);
                    }
                });

        textArea.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent keyEvent) {
                setVisible(false);
            }
        });

        JScrollPane pane = new JScrollPane(textArea);
        pane.setPreferredSize(new Dimension(500, 60));
        return pane;
    }

}
