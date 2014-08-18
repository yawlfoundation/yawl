package org.yawlfoundation.yawl.launch.components;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.yawlfoundation.yawl.launch.YControlPanel;
import org.yawlfoundation.yawl.launch.util.TomcatUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * @author Michael Adams
 * @date 4/08/2014
 */
public class OutputDialog extends JDialog {

    private AliasedTextArea _textArea;
    private Tailer _tailer;

    public OutputDialog(JFrame mainWindow) {
        super(mainWindow);
        setResizable(true);
        setModal(false);
        setMinimumSize(new Dimension(600, 400));
        setTitle("YAWL " + YControlPanel.VERSION + " Output Log Window");
        setLocationByPlatform(true);
        buildUI();
        startTailer();
        addOnCloseHandler();
        setVisible(true);
    }


    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(8,8,8,8));
        _textArea = new AliasedTextArea();
        _textArea.setForeground(Color.WHITE);
        _textArea.setBackground(Color.BLACK);
        _textArea.setBorder(new EmptyBorder(2, 4, 2, 0));
        _textArea.setLineWrap(true);
        _textArea.setWrapStyleWord(true);
        _textArea.setEditable(false);
        content.add(new JScrollPane(_textArea), BorderLayout.CENTER);
        add(content);
    }


    private void addOnCloseHandler() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                _tailer.stop();
                dispose();
            }
        });
    }


    private void startTailer() {
        _tailer = Tailer.create(getLogFile(), new TailerListener(), 2000, true);
    }


    private File getLogFile() {
        return new File(TomcatUtil.getCatalinaHome() + "/logs/catalina.out");
    }


    /**********************************************************************/

    class TailerListener extends TailerListenerAdapter {
        public void handle(final String line) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    _textArea.setEditable(true);
                    _textArea.append(line + "\n");
                    _textArea.setCaretPosition(_textArea.getText().length());
                    _textArea.setEditable(false);
                }
            });
        }
    }


    class AliasedTextArea extends JTextArea {

        public AliasedTextArea() { super(); }

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
