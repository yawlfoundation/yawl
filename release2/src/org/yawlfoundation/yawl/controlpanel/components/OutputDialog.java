package org.yawlfoundation.yawl.controlpanel.components;

import org.yawlfoundation.yawl.controlpanel.YControlPanel;
import org.yawlfoundation.yawl.controlpanel.pubsub.EngineStatus;
import org.yawlfoundation.yawl.controlpanel.pubsub.Publisher;
import org.yawlfoundation.yawl.controlpanel.tailer.Tailer;
import org.yawlfoundation.yawl.controlpanel.tailer.TailerListenerAdapter;
import org.yawlfoundation.yawl.controlpanel.util.FileUtil;
import org.yawlfoundation.yawl.controlpanel.util.TomcatUtil;
import org.yawlfoundation.yawl.controlpanel.util.WindowUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        setTitle("YAWL " + YControlPanel.VERSION + " Output Log");
        buildUI();
        startTailer();
        addOnCloseHandler();
        setLocation(WindowUtil.calcLocation(mainWindow, this));
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
        _tailer = Tailer.create(getLogFile(), new TailerAppendListener(), 2000, true);
    }


    private File getLogFile() {
        return new File(FileUtil.buildPath(TomcatUtil.getCatalinaHome(),
                "logs", "catalina.out"));
    }


    /**********************************************************************/

    class TailerAppendListener extends TailerListenerAdapter {
        public void handle(final String line) {
            if (line.startsWith("ERROR: transport error 202:")) {
                Publisher.abortStarting();
            }
            else if (line.startsWith("INFO: Server startup ")) {
                new WaitThenAnnounce(3000, EngineStatus.Running);
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    _textArea.append(line + "\n");
                }
            });
        }
    }


    /**********************************************************************/

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


    /**********************************************************************/

    class WaitThenAnnounce {

        WaitThenAnnounce(int msecs, final EngineStatus status) {
            Timer timer = new Timer(msecs, new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    Publisher.statusChange(status);
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

}
