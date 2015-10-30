package org.yawlfoundation.yawl.controlpanel.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.PrintStream;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class OutputPanel extends JPanel {

    private AliasedTextPane _textArea;

    public OutputPanel() {
        super();
        setMinimumSize(getPreferredSize());
        buildUI();
        redirectSysOut();
    }

    public Dimension getPreferredSize() {
        return new Dimension(600, 400);
    }


    private void buildUI() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8,8,8,8));
        _textArea = new AliasedTextPane();
        _textArea.setForeground(new Color(50,50,50));
        _textArea.setBackground(new Color(252,252,252));
        _textArea.setBorder(new EmptyBorder(2, 4, 2, 0));
//        _textArea.setLineWrap(true);
//        _textArea.setWrapStyleWord(true);
        _textArea.setEditable(false);
        add(new JScrollPane(_textArea), BorderLayout.CENTER);
    }


    private void redirectSysOut() {
        TextAreaOutputStream osTextArea = new TextAreaOutputStream(_textArea);
        System.setOut(new PrintStream(osTextArea));
    }


    /**********************************************************************/

    class AliasedTextPane extends JTextPane {

        public AliasedTextPane() { super(); }

        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            RenderingHints rh = new RenderingHints(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
            g2.setRenderingHints(rh);
            super.paint(g);
        }

    }

}
