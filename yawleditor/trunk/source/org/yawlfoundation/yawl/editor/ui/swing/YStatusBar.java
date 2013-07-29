/*
 * Created on 18/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.yawlfoundation.yawl.editor.ui.swing;

import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationStateListener;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;

import javax.swing.*;
import java.awt.*;

public class YStatusBar extends JPanel implements SpecificationStateListener {

    private static JLabel statusLabel = new JLabel();
    private static JConnectionStatus modeIndicator = new JConnectionStatus();
    private JProgressBar progressBar;
    private SecondUpdateThread secondUpdateThread;
    private String previousStatusText;


    public YStatusBar() {
        super();
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
        statusLabel.setForeground(Color.DARK_GRAY);
        statusLabel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLoweredBevelBorder(),
                        BorderFactory.createEmptyBorder(0, 2, 0, 2)
                )
        );

        add(modeIndicator, BorderLayout.WEST);
        add(statusLabel, BorderLayout.CENTER);
        add(getProgressBar(), BorderLayout.EAST);
        Publisher.getInstance().subscribe(this);
    }


    public String getText() {
        return statusLabel.getText();
    }


    public void setText(String message) {
        previousStatusText = getText();
        statusLabel.setText(" " + message);
    }


    public void setTextToPrevious() {
        setText(previousStatusText);
    }


    public void setConnectionMode(String component, boolean online) {
        modeIndicator.setStatusMode(component, online);
    }


    private JProgressBar getProgressBar() {
        progressBar = new JProgressBar();
        progressBar.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLoweredBevelBorder(),
                        BorderFactory.createEmptyBorder(1, 1, 1, 1)
                )
        );

        progressBar.setPreferredSize(new Dimension(200, 1));
        progressBar.setVisible(false);
        return progressBar;
    }


    public void updateProgress(int completionValue) {
        if (completionValue < 0 || completionValue > 100) {
            return;
        }
        progressBar.setVisible(true);
        progressBar.setValue(completionValue);
    }


    public void finishProgress() {
        progressBar.setVisible(false);
    }


    public void progressOverSeconds(final int pauseSeconds) {
        secondUpdateThread = new SecondUpdateThread();
        try {
            secondUpdateThread.setPauseSeconds(pauseSeconds);
            secondUpdateThread.start();
        }
        catch (Exception e) {
            LogWriter.error("Error initialising status bar", e);
        }
    }


    public void resetProgress() {
        secondUpdateThread.reset();
    }


    public void specificationStateChange(SpecificationState state) {
        switch(state) {
            case NoNetsExist: {
                setText("Open or create a specification to begin.");
                break;
            }
            case NetsExist: {
                setText("Select a net to continue editing it.");
                break;
            }
            case NoNetSelected: {
                setText("Select a net to continue editing it.");
                break;
            }
            case NetSelected: {
                setText("Use the palette toolbar to edit the selected net.");
                break;
            }
        }
    }



    /******************************************************************************/

    class SecondUpdateThread extends Thread {

        private int APPARENTLY_INSTANT_MILLISECONDS = 50;
        private int pauseSeconds = 0;
        private volatile boolean shouldReset = false;

        public void run() {
            final int secondsAsMillis = pauseSeconds * 1000;
            final int pausePasses = secondsAsMillis / APPARENTLY_INSTANT_MILLISECONDS;
            for (int i = 0; i < pausePasses; i++) {
                if (shouldReset) {
                    return;
                }
                updateProgress((i * 100) / (pausePasses));
                pause(APPARENTLY_INSTANT_MILLISECONDS);
            }
        }

        public void setPauseSeconds(int seconds) {
            this.pauseSeconds = seconds;
        }

        public void reset() {
            this.shouldReset = true;
            finishProgress();
        }

        private void pause(long milliseconds) {
            Object lock = new Object();
            long now = System.currentTimeMillis();
            long finishTime = now + milliseconds;
            while (now < finishTime) {
                long timeToWait = finishTime - now;
                synchronized (lock) {
                    try {
                        lock.wait(timeToWait);
                    }
                    catch (InterruptedException ex) {
                    }
                }
                now = System.currentTimeMillis();
            }
        }
    }
}
