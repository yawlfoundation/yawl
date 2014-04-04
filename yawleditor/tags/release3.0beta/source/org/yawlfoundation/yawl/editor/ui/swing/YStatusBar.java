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

package org.yawlfoundation.yawl.editor.ui.swing;

import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationStateListener;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;

import javax.swing.*;
import java.awt.*;

public class YStatusBar extends JPanel implements SpecificationStateListener {

    private static final JLabel statusLabel = new JLabel();
    private static final JConnectionStatus modeIndicator = new JConnectionStatus();
    private JProgressBar progressBar;
    private SecondUpdateThread secondUpdateThread;
    private String previousStatusText;
    private UpdateProgressByTime updater;


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


    public void updateProgress(final int completionValue) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (completionValue < 0 || completionValue > 100) {
                    return;
                }
                progressBar.setVisible(true);
                progressBar.setValue(completionValue);
            }
        });
    }


    public void finishProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setVisible(false);
            }
        });
    }


    public void progressOverSeconds(final int pauseSeconds) {
//        updater = new UpdateProgressByTime(pauseSeconds);
//        updater.addPropertyChangeListener(new PropertyChangeListener() {
//            public void propertyChange(PropertyChangeEvent evt) {
//                if ("progress".equals(evt.getPropertyName())) {
//                    progressBar.setValue((Integer) evt.getNewValue());
//                }
//            }
//        });
//
//        updater.execute();

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
//        updater.cancel(true);
//        finishProgress();
        secondUpdateThread.reset();
    }

    public void freeze() {
 //       updater.cancel(true);
        secondUpdateThread.freeze();
    }

    public void specificationStateChange(SpecificationState state) {
        switch(state) {
            case NoNetsExist: {
                setText("Open or create a specification to begin.");
                break;
            }
            case NetsExist:
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

    class UpdateProgressByTime extends SwingWorker<Void, Integer> {

        private static final long WAIT_MSECS = 100;
        private final int seconds;

        UpdateProgressByTime(int seconds) {
            this.seconds = seconds;
         }

        protected Void doInBackground() throws Exception {
            int progress = 0;
            int maxMSecs = seconds * 1000;
            while (progress < maxMSecs) {
                progress += WAIT_MSECS;
                setProgress((progress / maxMSecs) * 100);
                pause(WAIT_MSECS);
            }
            return null;
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

    class SecondUpdateThread extends Thread {

        private final int APPARENTLY_INSTANT_MILLISECONDS = 50;
        private int pauseSeconds = 0;
        private volatile boolean shouldStop = false;

        public void run() {
            final int secondsAsMillis = pauseSeconds * 1000;
            final int pausePasses = secondsAsMillis / APPARENTLY_INSTANT_MILLISECONDS;
            for (int i = 0; i < pausePasses; i++) {
                if (shouldStop) {
                    break;
                }
                updateProgress((i * 100) / (pausePasses));
                pause(APPARENTLY_INSTANT_MILLISECONDS);
            }
        }

        public void setPauseSeconds(int seconds) {
            this.pauseSeconds = seconds;
        }

        public void reset() {
            shouldStop = true;
            finishProgress();
        }

        public void freeze() {
            shouldStop = true;
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
